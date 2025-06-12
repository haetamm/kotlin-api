package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.specification.CustomerSpecification
import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.entities.customer.*
import com.belajar.api.kotlin.exception.BadRequestException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.Customer
import com.belajar.api.kotlin.repository.CustomerRepository
import com.belajar.api.kotlin.service.CustomerService
import com.belajar.api.kotlin.validation.ValidationUtil
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(rollbackFor = [Exception::class])
class CustomerServiceImpl(
    private val customerRepository: CustomerRepository,
    private val validationUtil: ValidationUtil,
    private val specification: CustomerSpecification,
): CustomerService {

    @Transactional(rollbackFor = [Exception::class])
    override fun saveAccount(request: NewAccountRequest): Customer {
        return customerRepository.saveAndFlush(
            Customer(
                name = request.name!!,
                phone = request.phone,
                userAccount = request.userAccount
            )
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun saveOrGet(request: CustomerRequest): CustomerResponse {
        validationUtil.validate(request)
        val customer = Customer(
            name = request.name,
            phone = request.phoneNumber,
        )

        val saved = customerRepository.findByNameLikeIgnoreCaseAndPhoneEquals(request.name, request.phoneNumber)
            .orElseGet { customerRepository.saveAndFlush(customer) }

        return saved?.let {
            createCustomerResponse(saved)
        } ?: throw RuntimeException("Failed to save or get customer")

    }

    @Transactional(rollbackFor = [Exception::class])
    override fun saveBulk(requests: List<CustomerRequest>): List<CustomerResponse> {
        validationUtil.validateAll(requests)
        val responses = mutableListOf<CustomerResponse>()
        requests.forEach { request ->
            val existingCustomer = customerRepository.findByNameLikeIgnoreCaseAndPhoneEquals(request.name, request.phoneNumber)
            val customer = existingCustomer.orElseGet {
                val newCustomer = Customer(
                    name = request.name,
                    phone = request.phoneNumber
                )
                customerRepository.saveAndFlush(newCustomer)
            }
            val response = createCustomerResponse(customer)
            responses.add(response)
        }
        return responses
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getById(id: String): CustomerResponse {
        val customer = getCustomerById(id)
        return createCustomerResponse(customer)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getAll(request: SearchCustomerRequest): Page<CustomerResponse> {
        val customerSpecification = specification.specification(request)

        val sort = Sort.by(Sort.Direction.fromString(request.direction), request.sortBy)
        val page = if (request.page <= 0) 1 else request.page
        val pageable = PageRequest.of(page - 1, request.size, sort)

        val customers = customerRepository.findAll(customerSpecification, pageable)
        return customers.map { customer ->
            createCustomerResponse(customer)
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun update(request: UpdateCustomerRequest, id: String): CustomerResponse {
        validationUtil.validate(request)
        val customer = getCustomerById(id)

        if (customer.userAccount != null) {
            throw BadRequestException("Member customers cannot be updated.")
        }
        customer.name = request.name
        customer.phone = request.phoneNumber
        customer.address = request.address
        customerRepository.saveAndFlush(customer)
        return createCustomerResponse(customer)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun delete(id: String): String {
        val customer = getCustomerById(id)
        if (customer.userAccount != null) {
            throw BadRequestException("Member customers cannot be deleted.")
        }
        customerRepository.softDelete(customer.id.toString())
        return StatusMessage.SUCCESS_DELETE
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getCustomerByNameAndPhone(name: String, phone: String): Customer? {
        return customerRepository.findByNameLikeIgnoreCaseAndPhoneEquals(name, phone).orElse(null)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun save(name: String, phone: String): Customer {
        return customerRepository.saveAndFlush(
            Customer(
                name = name,
                phone = phone
            )
        )
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getCustomerByUserId(id: Int): Customer {
        return customerRepository.findByUserAccountId(id)
            ?: throw NotFoundException(StatusMessage.CUSTOMER_NOT_FOUND)
    }

    private fun createCustomerResponse(customer: Customer): CustomerResponse {
        return CustomerResponse(
            id = customer.id!!,
            name = customer.name,
            phoneNumber = customer.phone!!,
            address = customer.address,
            member = customer.userAccount != null
        )
    }

    private fun getCustomerById(id: String): Customer {
        return customerRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.CUSTOMER_NOT_FOUND)
        }
    }
}