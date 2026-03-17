package net.adarw.store.rpc

import net.adarw.store.DatabaseManager
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

object RPCStoreManager {

    init {
        DatabaseManager.instance
        transaction { SchemaUtils.create(RPCStore) }
    }

    fun registerRPC(serialId: String): Int = transaction {
        RPCStore.insert { it[RPCStore.serialId] = serialId } get RPCStore.id
    }

    fun serialIdExists(serialId: String): Int? = transaction {
        RPCStore.select(RPCStore.id)
            .where { RPCStore.serialId eq serialId }
            .singleOrNull()
            ?.get(RPCStore.id)
    }

    fun idExists(id: Int): Boolean = transaction {
        RPCStore.selectAll()
            .where { RPCStore.id eq id }
            .count() > 0
    }

    fun getRawConfig(id: Int): String = transaction {
        RPCStore.select(RPCStore.config)
            .where { RPCStore.id eq id }
            .single()[RPCStore.config]
    }

    fun setRawConfig(id: Int, config: String) {
        transaction {
            RPCStore.update({ RPCStore.id eq id }) {
                it[RPCStore.config] = config
            }
        }
    }
}
