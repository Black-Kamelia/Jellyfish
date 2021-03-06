package com.kamelia.jellyfish.rest.user

import com.kamelia.jellyfish.core.Hasher
import com.kamelia.jellyfish.database.Connection
import com.kamelia.jellyfish.rest.core.auditable.AuditableUUIDEntity
import com.kamelia.jellyfish.rest.core.auditable.AuditableUUIDTable
import com.kamelia.jellyfish.rest.file.File
import com.kamelia.jellyfish.rest.file.Files
import java.time.Instant
import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.timestamp

enum class UserRole(private val power: Int) {
    REGULAR(1),
    ADMIN(10),
    OWNER(100),

    ;

    infix fun le(other: UserRole): Boolean = power <= other.power
    infix fun ge(other: UserRole): Boolean = power >= other.power
    infix fun gt(other: UserRole): Boolean = power > other.power
    infix fun lt(other: UserRole): Boolean = power < other.power
    infix fun eq(other: UserRole): Boolean = power == other.power
    infix fun ne(other: UserRole): Boolean = power != other.power
}

object Users : AuditableUUIDTable("users") {

    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 128).uniqueIndex()
    val password = varchar("password", 255)
    val role = enumerationByName("role", 32, UserRole::class)
    val enabled = bool("enabled")
    val lastInvalidation = timestamp("last_invalidation").nullable()
    val uploadToken = varchar("upload_token", 32)

    override val createdBy = reference("created_by", this)
    override val updatedBy = reference("updated_by", this).nullable()

    suspend fun countAll(): Long = Connection.query {
        User.count()
    }

    suspend fun getAll(): List<User> = Connection.query {
        User.all().toList()
    }

    suspend fun getAll(page: Long, pageSize: Int): List<User> = Connection.query {
        User.all()
            .limit(pageSize, page * pageSize)
            .toList()
    }

    suspend fun findById(uuid: UUID): User? = Connection.query {
        User.findById(uuid)
    }

    suspend fun findByUsername(username: String): User? = Connection.query {
        User.find { Users.username eq username }
            .firstOrNull()
    }

    suspend fun findByEmail(email: String): User? = Connection.query {
        User.find { Users.email eq email }
            .firstOrNull()
    }

    suspend fun findByUploadToken(uploadToken: String): User? = Connection.query {
        User.find { Users.uploadToken eq uploadToken }
            .firstOrNull()
    }

    suspend fun create(user: UserDTO, creator: User? = null): User = Connection.query {
        User.new {
            username = user.username
            email = user.email
            password = Hasher.hash(user.password)
            role = user.role
            enabled = false
            uploadToken = UUID.randomUUID().toString().replace("-", "")

            onCreate(creator ?: this)
        }
    }

    suspend fun update(user: User, dto: UserUpdateDTO, updater: User): User = Connection.query {
        user.apply {
            dto.username?.let { username = it }
            dto.email?.let { email = it }
            dto.role?.let { role = it }
            dto.enabled?.let { enabled = it }

            onUpdate(updater)
        }
    }

    suspend fun updatePassword(user: User, dto: UserPasswordUpdateDTO, updater: User): User = Connection.query {
        user.apply {
            password = Hasher.hash(dto.newPassword)
            onUpdate(updater)
        }
    }

    suspend fun delete(id: UUID): User? = Connection.query {
        User.findById(id)
            ?.apply { delete() }
    }

    suspend fun logoutAll(user: User) = Connection.query {
        user.lastInvalidation = Instant.now()
    }

    suspend fun regenerateUploadToken(user: User): User = Connection.query {
        user.uploadToken = UUID.randomUUID().toString().replace("-", "")
        user
    }
}

class User(id: EntityID<UUID>) : AuditableUUIDEntity(id, Users) {
    companion object : UUIDEntityClass<User>(Users)

    var username by Users.username
    var email by Users.email
    var password by Users.password
    var role by Users.role
    var enabled by Users.enabled
    var lastInvalidation by Users.lastInvalidation
    var uploadToken by Users.uploadToken

    private val files by File referrersOn Files.owner

    suspend fun countFiles(): Long = Connection.query {
        files.count()
    }

    suspend fun getFiles(): List<File> = Connection.query {
        files.toList()
    }

    suspend fun getFiles(page: Long, pageSize: Int): List<File> = Connection.query {
        files.limit(pageSize, page * pageSize).toList()
    }
}
