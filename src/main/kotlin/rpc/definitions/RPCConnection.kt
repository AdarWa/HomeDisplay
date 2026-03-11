package net.adarw.rpc.definitions

import org.jetbrains.exposed.v1.core.Table

data class RPCConnection(val serialId: String, val id: Int, val lastSeen: Long)

object RPCConnections : Table("rpc_connections") {
    val serialId = varchar("serial_id", 255)
    val id = integer("id")
    val lastSeen = long("last_seen")

    override val primaryKey = PrimaryKey(serialId)
}
