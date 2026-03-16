package net.adarw.store.rpc

import org.jetbrains.exposed.v1.core.Table

object RPCStore : Table("rpc_store") {
    val id = integer("id").autoIncrement()
    val serialId = text("serialId").uniqueIndex()

    override val primaryKey = PrimaryKey(id)
}
