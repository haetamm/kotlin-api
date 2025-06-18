package com.belajar.api.kotlin.service.impl

import com.belajar.api.kotlin.constant.StatusMessage
import com.belajar.api.kotlin.exception.BadRequestException
import com.belajar.api.kotlin.exception.NotFoundException
import com.belajar.api.kotlin.model.Image
import com.belajar.api.kotlin.repository.ImageRepository
import com.belajar.api.kotlin.service.ImageService
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

@Service
class ImageServiceImpl(
    private val imageRepository: ImageRepository,
    @Value("\${template_api.image.path}") private val path: String
): ImageService {

    private lateinit var imagePath: Path

    @PostConstruct
    fun initPath() {
        imagePath = Paths.get(path)
        if (!Files.exists(imagePath)) {
            try {
                Files.createDirectories(imagePath)
            } catch (e: IOException) {
                val errorMessage = "Failed to create image directory: $path"
                println(errorMessage)
            }
        }
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun save(image: MultipartFile): Image {
        val fileName = validateAndSaveImage(image)
        val filePath: Path = imagePath.resolve(fileName)

        val saved = Image(
            name = fileName,
            path = filePath.toString(),
            size = image.size,
            contentType = image.contentType!!
        )
        return imageRepository.saveAndFlush(saved)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun getById(id: String): Resource {
        val image = findById(id)
        val filePath = Paths.get(image.path)
        return UrlResource(filePath.toUri())
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun softDeleteById(id: String) {
        val image = findById(id)
        imageRepository.softDelete(image.id!!)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun deleteById(id: String) {
        val image = findById(id)
        // Hapus file fisik dari sistem file
        val filePath = Paths.get(image.path)
        try {
            Files.deleteIfExists(filePath)
        } catch (e: IOException) {
            println("Failed to delete image file: ${image.path}, error: ${e.message}")
        }
        // Hapus record dari database secara permanen
        imageRepository.deleteById(id)
    }

    @Transactional(rollbackFor = [Exception::class])
    override fun updateById(id: String, updateImage: MultipartFile): Image {
        val image = findById(id)

        val filePath = Paths.get(image.path)
        Files.delete(filePath)

        val newFileName = validateAndSaveImage(updateImage)
        val newFilePath: Path = imagePath.resolve(newFileName)

        image.name = newFileName
        image.path = newFilePath.toString()
        image.size = updateImage.size
        image.contentType = updateImage.contentType!!

        return image
    }

    private fun findById(id: String): Image {
        return imageRepository.findById(id).orElseThrow {
            throw NotFoundException(StatusMessage.IMAGE_NOT_FOUND)
        }
    }

    private fun validateAndSaveImage(image: MultipartFile): String {
        val allowedContentTypes = listOf("image/jpg", "image/jpeg", "image/png")
        val maxSize = 307_200

        if (!allowedContentTypes.contains(image.contentType)) {
            throw BadRequestException("Invalid image type")
        }

        if (image.size > maxSize) {
            throw BadRequestException("Image size exceeds limit of 300 KB")
        }

        val fileName = "${System.currentTimeMillis()}${image.originalFilename}"
        val filePath: Path = imagePath.resolve(fileName)
        Files.copy(image.inputStream, filePath)

        return fileName
    }

}