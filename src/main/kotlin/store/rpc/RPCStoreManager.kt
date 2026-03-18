package net.adarw.store.rpc

import java.time.Instant as JavaInstant
import kotlin.time.Instant
import kotlin.time.toKotlinInstant
import kotlinx.serialization.Serializable
import net.adarw.store.DatabaseManager
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.*
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

@Serializable
data class RpcNode(
    val id: Int,
    val serialId: String,
    val name: String,
    val status: Status,
    val lastSeen: Instant,
    val config: String,
)

object RPCStoreManager {

    init {
        DatabaseManager.instance
        transaction { SchemaUtils.create(RPCStore) }
    }

    private fun ResultRow.toRpcNode() =
        RpcNode(
            id = this[RPCStore.id],
            serialId = this[RPCStore.serialId],
            name = this[RPCStore.name],
            status = this[RPCStore.status],
            lastSeen = this[RPCStore.lastSeen].toKotlinInstant(),
            config = this[RPCStore.config],
        )

    fun registerRPC(
        serialId: String,
        name: String = "Unnamed Node",
        config: String = "",
    ): Int = transaction {
        RPCStore.insert {
            it[RPCStore.serialId] = serialId
            it[RPCStore.name] = name
            it[RPCStore.config] = config
        } get RPCStore.id
    }

    fun getAllNodes(): List<RpcNode> = transaction {
        RPCStore.selectAll().map { it.toRpcNode() }
    }

    fun getNodeById(id: Int): RpcNode? = transaction {
        RPCStore.selectAll()
            .where { RPCStore.id eq id }
            .singleOrNull()
            ?.toRpcNode()
    }

    fun getNodeBySerial(serialId: String): RpcNode? = transaction {
        RPCStore.selectAll()
            .where { RPCStore.serialId eq serialId }
            .singleOrNull()
            ?.toRpcNode()
    }

    fun serialIdExists(serialId: String): Int? = transaction {
        RPCStore.select(RPCStore.id)
            .where { RPCStore.serialId eq serialId }
            .singleOrNull()
            ?.get(RPCStore.id)
    }

    fun idExists(id: Int): Boolean = transaction {
        RPCStore.selectAll().where { RPCStore.id eq id }.count() > 0
    }

    fun getRawConfig(id: Int): String? = transaction {
        RPCStore.select(RPCStore.config)
            .where { RPCStore.id eq id }
            .singleOrNull()
            ?.get(RPCStore.config)
    }

    fun setRawConfig(id: Int, config: String): Boolean = transaction {
        RPCStore.update({ RPCStore.id eq id }) {
            it[RPCStore.config] = config
        } > 0
    }

    fun setName(id: Int, newName: String): Boolean = transaction {
        RPCStore.update({ RPCStore.id eq id }) { it[RPCStore.name] = newName } >
            0
    }

    fun setStatus(id: Int, newStatus: Status): Boolean = transaction {
        RPCStore.update({ RPCStore.id eq id }) {
            it[RPCStore.status] = newStatus
        } > 0
    }

    fun updateHeartbeat(id: Int): Boolean = transaction {
        RPCStore.update({ RPCStore.id eq id }) {
            it[RPCStore.lastSeen] = JavaInstant.now()
            it[RPCStore.status] = Status.ONLINE
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        RPCStore.deleteWhere { RPCStore.id eq id } > 0
    }
}
