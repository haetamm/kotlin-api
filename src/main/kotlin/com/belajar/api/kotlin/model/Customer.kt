package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.SQLDelete
import org.hibernate.annotations.Where


@Entity
@Table(name = TableName.M_CUSTOMER)
@SQLDelete(sql = "UPDATE " + TableName.M_CUSTOMER + " SET deleted = true WHERE id = ?") // Perhatikan penggunaan TableName.M_CUSTOMER untuk menyertakan nama tabel dalam pernyataan SQL
@Where(clause = "deleted = false") // Anotasi @Where digunakan untuk menentukan kondisi secara global untuk menyembunyikan entitas yang telah dihapus secara logis
data class Customer (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,

    @Column(name = "name", nullable = false)
    var name: String,

    @Column(name = "phone_number")
    var phone: String? = null,

    @Column(name = "address")
    var address: String? = null,

    @Column(name = "deleted", nullable = false)
    var deleted: Boolean = false,

    @OneToOne
    @JoinColumn(name = "user_account_id", unique = true) val userAccount: UserAccount? = null
)



