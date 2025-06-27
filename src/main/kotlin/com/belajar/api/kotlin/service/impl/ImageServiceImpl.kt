package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.exception.BadRequestException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.Image
import com.belajar.api.kotlin.repository.ImageRepository
import com.belajar.api.kotlin.service.ImageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.reactive.function.client.WebClient

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    private var webClient: WebClient,
    ): ImageService {

    @Value("\${app.supabase.url}")
    private lateinit var supabaseUrl: String

    @Value("\${app.supabase.api.key}")
    private lateinit var supabaseApiKey: String

    @Value("\${app.supabase.bucket}")
    private lateinit var supabaseBucket: String

    @Transactional(rollbackFor = [Exception::class])
    override fun save(image: MultipartFile): Image {
        validateImage(image)
        val fileName = uploadToSupabaseStorage(image)
        val publicUrl = "$supabaseUrl/storage/v1/object/public/$supabaseBucket/$fileName"

        val saved = Image(
            name = fileName,
            path = publicUrl,
            size = image.size,
            contentType = image.contentType!!
        )
        return imageRepository.saveAndFlush(saved)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteById(id: String) {
        val image = findById(id)
        callSupabaseStorage("DELETE", image.name)
        imageRepository.deleteById(id)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateById(id: String, updateImage: MultipartFile): Image {
        validateImage(updateImage)
        val image = findById(id)

        // Delete image lama
        callSupabaseStorage("DELETE", image.name)

        // Upload baru
        val newFileName = uploadToSupabaseStorage(updateImage)
        val newPublicUrl = "$supabaseUrl/storage/v1/object/public/$supabaseBucket/$newFileName"

        // Update entity
        image.name = newFileName
        image.path = newPublicUrl
        image.size = updateImage.size
        image.contentType = updateImage.contentType!!

        return image
    }

    private fun findById(id: String): Image {
        return imageRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.IMAGE_NOT_FOUND)
        }
    }

    private fun validateImage(image: MultipartFile) {
        val allowedContentTypes = listOf("image/jpg", "image/jpeg", "image/png")
        val maxSize = 307_200

        if (!allowedContentTypes.contains(image.contentType)) {
            throw BadRequestException("Invalid image type")
        }

        if (image.size > maxSize) {
            throw BadRequestException("Image size exceeds limit of 300 KB")
        }
    }

    private fun callSupabaseStorage(
        method: String,
        fileName: String,
        contentType: String? = null,
        body: ByteArray? = null
    ) {
        val url = "$supabaseUrl/storage/v1/object/$supabaseBucket/$fileName"

        val request = when (method.uppercase()) {
            "PUT" -> webClient.put()
                .uri(url)
                .header("Authorization", "Bearer $supabaseApiKey")
                .header("Content-Type", contentType ?: "application/octet-stream")
                .bodyValue(body!!)
            "DELETE" -> webClient.delete()
                .uri(url)
                .header("Authorization", "Bearer $supabaseApiKey")
            else -> throw BadRequestException("Unsupported method: $method")
        }

        request.retrieve()
            .onStatus({ it.isError }) { response ->
                response.bodyToMono(String::class.java).map {
                    println("$method failed. Response: $it")
                    throw BadRequestException("$method to Supabase failed: $it")
                }.block()
            }
            .toBodilessEntity()
            .block()
    }

    private fun uploadToSupabaseStorage(file: MultipartFile): String {
        val fileName = "${System.currentTimeMillis()}-${file.originalFilename}"
        callSupabaseStorage(
            method = "PUT",
            fileName = fileName,
            contentType = file.contentType,
            body = file.bytes
        )
        println("Upload success. File name: $fileName")
        return fileName
    }

}