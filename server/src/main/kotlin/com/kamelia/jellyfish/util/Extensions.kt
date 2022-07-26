package com.kamelia.jellyfish.util

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload
import com.kamelia.jellyfish.core.ExpiredOrInvalidTokenException
import com.kamelia.jellyfish.core.InvalidUUIDException
import com.kamelia.jellyfish.core.MissingHeaderException
import com.kamelia.jellyfish.core.MissingParameterException
import com.kamelia.jellyfish.core.MultipartParseException
import com.kamelia.jellyfish.rest.core.pageable.PageDefinitionDTO
import com.kamelia.jellyfish.rest.user.UserRole
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.contentType
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.util.pipeline.PipelineContext
import java.util.UUID
import org.jetbrains.exposed.dao.UUIDEntity

fun ApplicationCall.getParamOrNull(name: String): String? = parameters[name]

fun ApplicationCall.getParam(name: String): String = getParamOrNull(name) ?: throw MissingParameterException(name)

fun String.toUUIDOrNull() = try {
    UUID.fromString(this)
} catch (e: IllegalArgumentException) {
    null
}

fun String.toUUID(): UUID = try {
    UUID.fromString(this)
} catch (e: IllegalArgumentException) {
    throw InvalidUUIDException()
}

val UUIDEntity.uuid: UUID
    get() = id.value

fun ApplicationCall.getUUIDOrNull(name: String = "uuid"): UUID? = getParamOrNull(name)?.toUUIDOrNull()

fun ApplicationCall.getUUID(name: String = "uuid"): UUID = getUUIDOrNull(name) ?: throw InvalidUUIDException()

suspend fun ApplicationCall.receivePageDefinition(): PageDefinitionDTO = if (request.contentType() == ApplicationJSON) {
    receive()
} else {
    PageDefinitionDTO()
}

val PipelineContext<*, ApplicationCall>.jwt: Payload
    get() = this.call.principal<JWTPrincipal>()?.payload ?: throw ExpiredOrInvalidTokenException()

operator fun Payload.get(key: String): Claim = this.getClaim(key)

val Payload.uuid get() = this["id"].asString().toUUID()

inline fun PipelineContext<*, ApplicationCall>.ifRegular(block: () -> Unit) {
    val role = UserRole.valueOf(jwt["role"].asString())
    if (role == UserRole.REGULAR) block()
}

inline fun PipelineContext<*, ApplicationCall>.ifNotRegular(block: () -> Unit) {
    val role = UserRole.valueOf(jwt["role"].asString())
    if (role != UserRole.REGULAR) block()
}

fun PipelineContext<*, ApplicationCall>.adminRestrict() {
    ifRegular { throw ExpiredOrInvalidTokenException() }
}

fun PipelineContext<*, ApplicationCall>.idRestrict(uuid: UUID) {
    val id = jwt["id"].asString()
    if (id != uuid.toString()) throw ExpiredOrInvalidTokenException()
}

fun ApplicationCall.getPageParameters(): Pair<Long, Int> {
    val params = request.queryParameters
    val page = (params["page"] ?: "0").let {
        val page = it.toLongOrNull() ?: throw IllegalArgumentException("Invalid page number")
        if (page < 0) throw IllegalArgumentException("errors.number.negative")
        page
    }
    val pageSize = (params["pageSize"] ?: "25").let {
        val pageSize = it.toIntOrNull() ?: throw IllegalArgumentException("Invalid page size")
        if (pageSize < 0) throw IllegalArgumentException("errors.number.negative")
        pageSize
    }
    return page to pageSize
}

fun ApplicationCall.getHeader(header: String) = request.headers[header] ?: throw MissingHeaderException(header)

suspend fun ApplicationCall.doWithForm(
    onFields: Map<String, suspend (PartData.FormItem) -> Unit> = mapOf(),
    onFiles: Map<String, suspend (PartData.FileItem) -> Unit> = mapOf(),
) = runCatching { receiveMultipart() }.onSuccess {
    it.forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                val field = part.name
                if (field in onFields) onFields[field]?.invoke(part)
            }
            is PartData.FileItem -> {
                val field = part.name
                if (field in onFiles) onFiles[field]?.invoke(part)
            }
            else -> {}
        }
        part.dispose()
    }
}.onFailure { throw MultipartParseException() }

private const val CHARSET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
fun String.Companion.random(size: Int) = (1..size)
    .map { CHARSET.random() }
    .joinToString("")
