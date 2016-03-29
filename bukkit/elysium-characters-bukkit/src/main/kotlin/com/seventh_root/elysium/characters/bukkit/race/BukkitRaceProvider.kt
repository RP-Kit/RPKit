package com.seventh_root.elysium.characters.bukkit.race

import com.seventh_root.elysium.api.character.RaceProvider
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitRaceTable
import com.seventh_root.elysium.core.database.use
import java.sql.SQLException
import java.util.*

class BukkitRaceProvider(private val plugin: ElysiumCharactersBukkit) : RaceProvider<BukkitRace> {

    override fun getRace(id: Int): BukkitRace? {
        return plugin.core!!.database.getTable(BukkitRace::class.java)!![id]
    }

    override fun getRace(name: String): BukkitRace? {
        val table = plugin.core!!.database.getTable(BukkitRace::class.java)
        if (table is BukkitRaceTable) {
            return table[name]
        }
        return null
    }

    override val races: Collection<BukkitRace>
        get() {
            try {
                var races: MutableList<BukkitRace> = ArrayList()
                plugin.core!!.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM bukkit_race").use({ statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            races.add(BukkitRace(resultSet.getInt("id"), resultSet.getString("name")))
                        }
                    })
                }
                return races
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            return emptyList()
        }

    override fun addRace(race: BukkitRace) {
        plugin.core!!.database.getTable(BukkitRace::class.java)!!.insert(race)
    }

    override fun removeRace(race: BukkitRace) {
        plugin.core!!.database.getTable(BukkitRace::class.java)!!.delete(race)
    }

}
