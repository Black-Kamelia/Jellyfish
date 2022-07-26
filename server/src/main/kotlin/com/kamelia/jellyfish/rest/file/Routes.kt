package com.kamelia.jellyfish.rest.file

import com.kamelia.jellyfish.core.ExpiredOrInvalidTokenException
import com.kamelia.jellyfish.core.deleteOrCatch
import com.kamelia.jellyfish.core.getOrCatch
import com.kamelia.jellyfish.core.patchOrCatch
import com.kamelia.jellyfish.core.postOrCatch
import com.kamelia.jellyfish.rest.user.Users
import com.kamelia.jellyfish.util.adminRestrict
import com.kamelia.jellyfish.util.doWithForm
import com.kamelia.jellyfish.util.getHeader
import com.kamelia.jellyfish.util.getPageParameters
import com.kamelia.jellyfish.util.getUUID
import com.kamelia.jellyfish.util.getUUIDOrNull
import com.kamelia.jellyfish.util.jwt
import com.kamelia.jellyfish.util.receivePageDefinition
import com.kamelia.jellyfish.util.respond
import com.kamelia.jellyfish.util.uuid
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.Route
import io.ktor.server.routing.route

fun Route.filesRoutes() = route("/files") {
    uploadFileFromToken()

    authenticate("auth-jwt") {
        uploadFile()
        getPagedFiles()
        editFile()
        deleteFile()
    }
}

private fun Route.uploadFile() = postOrCatch(path = "/upload") {
    val uuid = jwt.uuid
    val user = Users.findById(uuid) ?: throw ExpiredOrInvalidTokenException()

    call.doWithForm(onFiles = mapOf(
        "file" to { call.respond(FileService.handleFile(it, user)) }
    ))
}

private fun Route.uploadFileFromToken() = postOrCatch(path = "/upload/token") {
    val authToken = call.getHeader("Upload-Token")
    val user = Users.findByUploadToken(authToken) ?: throw ExpiredOrInvalidTokenException()

    call.doWithForm(onFiles = mapOf(
        "file" to { call.respond(FileService.handleFile(it, user)) }
    ))
}

private fun Route.getPagedFiles() = getOrCatch(path = "/{uuid?}") {
    val uuid = call.getUUIDOrNull("uuid")
    val jwtId = jwt.uuid
    val userId = uuid?.apply { if (uuid != jwtId) adminRestrict() } ?: jwtId
    val user = Users.findById(userId) ?: throw ExpiredOrInvalidTokenException()
    val (page, pageSize) = call.getPageParameters()
    val definition = call.receivePageDefinition()

    call.respond(FileService.getFiles(user, page, pageSize, definition))
}

private fun Route.editFile() = patchOrCatch<FileUpdateDTO>(path = "/{uuid}") { body ->
    val fileId = call.getUUID("uuid")
    val userId = jwt.uuid

    call.respond(FileService.updateFile(fileId, userId, body))
}

private fun Route.deleteFile() = deleteOrCatch(path = "/{uuid}") {
    val fileId = call.getUUID("uuid")
    val userId = jwt.uuid

    call.respond(FileService.deleteFile(fileId, userId))
}
