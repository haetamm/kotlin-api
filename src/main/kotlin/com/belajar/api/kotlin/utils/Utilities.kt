package com.belajar.api.kotlin.utils

import com.belajar.api.kotlin.entities.WebResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Component
class Utilities {

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
}