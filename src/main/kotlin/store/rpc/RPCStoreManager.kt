package net.adarw.store.rpc

import net.adarw.store.DatabaseManager
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object RPCStoreManager {

    init {
        DatabaseManager.instance
        transaction { SchemaUtils.create(RPCStore) }
    }

    fun registerRPC(serialId: String): Int = transaction {
        RPCStore.insert { it[RPCStore.serialId] = serialId } get RPCStore.id
    }

    fun serialIdExists(serialId: String): Boolean = transaction {
        RPCStore.selectAll().where { RPCStore.serialId eq serialId }.any()
    }
}
