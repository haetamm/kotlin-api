package com.belajar.api.kotlin.model

import com.belajar.api.kotlin.constant.TableName
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.time.LocalDateTime
import jakarta.persistence.Table

@Entity
@Table(name = TableName.M_USER_ACCOUNT)
data class UserAccount (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null,

    @Column(unique = true, nullable = false, name = "email")
    var email: String,

    @Column(unique = true, nullable = false, name = "username")
    private var username: String,

    @JsonIgnore
    @Column(name = "password", nullable = false)
    private var password: String,

    @Column(name = "is_enable")
    var isEnable: Boolean = false,

    @Column(name  = "confirmed")
    var confirmed: Boolean = false,

    @Column(name = "confirmation_token")
    var confirmationToken: String? = null,

    @Column(name = "pending_email")
    var pendingEmail: String? = null,

    @Column(name = "confirmation_email_token")
    var confirmationEmailToken: String? = null,

    @Column(name = "confirmation_token_expiry")
    var confirmationTokenExpiry: LocalDateTime? = null,

    @Column(name = "resetPasswordToken")
    var resetPasswordToken: String? = null,

    @ManyToMany(fetch = FetchType.EAGER)
    var roles: List<UserRole> = mutableListOf(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
) : UserDetails {

    @JsonIgnore
    override fun getPassword(): String = password

    override fun getUsername(): String = username

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = isEnable

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
    }

    @PreUpdate
    fun onUpdate() {
        updatedAt = LocalDateTime.now()
    }

    override fun getAuthorities(): Collection<GrantedAuthority> =
        roles.mapNotNull { role -> role.role?.name?.let { SimpleGrantedAuthority(it) } }

    fun updateUsername(newUsername: String) {
        this.username = newUsername
    }

    fun updatePassword(newPassword: String) {
        this.password = newPassword
    }

    internal fun comparePassword(password: String): Boolean {
        return BCryptPasswordEncoder().matches(password, this.password)
    }

}
