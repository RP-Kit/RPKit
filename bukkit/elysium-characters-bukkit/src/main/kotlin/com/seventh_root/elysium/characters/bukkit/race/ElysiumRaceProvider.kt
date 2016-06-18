package com.seventh_root.elysium.characters.bukkit.race

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.database.table.ElysiumRaceTable
import com.seventh_root.elysium.core.database.use
import com.seventh_root.elysium.core.service.ServiceProvider
import java.sql.SQLException
import java.util.*

class ElysiumRaceProvider(private val plugin: ElysiumCharactersBukkit): ServiceProvider {

    fun getRace(id: Int): ElysiumRace? {
        return plugin.core.database.getTable(ElysiumRace::class.java)!![id]
    }

    fun getRace(name: String): ElysiumRace? {
        val table = plugin.core.database.getTable(ElysiumRace::class.java)
        if (table is ElysiumRaceTable) {
            return table[name]
        }
        return null
    }

    val races: Collection<ElysiumRace>
        get() {
            try {
                val races: MutableList<ElysiumRace> = ArrayList()
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM elysium_race").use { statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            races.add(ElysiumRace(resultSet.getInt("id"), resultSet.getString("name")))
                        }
                    }
                }
                return races
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            return emptyList()
        }

    fun addRace(race: ElysiumRace) {
        plugin.core.database.getTable(ElysiumRace::class.java)!!.insert(race)
    }

    fun removeRace(race: ElysiumRace) {
        plugin.core.database.getTable(ElysiumRace::class.java)!!.delete(race)
    }

}
