package com.kamelia.jellyfish.rest.user

import com.kamelia.jellyfish.core.deleteOrCatch
import com.kamelia.jellyfish.core.getOrCatch
import com.kamelia.jellyfish.core.patchOrCatch
import com.kamelia.jellyfish.core.postOrCatch
import com.kamelia.jellyfish.core.putOrCatch
import com.kamelia.jellyfish.util.adminRestrict
import com.kamelia.jellyfish.util.getPageParameters
import com.kamelia.jellyfish.util.getUUID
import com.kamelia.jellyfish.util.idRestrict
import com.kamelia.jellyfish.util.ifRegular
import com.kamelia.jellyfish.util.jwt
import com.kamelia.jellyfish.util.receivePageDefinition
import com.kamelia.jellyfish.util.respond
import com.kamelia.jellyfish.util.uuid
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route


fun Route.userRoutes() = route("/users") {
    signup()

    authenticate("auth-jwt") {
        getUserById()
        getAllUsers()
        getPagedUsers()
        updateUser()
        updateUserPassword()
        deleteUser()
        regenerateUploadToken()
    }
}

private fun Route.signup() = postOrCatch<UserDTO>(path = "/signup") { body ->
    call.respond(UserService.signup(body))
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
    val (page, pageSize) = call.getPageParameters()
    val definition = call.receivePageDefinition()

    call.respond(UserService.getUsers(page, pageSize, definition))
}

private fun Route.updateUser() = patchOrCatch<UserUpdateDTO>(path = "/{uuid}") { body ->
    val uuid = call.getUUID()
    ifRegular {
        idRestrict(uuid)
    }
    val updaterID = jwt.uuid

    call.respond(UserService.updateUser(uuid, body, updaterID))
}

private fun Route.updateUserPassword() = patchOrCatch<UserPasswordUpdateDTO>(path = "/{uuid}/password") { body ->
    val uuid = call.getUUID()
    idRestrict(uuid)

    call.respond(UserService.updateUserPassword(uuid, body, uuid))
}

private fun Route.deleteUser() = deleteOrCatch(path = "/{uuid}") {
    val uuid = call.getUUID()
    ifRegular { idRestrict(uuid) }

    call.respond(UserService.deleteUser(uuid))
}

private fun Route.regenerateUploadToken() = putOrCatch(path = "/uploadToken") {
    val uuid = jwt.uuid
    idRestrict(uuid)

    call.respond(UserService.regenerateUploadToken(uuid))
}
