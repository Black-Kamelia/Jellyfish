package com.kamelia.jellyfish.plugins

import com.kamelia.jellyfish.rest.auth.authRoutes
import com.kamelia.jellyfish.rest.file.filesRoutes
import com.kamelia.jellyfish.rest.user.userRoutes
import com.kamelia.jellyfish.util.Environment.isDev
import com.kamelia.jellyfish.util.Environment.isProd
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.http.content.resource
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing

fun Application.configureRouting() {
    routing {
        route("/api") {
            authRoutes()
            userRoutes()
            filesRoutes()
        }

        when {
            isDev -> get("/") {
                call.respondRedirect("http://localhost:3000")
            }
            isProd -> {
                static("/") {
                    resource("/", "static/index.html")
                    resources("static")
                }
            }
        }
    }
}
