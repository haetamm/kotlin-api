package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.constant.TransTypeEnum
import com.belajar.api.kotlin.entities.trans_type.TransTypeResponse
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.TransType
import com.belajar.api.kotlin.repository.TransTypeRepository
import com.belajar.api.kotlin.service.TransTypeService
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransTypeServiceImpl(
    private val transTypeRepository: TransTypeRepository
): TransTypeService {

    @Transactional(rollbackFor = [Exception::class])
    @PostConstruct
    fun initTransType() {
        val transTypes = listOf(
            TransType(TransTypeEnum.DI, "Dine In"),
            TransType(TransTypeEnum.D, "Delivery")
        )

        transTypes.forEach { transType ->
            if (!transTypeRepository.existsById(transType.id)) {
                transTypeRepository.save(transType)
            }
        }
        transTypeRepository.flush()
    }

    override fun getById(id: String): TransTypeResponse {
        val transType = transTypeRepository.findTransTypeByEnumId(id).orElseThrow {
            throw NotFoundException(StatusMessage.TRANS_TYPE_NOT_FOUND)
        }
        return createTransTypeResponse(transType)
    }

    private fun createTransTypeResponse(transType: TransType): TransTypeResponse {
        return TransTypeResponse(
            id = transType.id.toString(),
            description = transType.description
        )
    }
}