package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.MidtransTransactionStatus
import com.belajar.api.kotlin.entities.payment.MidtransWebhookRequest
import com.belajar.api.kotlin.exception.BadRequestException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.Bill
import com.belajar.api.kotlin.model.Payment
import com.belajar.api.kotlin.repository.BillRepository
import com.belajar.api.kotlin.repository.PaymentRepository
import com.belajar.api.kotlin.repository.UserAccountRepository
import com.belajar.api.kotlin.service.EmailService
import com.belajar.api.kotlin.service.PaymentService
import com.belajar.api.kotlin.utils.Utilities
import com.midtrans.httpclient.SnapApi
import com.midtrans.httpclient.TransactionApi
import com.midtrans.httpclient.error.MidtransError
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import com.midtrans.Midtrans
import org.springframework.transaction.annotation.Transactional
import org.json.JSONObject
import java.time.temporal.ChronoUnit
import java.time.Instant
import java.time.LocalDateTime

@Service
class PaymentServiceImpl(
    private val paymentRepository: PaymentRepository,
    private val billRepository: BillRepository,
    private val utilities: Utilities,
    private val userAccountRepository: UserAccountRepository,
    private val emailService: EmailService,
    @Value("\${midtrans.api.key}") private val key: String,

): PaymentService {

    @Value("\${template_api.url-frontend}")
    private lateinit var urlFrontend: String
    init {
        // Set Midtrans server key
        Midtrans.serverKey = key
        // Set Midtrans environment
        Midtrans.isProduction = false
    }

    private val log = LoggerFactory.getLogger(PaymentServiceImpl::class.java)

    @Transactional(rollbackFor = [Exception::class])
    override fun createPayment(bill: Bill): Payment {

        val amount = bill.billDetails?.sumOf { value ->
            value.price * value.qty
        } ?: 0L

        val paymentItemDetailRequest = bill.billDetails?.map { billDetail ->
            mapOf(
                "id" to billDetail.id!!,
                "name" to billDetail.menu.name,
                "price" to billDetail.price,
                "quantity" to billDetail.qty
            )
        } ?: emptyList()

        val transactionDetails = mapOf(
            "order_id" to bill.id,
            "gross_amount" to amount.toString()
        )

        val customerDetails = mapOf(
            "first_name" to bill.customer.name,
            "phone" to bill.customer.phone
        )

        val requestBody = mapOf(
            "transaction_details" to transactionDetails,
            "customer_details" to customerDetails,
            "credit_card" to mapOf("secure" to true),
            "item_details" to paymentItemDetailRequest
        )

        val (transactionToken, redirectUrl) = try {
            val transactionToken = SnapApi.createTransactionToken(requestBody)
            val redirectUrl = SnapApi.createTransactionRedirectUrl(requestBody)
            transactionToken to redirectUrl
        } catch (e: MidtransError) {
            log.error("Error creating transaction token or redirect URL: ${e.message}")
            throw RuntimeException("Error creating transaction token or redirect URL", e)
        }

        val payment = Payment(
            token = transactionToken,
            redirectUrl = redirectUrl,
            transactionStatus = "ordered"
        )
        paymentRepository.saveAndFlush(payment)
        return payment
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun handleWebhook(request: MidtransWebhookRequest): String {

        /* ---------- 1. Verifikasi signature Midtrans ---------- */
        val isValidSignature = utilities.verifyMidtransBodySignature(
            orderId = request.order_id,
            statusCode = request.status_code,
            grossAmount = request.gross_amount,
            actualSignatureKey = request.signature_key
        )
        if (!isValidSignature) {
            log.warn("Invalid Midtrans signature for order_id=${request.order_id}")
            throw BadRequestException("Invalid Midtrans signature")
        }

        /* ---------- 2. Ambil Bill & Payment ---------- */
        val bill = billRepository.findById(request.order_id).orElseThrow {
            NotFoundException("Bill not found for order_id=${request.order_id}")
        }
        val payment = bill.payment ?: run {
            log.warn("Bill found but no associated payment: order_id=${request.order_id}")
            return "No payment associated with bill"
        }

        /* ---------- 3. Update status pembayaran ---------- */
        val statusEnum = MidtransTransactionStatus.from(request.transaction_status)
            ?: throw BadRequestException("Unknown transaction status: ${request.transaction_status}")

        payment.transactionStatus = statusEnum.name.lowercase()
        paymentRepository.save(payment)
        log.info("Payment status updated for order_id=${request.order_id} to ${payment.transactionStatus}")

        /* ---------- 4. Kirim Email jika pembayaran sukses ---------- */
        if (request.transaction_status == "settlement") {
            val admins = userAccountRepository.findAll()
                .filter { it.roles.any { r -> r.role?.name in listOf("ROLE_ADMIN", "ROLE_SUPER_ADMIN") } }

            val totalAmount = utilities.calculateTotalAmount(bill.billDetails!!)

            val details = bill.billDetails?.joinToString("\n") {
                "- ${it.menu.name} x${it.qty} = Rp. ${"%,d".format(it.qty * it.price)}"
            } ?: "- (tidak ada detail)"


            admins.forEach { admin ->
                try {

                    val subject = "Pembayaran Berhasil dari ${bill.customer.name}"

                    val message = """
                        Pelanggan: ${bill.customer.name}<br/>
                        No. Telepon: ${bill.phone ?: bill.customer.phone}<br/>
                        Alamat Pengiriman: ${bill.deliveryAddress ?: "-"}<br/>
                        <br/>
                        Detail Pesanan:<br/>
                        ${details.replace("\n", "<br/>")}
                        <br/>
                        <br/>
                        Total Pembayaran: Rp. $totalAmount
                        <br/>
                    """.trimIndent()


                    val variables = mapOf(
                        "username" to admin.username,
                        "subject"  to subject,
                        "message"  to message,
                        "logoUrl"  to "$urlFrontend/img/logo.png",
                        "year"     to LocalDateTime.now().year.toString()
                    )


                    emailService.sendTemplateEmail(
                        user          = admin,
                        subject       = subject,
                        templatePath  = "templates/email_payment_success.html",
                        variables     = variables
                    )

                    log.info("✅ Email sent to ${admin.email}")
                } catch (e: Exception) {
                    log.error("🔥 Failed to send email to ${admin.email}: ${e.message}", e)
                }
            }
        }
        return "OK"
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun syncPaymentStatus(orderId: String): String {
        val rawOrderId = utilities.decodeUuid(orderId)
        log.info("🔍 Decoded orderId: $rawOrderId")

        val bill = billRepository.findById(rawOrderId).orElseThrow {
            log.error(" Bill not found for order_id=$rawOrderId")
            NotFoundException("Bill not found for order_id=$rawOrderId")
        }

        val payment = bill.payment ?: throw BadRequestException("No associated payment for bill")

        val response: JSONObject = try {
            log.info(" Checking transaction status with Midtrans for order_id=$rawOrderId")
            TransactionApi.checkTransaction(rawOrderId)
        } catch (e: Exception) {
            log.warn("⚠ Midtrans transaction not found for order_id=$rawOrderId, likely never started. ${e.message}")

            val transDateInstant = bill.transDate.toInstant()
            val now = Instant.now()
            val oneDayAgo = now.minus(1, ChronoUnit.DAYS)
            log.info("🕒 transDate=$transDateInstant | now=$now | 1-day threshold=$oneDayAgo")

            return if (transDateInstant.isBefore(oneDayAgo)) {
                log.info("❌ Transaction never started and >1 day old. Forcing status to 'expire'")
                payment.transactionStatus = "expire"
                paymentRepository.save(payment)
                "expire"
            } else {
                log.info("ℹ️ Transaction never started but still <1 day. Keeping status as 'ordered'")
                payment.transactionStatus = "ordered"
                paymentRepository.save(payment)
                "ordered"
            }
        }

        val transactionStatus = response.optString("transaction_status", "")
        val fraudStatus = response.optString("fraud_status", null)

        log.info("✅ Midtrans response: transaction_status=$transactionStatus, fraud_status=$fraudStatus")

        if (transactionStatus.isNotBlank() && payment.transactionStatus != transactionStatus) {
            log.info("🔄 Updating local transaction status from '${payment.transactionStatus}' to '$transactionStatus'")
            payment.transactionStatus = transactionStatus
            paymentRepository.save(payment)
        } else {
            log.info("✔ No change in status. Current status: '${payment.transactionStatus}'")
        }

        return payment.transactionStatus
    }

}