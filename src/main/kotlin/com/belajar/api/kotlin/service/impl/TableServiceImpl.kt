package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.table.TableRequest
import com.belajar.api.kotlin.entities.table.TableResponse
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.TableRest
import com.belajar.api.kotlin.repository.TableRepository
import com.belajar.api.kotlin.service.TableService
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class TableServiceImpl(
    private val validationUtil: ValidationUtil,
    private val tableRepository: TableRepository
): TableService {

    @Transactional(rollbackFor = [Exception::class])
    override fun save(request: TableRequest): TableResponse {
        validationUtil.validate(request)
        val table = tableRepository.saveAndFlush(
            TableRest(
                name = request.name,
                isTaken = request.isTaken
            )
        )
        return createTableResponse(table)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getById(id: String): TableResponse {
        val table = getTableById(id)
        return createTableResponse(table)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(): List<TableResponse> {
        val tables = tableRepository.findAll()
        return tables.map { table ->
            createTableResponse(table)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun update(request: TableRequest, id: String): TableResponse {
        validationUtil.validate(request)
        val table = getTableById(id)
        table.name = request.name
        table.isTaken = request.isTaken
        tableRepository.saveAndFlush(table)
        return createTableResponse(table)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun delete(id: String): String {
        val table = getTableById(id)
        tableRepository.softDelete(table.id.toString())
        return StatusMessage.SUCCESS_DELETE
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getByName(name: String): TableRest {
        val table = tableRepository.findByNameLikeIgnoreCase(name).orElseThrow {
            throw NotFoundException(StatusMessage.TABLE_NOT_FOUND)
        }
        return table
    }

    private fun createTableResponse(table: TableRest): TableResponse {
        return TableResponse(
            id = table.id!!,
            name = table.name,
            isTaken = table.isTaken
        )
    }

    private fun getTableById(id: String): TableRest {
        return tableRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.TABLE_NOT_FOUND)
        }
    }

}