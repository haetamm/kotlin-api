package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.model.Image
import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile

interface ImageService {
    fun save(image: MultipartFile): Image
    fun deleteById(id: String)
    fun updateById(id: String, updateImage: MultipartFile): Image
}