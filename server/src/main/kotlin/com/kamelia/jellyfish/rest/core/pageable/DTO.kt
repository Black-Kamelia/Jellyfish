@file:UseSerializers(UUIDSerializer::class)

package com.kamelia.jellyfish.rest.core.pageable

import com.kamelia.jellyfish.rest.core.DTO
import com.kamelia.jellyfish.util.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers

@Serializable
class PageDTO<T : DTO>(
    val items: List<T>,
    val page: Long,
    val pageSize: Int,
    val totalPages: Long,
    val totalItems: Long,
) : DTO

@Serializable
data class PageDefinitionDTO(
    val filters: FilterDefinitionDTO = listOf(),
    val sorter: SorterDefinitionDTO = listOf(),
) : DTO

typealias FilterDefinitionDTO = List<FilterGroupDTO>

typealias FilterGroupDTO = List<FilterObject>

@Serializable
data class FilterObject(
    val field: String,
    val operator: String,
    val value: String,
    val type: FilterType = FilterType.POS,
) : DTO

typealias SorterDefinitionDTO = List<SortObject>

@Serializable
data class SortObject(
    val field: String,
    val direction: SortDirection = SortDirection.ASC,
) : DTO

enum class FilterType { POS, NEG }
enum class SortDirection { ASC, DES }
