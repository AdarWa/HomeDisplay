package net.adarw.store

import mu.KotlinLogging
import org.jetbrains.exposed.v1.jdbc.Database

object DatabaseManager{

    private val logger = KotlinLogging.logger {  }

    val instance: Database by lazy {
        logger.info { "Initializing database connection..." }
        Database.connect(
            url = "jdbc:h2:file:./data/propertystore;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "homedisplay",
            password = ""
        )
    }
}