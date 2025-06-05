package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import jakarta.persistence.*
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where

@Entity
@Table(name = TableName.M_TABLE)
@SQLDelete(sql = "UPDATE " + TableName.M_TABLE + " SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
data class TableRest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "is_taken", nullable = false)
    var isTaken: Boolean = false,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,
)
