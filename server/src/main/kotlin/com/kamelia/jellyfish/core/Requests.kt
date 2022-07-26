package com.kamelia.jellyfish.core

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor


inline fun <reified T : Any> Route.request(
    method: Route.(String, PipelineInterceptor<Unit, ApplicationCall>) -> Route,
    path: String,
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
): Route = method(path) {
    val body = call.receive<T>()
    block(body)
}

inline fun <reified T : Any> Route.get(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
) = request(
    Route::get,
    path = path,
    block = block
)

inline fun <reified T : Any> Route.post(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
) = request(
    Route::post,
    path = path,
    block = block
)

inline fun <reified T : Any> Route.put(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
) = request(
    Route::put,
    path = path,
    block = block
)

inline fun <reified T : Any> Route.delete(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
) = request(
    Route::delete,
    path = path,
    block = block
)

inline fun <reified T : Any> Route.patch(
    path: String = "",
    crossinline block: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit,
) = request(
    Route::patch,
    path = path,
    block = block
)
