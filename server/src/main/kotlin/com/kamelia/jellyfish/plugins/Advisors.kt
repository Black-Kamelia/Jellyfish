package com.kamelia.jellyfish.plugins

import com.kamelia.jellyfish.core.ExpiredOrInvalidTokenException
import com.kamelia.jellyfish.core.IllegalFilterException
import com.kamelia.jellyfish.core.InvalidUUIDException
import com.kamelia.jellyfish.core.MissingHeaderException
import com.kamelia.jellyfish.core.MissingParameterException
import com.kamelia.jellyfish.core.MultipartParseException
import com.kamelia.jellyfish.util.QueryResult
import com.kamelia.jellyfish.util.respond
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.statuspages.StatusPages
import kotlinx.serialization.SerializationException

fun Application.configureExceptionAdvisors() {
    install(StatusPages) {
        exception(::handleException)
    }
}


private suspend fun handleException(call: ApplicationCall, cause: Throwable) = when (cause) {
    is MissingParameterException,
    is MissingHeaderException,
    is SerializationException,
    is IllegalArgumentException,
    is InvalidUUIDException,
    is MultipartParseException,
    is IllegalFilterException -> badRequestMessage(call, cause)

    is ExpiredOrInvalidTokenException -> unauthorizedMessage(call, cause)

    else -> unhandledError(call, cause)
}

private suspend fun badRequestMessage(call: ApplicationCall, cause: Throwable) =
    call.respond(QueryResult.badRequest(cause.message ?: cause.javaClass.name))

private suspend fun unauthorizedMessage(call: ApplicationCall, cause: Throwable) =
    call.respond(QueryResult.unauthorized(cause.message ?: cause.javaClass.name))

private suspend fun unhandledError(call: ApplicationCall, cause: Throwable) {
    call.respond(QueryResult.error(HttpStatusCode.InternalServerError, listOf("errors.unknown")))
    call.application.log.error("Unexpected error", cause)
}
