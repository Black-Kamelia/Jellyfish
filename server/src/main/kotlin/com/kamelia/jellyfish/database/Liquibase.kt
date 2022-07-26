package com.kamelia.jellyfish.database

import com.kamelia.jellyfish.util.Environment.liquibaseMaster
import liquibase.Contexts
import liquibase.Liquibase
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.resource.ClassLoaderResourceAccessor

fun configureLiquibase() {
    // Establish connection to the database
    val jdbcConnection = JdbcConnection(Connection.connection)
    val database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection)
    val liquibase = Liquibase(
        liquibaseMaster,
        ClassLoaderResourceAccessor(),
        database
    )

    // Run migrations
    liquibase.update(Contexts())
}
