package com.kamelia.jellyfish.rest.core.pageable

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.SortOrder

private fun SortDirection.toSortOrder(): SortOrder = when (this) {
    SortDirection.ASC -> SortOrder.ASC
    SortDirection.DES -> SortOrder.DESC
}

fun Query.applySort(sorters: SorterDefinitionDTO, mapper: (String) -> Column<*>): Query = apply {
    sorters.forEach { (field, direction) ->
        orderBy(mapper(field), direction.toSortOrder())
    }
}
