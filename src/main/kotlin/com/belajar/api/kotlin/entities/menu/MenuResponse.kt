package com.belajar.api.kotlin.entities.menu

import com.belajar.api.kotlin.entities.image.ImageResponse

data class MenuResponse(
    var id: String,
    var name: String,
    var price: Long,
    var image: ImageResponse?,
    var category: String,
    var categoryId: String?
)
