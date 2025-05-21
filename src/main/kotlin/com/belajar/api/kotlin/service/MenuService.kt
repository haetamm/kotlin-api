package com.belajar.api.kotlin.service

import com.belajar.api.kotlin.entities.menu.MenuRequest
import com.belajar.api.kotlin.entities.menu.MenuResponse
import com.belajar.api.kotlin.entities.menu.SearchMenuRequest
import com.belajar.api.kotlin.entities.menu.UpdateMenuRequest
import com.belajar.api.kotlin.model.Menu
import org.springframework.data.domain.Page
import org.springframework.web.multipart.MultipartFile

interface MenuService {
    fun save(request: MenuRequest, image: MultipartFile): MenuResponse
    fun saveBulk(requests: List<MenuRequest>): List<MenuResponse>
    fun getById(id: String): MenuResponse
    fun updateById(request: UpdateMenuRequest, updateImage: MultipartFile?, id: String): MenuResponse
    fun getAll(request: SearchMenuRequest): Page<MenuResponse>
    fun delete(id: String): String
    fun getMenuById(id: String): Menu

    fun findById(id: String): Menu
}