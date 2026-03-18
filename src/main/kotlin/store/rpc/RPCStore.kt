package net.adarw.store.rpc

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.javatime.timestamp
import java.time.Instant

object RPCStore : Table("rpc_store") {
    val id = integer("id").autoIncrement()
    val serialId = text("serialId").uniqueIndex()
    val name = varchar("name", 255)
    val status = enumerationByName("status", 20, Status::class).default(Status.OFFLINE)
    val lastSeen = timestamp("last_seen").default(Instant.now())
    val config = text("config")

    override val primaryKey = PrimaryKey(id)
}

enum class Status {
    ONLINE, OFFLINE
}
