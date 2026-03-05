package net.adarw.store.property

import net.adarw.components.states.InternalState
import net.adarw.components.states.InternalStateDefinition
import net.adarw.components.states.StateDefinition
import net.adarw.components.states.StateType
import net.adarw.components.states.toStateValue
import net.adarw.store.DatabaseManager
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.upsert

object PropertyStoreManager {

    init {
        DatabaseManager.instance
        transaction {
            SchemaUtils.create(PropertyStore)
        }
    }

    /**
     * Saves or updates a property.
     */
    fun setProperty(key: String, value: Any) {
        val type = when (value) {
            is Int -> StateType.INT
            is Float -> StateType.FLOAT
            is Boolean -> StateType.BOOLEAN
            is String -> StateType.STRING
            else -> error("Unsupported data type")
        }

        transaction {
            PropertyStore.upsert {
                it[id] = key
                it[dataType] = type
                it[PropertyStore.value] = value.toString()
            }
        }
    }

    /**
     * Retrieves a property and casts it to the expected type.
     */
    fun getProperty(key: String): Any? = transaction {
            PropertyStore.select(PropertyStore.id, PropertyStore.dataType, PropertyStore.value)
                .where { PropertyStore.id eq key }
                .map {
                    val rawValue = it[PropertyStore.value]
                    when (it[PropertyStore.dataType]) {
                        StateType.INT -> rawValue.toInt()
                        StateType.FLOAT -> rawValue.toFloat()
                        StateType.BOOLEAN -> rawValue.toBoolean()
                        StateType.STRING -> rawValue
                    }
                }.singleOrNull()
        }

    /**
     * Deletes a property by its key.
     * @return true if a record was deleted, false otherwise.
     */
    fun deleteProperty(key: String) : Boolean = transaction {
            val rowsDeleted = PropertyStore.deleteWhere { id eq key }
            rowsDeleted > 0
        }

    fun getInternalState(definition: InternalStateDefinition): InternalState {
        val value = getProperty(definition.id)
        requireNotNull(value) { "State ${definition.id} was not found" }
        val stateValue = value.toStateValue()
        return InternalState(definition, stateValue)
    }

    fun setInternalState(state: InternalState) =
        setProperty(state.definition.id, state.value.data)
}