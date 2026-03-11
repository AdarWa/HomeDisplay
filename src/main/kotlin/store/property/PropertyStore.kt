package net.adarw.store.property

import net.adarw.components.states.StateType
import org.jetbrains.exposed.v1.core.Table

object PropertyStore : Table("property_store") {
    val id = varchar("id", 255)
    val dataType = enumerationByName("data_type", 20, StateType::class)
    val value = text("value")

    override val primaryKey = PrimaryKey(id)
}
