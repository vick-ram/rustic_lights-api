package vickram.tech.controllers

import vickram.tech.db.UserEntity
import vickram.tech.db.Users
import vickram.tech.models.Credential
import vickram.tech.models.User
import vickram.tech.plugins.Payload
import vickram.tech.plugins.TokenResp
import vickram.tech.plugins.dbQuery
import vickram.tech.plugins.makeJwt
import vickram.tech.utils.NotFoundException
import vickram.tech.utils.hashPassword
import vickram.tech.utils.verifyPassword
import java.time.LocalDateTime
import java.util.*

val blacklistedTokens: MutableSet<String> = Collections.synchronizedSet(HashSet())

fun blackListToken(userId: String) {
    blacklistedTokens.add(userId)
}

suspend fun createUser(user: User): User = dbQuery {
    val userExists = UserEntity.find { Users.email eq user.email }.empty()
    if (!userExists) {
        throw IllegalArgumentException("User with email ${user.email} already exists")
    }
    val newUser = UserEntity.new {
        this.name = user.name.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
        this.email = user.email.lowercase()
        this.password = hashPassword(user.password)
        this.phone = user.phone
        this.profile = user.profile
        this.createdAt = LocalDateTime.now()
        this.updatedAt = LocalDateTime.now()
    }

    return@dbQuery newUser.toUser()
}

suspend fun authenticateUser(
    credential: Credential,
    payload: Payload
): TokenResp = dbQuery {
    val user = UserEntity.find { Users.email eq credential.email }.firstOrNull()
    if (user == null) {
        throw IllegalArgumentException("User with email ${credential.email} not found")
    }
    if (!verifyPassword(credential.password, user.password)) {
        throw IllegalArgumentException("Password is incorrect")
    }
    return@dbQuery makeJwt(
        payload = payload,
        email = user.email,
        userId = user.id.toString()
    )
}

suspend fun updateUserProfile(
    id: UUID,
    profile: String
): User = dbQuery {
    val user = UserEntity.findById(id) ?: throw NotFoundException("User not found")
    user.profile = profile
    user.updatedAt = LocalDateTime.now()
    return@dbQuery user.toUser()
}
/*To be called later*/
suspend fun logout(token: String) {
    blackListToken(token)
}

suspend fun getUser(id: UUID): User? = dbQuery {
    return@dbQuery UserEntity
        .findById(id)
        ?.toUser()
}

suspend fun getUsers(): List<User> = dbQuery {
    return@dbQuery UserEntity
        .all()
        .map(UserEntity::toUser)
}

suspend fun getUserByEmail(email: String): User = dbQuery {
    return@dbQuery UserEntity
        .find { Users.email eq email }
        .firstOrNull()
        ?.toUser()
        ?: throw NotFoundException("User not found")
}

suspend fun updateUser(id: UUID, user: User): User? = dbQuery {
    val userEntity = UserEntity.findById(id) ?: return@dbQuery null
    userEntity.name = user.name
    userEntity.email = user.email
    userEntity.password = hashPassword(user.password)
    userEntity.phone = user.phone
    userEntity.profile = user.profile
    userEntity.updatedAt = LocalDateTime.now()
    return@dbQuery userEntity.toUser()
}

suspend fun deleteUser(id: UUID): Boolean = dbQuery {
    val user = UserEntity
        .findById(id)
        ?: throw NotFoundException("User not found")
    user.delete()
    return@dbQuery true
}