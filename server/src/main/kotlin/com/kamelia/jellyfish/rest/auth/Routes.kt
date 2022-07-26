package com.kamelia.jellyfish.rest.auth

import com.kamelia.jellyfish.util.jwt
import com.kamelia.jellyfish.util.respond
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.authRoutes() = route("/login") {
    login()

    authenticate("auth-jwt") {
        logoutAll()
    }
    authenticate("refresh-jwt") {
        refresh()
    }
}

private fun Route.login() = post<LoginDTO> { body ->
    call.respond(AuthService.verify(body.username, body.password))
}

private fun Route.logoutAll() = delete {
    val username = jwt.subject
    call.respond(AuthService.logoutAll(username))
}

private fun Route.refresh() = patch {
    call.respond(AuthService.refresh(jwt))
}
