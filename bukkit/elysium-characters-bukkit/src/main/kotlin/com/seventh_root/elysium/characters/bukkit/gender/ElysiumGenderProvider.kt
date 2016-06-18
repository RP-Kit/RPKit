package com.seventh_root.elysium.characters.bukkit.gender

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.database.table.ElysiumGenderTable
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.core.service.ServiceProvider
import java.sql.SQLException
import java.util.*

class ElysiumGenderProvider(private val plugin: ElysiumCharactersBukkit): ServiceProvider {

    fun getGender(id: Int): ElysiumGender? {
        return plugin.core.database.getTable(ElysiumGender::class.java)!![id]
    }

    fun getGender(name: String): ElysiumGender? {
        val table = plugin.core.database.getTable(ElysiumGender::class.java)
        if (table is ElysiumGenderTable) {
            return table[name]
        }
        return null
    }

    val genders: Collection<ElysiumGender>
        get() {
            try {
                val genders: MutableList<ElysiumGender> = ArrayList()
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM elysium_gender").use { statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            genders.add(ElysiumGender(resultSet.getInt("id"), resultSet.getString("name")))
                        }
                    }
                }
                return genders
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            return emptyList()
        }

    fun addGender(gender: ElysiumGender) {
        plugin.core.database.getTable(ElysiumGender::class.java)!!.insert(gender)
    }

    fun removeGender(gender: ElysiumGender) {
        plugin.core.database.getTable(ElysiumGender::class.java)!!.delete(gender)
    }

}
