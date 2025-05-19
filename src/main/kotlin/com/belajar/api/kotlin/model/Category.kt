package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import jakarta.persistence.*
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where

@Entity
@Table(name = TableName.M_CATEGORY)
@SQLDelete(sql = "UPDATE " + TableName.M_CATEGORY + " SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
data class Category(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(name = "name", nullable = false, unique = true)
    var name: String,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    var menus: List<Menu> = mutableListOf()
)
