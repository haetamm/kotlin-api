package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.constant.TransTypeEnum
import com.belajar.api.kotlin.entities.bill.*
import com.belajar.api.kotlin.entities.bill_detail.BillDetailResponse
import com.belajar.api.kotlin.entities.payment.PaymentResponse
import com.belajar.api.kotlin.exception.BadRequestException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.Bill
import com.belajar.api.kotlin.model.BillDetail
import com.belajar.api.kotlin.model.Customer
import com.belajar.api.kotlin.model.TransType
import com.belajar.api.kotlin.repository.BillDetailRepository
import com.belajar.api.kotlin.repository.BillRepository
import com.belajar.api.kotlin.repository.CartItemRepository
import com.belajar.api.kotlin.repository.CartRepository
import com.belajar.api.kotlin.service.*
import com.belajar.api.kotlin.specification.BillSpecification
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.Date

@Service
class BillServiceImpl(
    private val validationUtil: ValidationUtil,
    private val customerService: CustomerService,
    private val transTypeService: TransTypeService,
    private val tableService: TableService,
    private val billRepository: BillRepository,
    private val menuService: MenuService,
    private val paymentService: PaymentService,
    private val billDetailRepository: BillDetailRepository,
    private val specification: BillSpecification,
    private val cartService: CartService,
    private val cartItemRepository: CartItemRepository,
    private val cartRepository: CartRepository
): BillService {

    @Transactional(rollbackFor = [Exception::class])
    override fun createDineInBill(request: DineInBillRequest): BillResponse {
        validationUtil.validate(request)

        val customer: Customer = customerService.getCustomerByNameAndPhone(request.customerName, request.customerPhone)
            ?: customerService.save(request.customerName, request.customerPhone)

        val transType = transTypeService.getById(TransTypeEnum.DI.toString())

        val table = tableService.getByName(request.tableName)

        val bill = Bill(
            customer = customer,
            transDate = Date.from(Instant.now()),
            table = table,
            transType = TransType(
                id = TransTypeEnum.valueOf(transType.id),
                description = transType.description
            )
        )
        billRepository.saveAndFlush(bill)

        val billDetails = request.billRequest.map { billDetailRequest ->
            validationUtil.validate(billDetailRequest)
            val menu = menuService.findById(billDetailRequest.menuId)
            BillDetail(
                bill = bill,
                menu = menu,
                qty = billDetailRequest.qty,
                price = menu.price
            )
        }
        billDetailRepository.saveAll(billDetails)
        bill.billDetails = billDetails

        val payment = paymentService.createPayment(bill)
        bill.payment = payment
        billRepository.saveAndFlush(bill)

        return createBillResponse(bill)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun createDeliveryBill(request: DeliveryBillRequest): BillResponse {
        validationUtil.validate(request)
        val userId = getUserId()
        val customer = customerService.getCustomerByUserId(userId.toInt())

        val transType = transTypeService.getById(TransTypeEnum.D.toString())

        // Ambil cart milik customer
        val cart = cartService.findByCustomerId(customer.id.toString())
        val cartItems = cart.items ?: emptyList()
        val cartMenuMap = cartItems.associateBy { it.menu.id }

        // Validasi menuId harus ada di cart
        val requestMenuIds = request.billRequest.map { it.menuId }
        val notInCart = requestMenuIds.filter { it !in cartMenuMap.keys }
        if (notInCart.isNotEmpty()) {
            throw BadRequestException("Menu(s) not found in cart")
        }

        // Buat Bill
        val bill = Bill(
            recipientName = request.recipientName,
            phone = request.phone,
            deliveryAddress = request.deliveryAddress,
            customer = customer,
            transDate = Date.from(Instant.now()),
            transType = TransType(
                id = TransTypeEnum.valueOf(transType.id),
                description = transType.description
            )
        )
        billRepository.saveAndFlush(bill)

        // Buat BillDetail dari menu di cart
        val billDetails = request.billRequest.map { billDetailRequest ->
            validationUtil.validate(billDetailRequest)
            val menu = menuService.findById(billDetailRequest.menuId)
            BillDetail(
                bill = bill,
                menu = menu,
                qty = billDetailRequest.qty,
                price = menu.price
            )
        }
        billDetailRepository.saveAll(billDetails)
        bill.billDetails = billDetails

        // Hapus item dari cart sesuai dengan billRequest
        val itemsToRemove = cartItems.filter { it.menu.id in requestMenuIds }
        if (itemsToRemove.isNotEmpty()) {
            cart.items = cartItems.filter { it.menu.id !in requestMenuIds }
            cartItemRepository.deleteAll(itemsToRemove)
            cartRepository.save(cart)
        }

        // Buat Payment
        val payment = paymentService.createPayment(bill)
        bill.payment = payment
        billRepository.saveAndFlush(bill)

        return createBillResponse(bill)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getById(id: String): BillResponse {
        val bill = findById(id)
        return createBillResponse(bill)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(request: SearchBillRequest): Page<BillResponse> {
        val billSpecification = specification.specification(request)

        val sort = Sort.by(Sort.Direction.fromString(request.direction), request.sortBy)
        val page = if (request.page <= 0) 1 else request.page
        val pageable = PageRequest.of(page - 1, request.size, sort)

        val bills = billRepository.findAll(billSpecification, pageable)
        return bills.map { bill ->
            createBillResponse(bill)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getByCurrentUser(request: SearchBillRequest): Page<BillResponse> {
        val userId = getUserId()

        // Filter berdasarkan userId di Customer.userAccount.id
        val userSpec = Specification<Bill> { root, _, cb ->
            cb.equal(root.get<Any>("customer").get<Any>("userAccount").get<String>("id"), userId)
        }

        // Ambil spesifikasi filter lainnya dari request
        val requestSpec = specification.specification(request)

        // Gabungkan semua spesifikasi
        val billSpecification = Specification.where(userSpec).and(requestSpec)

        val sort = Sort.by(Sort.Direction.fromString(request.direction), request.sortBy)
        val page = if (request.page <= 0) 1 else request.page
        val pageable = PageRequest.of(page - 1, request.size, sort)

        val bills = billRepository.findAll(billSpecification, pageable)
        return bills.map { bill -> createBillResponse(bill) }
    }


    @Transactional(rollbackFor = [Exception::class])
    override fun updateStatusPayment(request: UpdateBillRequest, id: String): String {
        val bill = findById(id)
        val payment = bill.payment
        if (payment != null) {
            payment.transactionStatus = request.transactionStatus
        }
        return StatusMessage.SUCCESS_UPDATE
    }

    private fun findById(id: String): Bill {
        return billRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.BILL_NOT_FOUND)
        }
    }

    private fun getUserId(): String {
        return SecurityContextHolder.getContext().authentication.name
    }

    private fun createBillResponse(bill: Bill): BillResponse {
        val totalPayment = bill.billDetails?.sumOf { it.qty * it.price } ?: 0L
        return BillResponse(
            id = bill.id!!,
            recipientName = bill.recipientName,
            phone = bill.phone,
            deliveryAddress = bill.deliveryAddress,
            transDate = bill.transDate.toString(),
            customerId = bill.customer.id!!,
            customerName = bill.customer.name,
            tableName = bill.table?.name,
            transType = bill.transType.description,
            billDetails = bill.billDetails!!.map { billDetail ->
                BillDetailResponse(
                    id = billDetail.id!!,
                    menuId = billDetail.menu.id!!,
                    name = billDetail.menu.name,
                    qty = billDetail.qty,
                    price = billDetail.price
                )
            },
            payment = PaymentResponse(
                id = bill.payment?.id!!,
                token = bill.payment!!.token,
                transactionStatus = bill.payment!!.transactionStatus,
                redirectUrl = bill.payment!!.redirectUrl
            ),
            totalPayment
        )
    }

}