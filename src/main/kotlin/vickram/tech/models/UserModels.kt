package vickram.tech.models

import kotlinx.serialization.Serializable
import vickram.tech.utils.*
import java.time.LocalDateTime
import java.util.*

@Serializable
data class User(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    val name: String,
    val email: String,
    val password: String,
    val phone: String,
    val role: ROLE = ROLE.CUSTOMER,
    val profile: String? = null,
    @Serializable(with = DateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = DateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun validate(): User {
        if (name.isBlank()) {
            throw BlankException("User name cannot be blank")
        }

        if (email.isBlank()) {
            throw BlankException("User email cannot be blank")
        }

        if (!emailPattern.toRegex().matches(email)) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (password.isBlank()) {
            throw BlankException("User password cannot be blank")
        }

        if (password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters long")
        }

        if (phone.isBlank()) {
            throw BlankException("User phone cannot be blank")
        }

        if (!phonePattern.toRegex().matches(phone)) {
            throw IllegalArgumentException("Invalid phone number format")
        }

        return this
    }
}

@Serializable
data class Credential(
    val email: String,
    val password: String
) {
    fun validate(): Credential {
        if (email.isBlank()) {
            throw BlankException("Email cannot be blank")
        }

        if (!emailPattern.toRegex().matches(email)) {
            throw IllegalArgumentException("Invalid email format")
        }

        if (password.isBlank()) {
            throw BlankException("Password cannot be blank")
        }

        if (password.length < 8) {
            throw IllegalArgumentException("Password must be at least 8 characters long")
        }

        return this
    }
}

@Serializable
data class RefreshToken(
    val refreshToken: String
)

@Serializable
data class Address(
    @Serializable(with = UUIDSerializer::class)
    val id: UUID = UUID.randomUUID(),
    @Serializable(with = UUIDSerializer::class)
    val userId: UUID,
    val name: String,
    val phone: String,
    val county: String,
    val city: String,
    val address: String,
    @Serializable(with = DateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Serializable(with = DateTimeSerializer::class)
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    fun validate(): Address {
        if (name.isBlank()) {
            throw BlankException("Address name cannot be blank")
        }

        if (county.isBlank()) {
            throw BlankException("County cannot be blank")
        }

        if (city.isBlank()) {
            throw BlankException("City cannot be blank")
        }

        if (address.isBlank()) {
            throw BlankException("Address cannot be blank")
        }

        if (phone.isBlank()) {
            throw BlankException("Phone cannot be blank")
        }

        if (!phonePattern.toRegex().matches(phone)) {
            throw IllegalArgumentException("Invalid phone number format")
        }

        return this
    }
}


