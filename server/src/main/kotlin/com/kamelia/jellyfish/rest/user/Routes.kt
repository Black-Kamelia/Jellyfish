package com.kamelia.jellyfish.rest.user

import com.kamelia.jellyfish.core.deleteOrCatch
import com.kamelia.jellyfish.core.getOrCatch
import com.kamelia.jellyfish.core.patchOrCatch
import com.kamelia.jellyfish.core.postOrCatch
import com.kamelia.jellyfish.util.adminRestrict
import com.kamelia.jellyfish.util.getUUID
import com.kamelia.jellyfish.util.idRestrict
import com.kamelia.jellyfish.util.ifRegular
import com.kamelia.jellyfish.util.respond
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route


fun Route.userRoutes() = route("/users") {
    signup()
    login()

    authenticate("auth-jwt") {
        getUserById()
        getAllUsers()
        getPagedUsers()
        updateUser()
        updateUserPassword()
        deleteUser()
    }
}

private fun Route.signup() = postOrCatch<UserDTO> { body ->
    call.respond(UserService.signup(body))
}

private fun Route.login() = postOrCatch<UserLoginDTO> { body ->
    call.respond(UserService.verify(body.username, body.password))
}

private fun Route.getUserById() = getOrCatch(path = "/{uuid}") {
    val uuid = call.getUUID()
    ifRegular { idRestrict(uuid) }
    call.respond(UserService.getUserById(uuid))
}

private fun Route.getAllUsers() = getOrCatch(path = "/all") {
    adminRestrict()
    call.respond(UserService.getUsers())
}

private fun Route.getPagedUsers() = getOrCatch {
    adminRestrict()
    val params = call.request.queryParameters
    val page = (params["page"] ?: "0").let {
        it.toLongOrNull() ?: throw IllegalArgumentException("Invalid page number")
    }
    val pageSize = (params["pageSize"] ?: "25").let {
        it.toIntOrNull() ?: throw IllegalArgumentException("Invalid page size")
    }
    call.respond(UserService.getUsers(page, pageSize))
}

private fun Route.updateUser() = patchOrCatch<UserUpdateDTO>(path = "/{uuid}") { body ->
    val uuid = call.getUUID()
    ifRegular { idRestrict(uuid) }
    call.respond(UserService.updateUser(uuid, body))
}

private fun Route.updateUserPassword() = patchOrCatch<UserPasswordUpdateDTO>(path = "/{uuid}/password") { body ->
    val uuid = call.getUUID()
    idRestrict(uuid)
    call.respond(UserService.updateUserPassword(uuid, body))
}

private fun Route.deleteUser() = deleteOrCatch(path = "/{uuid}") {
    val uuid = call.getUUID()
    ifRegular { idRestrict(uuid) }
    call.respond(UserService.deleteUser(uuid))
}
