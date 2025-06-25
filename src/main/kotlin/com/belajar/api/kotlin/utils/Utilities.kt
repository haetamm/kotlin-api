package com.belajar.api.kotlin.utils

import com.belajar.api.kotlin.entities.WebResponse
import com.belajar.api.kotlin.exception.BadRequestException
import com.belajar.api.kotlin.model.BillDetail
import com.belajar.api.kotlin.model.UserAccount
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import org.hashids.Hashids
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import java.security.MessageDigest
import java.text.NumberFormat

@Component
class Utilities {

    @Value("\${hashed.salt}")
    private lateinit var salt: String

    @Value("\${midtrans.api.key}")
    private lateinit var midtransServerKey: String

    private val hashids: Hashids by lazy { Hashids(salt, 8) }

    internal fun encodeId(id: Int): String = hashids.encode(id.toLong())

    internal fun decodeId(hash: String): Int {
        val decoded = hashids.decode(hash)
        if (decoded.isEmpty()) throw BadRequestException("Invalid ID")
        return decoded[0].toInt()
    }

    internal fun encodeUuid(uuid: String): String =
        Base64.getUrlEncoder().withoutPadding().encodeToString(uuid.toByteArray())


    internal fun decodeUuid(encoded: String): String =
        String(Base64.getUrlDecoder().decode(encoded))

    internal  fun getUserId(): Int {
        val principal = SecurityContextHolder.getContext().authentication.principal
        if (principal is UserAccount) {
            return principal.id!!
        }
        throw IllegalStateException("Invalid user principal")
    }

    internal fun <T> handleRequest(requestHandler: () -> T, status: HttpStatus, message: String): ResponseEntity<WebResponse<T>> {
        val data = requestHandler()
        val response = WebResponse(
            code = status.value(),
            status = message,
            data = data,
            paginationResponse = null
        )
        return ResponseEntity.status(status).body(response)
    }

    internal fun parseDate(requestDate: String?, format: String): Date {
        val simpleDateFormat = SimpleDateFormat(format)
        return try {
            simpleDateFormat.parse(requestDate)
        } catch (e: ParseException) {
            throw RuntimeException(e)
        }
    }

    internal fun verifyMidtransBodySignature(
        orderId: String,
        statusCode: String,
        grossAmount: String,
        actualSignatureKey: String
    ): Boolean {
        val toHash = orderId + statusCode + grossAmount + midtransServerKey
        val expectedSignature = MessageDigest
            .getInstance("SHA-512")
            .digest(toHash.toByteArray())
            .joinToString("") { "%02x".format(it) }

        return expectedSignature.equals(actualSignatureKey, ignoreCase = true)
    }

    internal fun calculateTotalAmount(billDetails: List<BillDetail>): String {
        val total = billDetails.sumOf { it.qty * it.price }
        val formatter = NumberFormat.getNumberInstance(Locale("in", "ID"))
        return formatter.format(total)
    }


}