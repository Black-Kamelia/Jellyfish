package com.kamelia.jellyfish.rest.file

import java.nio.file.Files as NIOFiles
import com.kamelia.jellyfish.core.UploadCodeGenerationException
import com.kamelia.jellyfish.rest.core.pageable.PageDefinitionDTO
import com.kamelia.jellyfish.rest.core.pageable.PageDTO
import com.kamelia.jellyfish.rest.user.User
import com.kamelia.jellyfish.rest.user.UserRole
import com.kamelia.jellyfish.rest.user.Users
import com.kamelia.jellyfish.util.ErrorDTO
import com.kamelia.jellyfish.util.QueryResult
import com.kamelia.jellyfish.util.random
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import java.net.URLConnection
import java.nio.file.Path
import java.util.UUID
import kotlin.math.ceil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction

object FileService {

    private val UPLOAD_PATH = Path.of("upload")

    init {
        NIOFiles.createDirectories(UPLOAD_PATH)
    }

    suspend fun handleFile(part: PartData.FileItem, creator: User): QueryResult<FileRepresentationDTO, List<ErrorDTO>> {
        val fileName = part.originalFileName!!
        val fileCode = generateUniqueCode()// + "." + Path.of(fileName).extension
        val fileBytes = part.streamProvider().readBytes()

        if (fileName.isBlank()) return QueryResult.badRequest("errors.file.name.empty")

        return withContext(Dispatchers.IO) {
            val directory = UPLOAD_PATH.resolve(creator.id.toString())
            NIOFiles.createDirectories(directory)
            val filePath = directory.resolve(fileCode)
            NIOFiles.write(filePath, fileBytes)
            val fileMimeType = URLConnection.guessContentTypeFromName(fileName) ?: "application/octet-stream"
            val fileSize = NIOFiles.size(filePath)

            QueryResult.ok(
                Files.create(
                    code = fileCode,
                    name = fileName,
                    mimeType = fileMimeType,
                    size = fileSize,
                    creator = creator
                ).toRepresentationDTO()
            )
        }
    }

    private suspend fun generateUniqueCode(): String {
        repeat(10) {
            val code = String.random(10)
            if (Files.findByCode(code) == null) {
                return code
            }
        }
        throw UploadCodeGenerationException()
    }

    suspend fun getFiles(user: User, page: Long, pageSize: Int, definition: PageDefinitionDTO): QueryResult<FilePageDTO, List<ErrorDTO>> {
        val (files, total) = user.getFiles(page, pageSize, definition)
        return QueryResult.ok(FilePageDTO(
            PageDTO(
                files.map { it.toRepresentationDTO() },
                page,
                pageSize,
                ceil(total / pageSize.toDouble()).toLong(),
                total
            )
        ))
    }

    suspend fun updateFile(
        fileId: UUID,
        userId: UUID,
        dto: FileUpdateDTO,
    ): QueryResult<FileRepresentationDTO, List<ErrorDTO>> {
        val user = Users.findById(userId) ?: return QueryResult.unauthorized()
        val file = Files.findById(fileId) ?: return QueryResult.notFound()

        val owner = transaction { file.owner }
        if (user.role eq UserRole.REGULAR && owner.id != user.id) {
            return QueryResult.forbidden("errors.files.edition.forbidden")
        }

        return QueryResult.ok(Files.update(file, dto, user).toRepresentationDTO())
    }

    suspend fun deleteFile(
        fileId: UUID,
        userId: UUID,
    ): QueryResult<FileRepresentationDTO, List<ErrorDTO>> {
        val user = Users.findById(userId) ?: return QueryResult.unauthorized()
        val file = Files.findById(fileId) ?: return QueryResult.notFound()

        val owner = transaction { file.owner }
        if (user.role eq UserRole.REGULAR && owner.id != user.id) {
            return QueryResult.forbidden("errors.files.deletion.forbidden")
        }

        return QueryResult.ok(Files.delete(fileId)?.toRepresentationDTO())
    }
}
