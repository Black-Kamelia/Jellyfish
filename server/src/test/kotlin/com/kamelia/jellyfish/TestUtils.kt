package com.kamelia.jellyfish

import com.kamelia.jellyfish.core.TokenPair
import com.kamelia.jellyfish.rest.auth.LoginDTO
import com.kamelia.jellyfish.rest.core.DTO
import com.kamelia.jellyfish.rest.user.UserRepresentationDTO
import com.kamelia.jellyfish.util.UUIDSerializer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.ApplicationTestBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

fun ApplicationTestBuilder.client() = createClient {
    install(ContentNegotiation) {
        json(Json {
            serializersModule = SerializersModule {
                polymorphic(DTO::class) {
                    subclass(UserRepresentationDTO::class)
                }
                contextual(UUIDSerializer)
            }
        })
    }
}

suspend fun ApplicationTestBuilder.login(
    username: String,
    password: String,
): Pair<HttpStatusCode, TokenPair?> {
    val dto = LoginDTO(username, password)
    val response = client().post("/api/login") {
        contentType(ContentType.Application.Json)
        setBody(dto)
    }
    val body = if (response.status == HttpStatusCode.OK) {
        Json.decodeFromString(TokenPair.serializer(), response.bodyAsText())
    } else {
        System.err.println(response.bodyAsText())
        null
    }
    return response.status to body
}
