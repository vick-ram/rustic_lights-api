package vickram.tech.db

import org.jetbrains.exposed.dao.UUIDEntity
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.UUIDTable
import org.jetbrains.exposed.sql.javatime.datetime
import vickram.tech.models.User
import vickram.tech.utils.PGEnum
import vickram.tech.utils.ROLE
import java.time.LocalDateTime
import java.util.*

object Users: UUIDTable("users") {
    val name = varchar("name", 250)
    val email = varchar("email", 250).uniqueIndex()
    val password = varchar("password", 250)
    val phone = varchar("phone", 250)
    val role = customEnumeration(
        "role",
        "ROLE",
        { value -> ROLE.valueOf(value as String) },
        { PGEnum("ROLE", it) }
    ).default(ROLE.CUSTOMER)
    val profile = varchar("profile", 250).nullable()
    val createdAt = datetime("created_at").default(LocalDateTime.now())
    val updatedAt = datetime("updated_at").default(LocalDateTime.now())
}

class UserEntity(id: EntityID<UUID>): UUIDEntity(id) {
    companion object: UUIDEntityClass<UserEntity>(Users)

    var name by Users.name
    var email by Users.email
    var password by Users.password
    var phone by Users.phone
    var role by Users.role
    var profile by Users.profile
    var createdAt by Users.createdAt
    var updatedAt by Users.updatedAt


    fun toUser() = User(
        id = id.value,
        name = name,
        email = email,
        password = password,
        phone = phone,
        role = role,
        profile = profile,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
