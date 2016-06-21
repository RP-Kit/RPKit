/*
 * Copyright 2016 Ross Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
