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

package com.seventh_root.elysium.characters.bukkit.gender

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.database.table.ElysiumGenderTable
import com.seventh_root.elysium.core.database.use
import java.sql.SQLException
import java.util.*

class ElysiumGenderProviderImpl(private val plugin: ElysiumCharactersBukkit): ElysiumGenderProvider {

    override fun getGender(id: Int): ElysiumGender? {
        return plugin.core.database.getTable(ElysiumGender::class.java)!![id]
    }

    override fun getGender(name: String): ElysiumGender? {
        val table = plugin.core.database.getTable(ElysiumGender::class.java)
        if (table is ElysiumGenderTable) {
            return table[name]
        }
        return null
    }

    override val genders: Collection<ElysiumGender>
        get() {
            try {
                val genders: MutableList<ElysiumGender> = ArrayList()
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM elysium_gender").use { statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            genders.add(ElysiumGenderImpl(resultSet.getInt("id"), resultSet.getString("name")))
                        }
                    }
                }
                return genders
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            return emptyList()
        }

    override fun addGender(gender: ElysiumGender) {
        plugin.core.database.getTable(ElysiumGender::class.java)!!.insert(gender)
    }

    override fun removeGender(gender: ElysiumGender) {
        plugin.core.database.getTable(ElysiumGender::class.java)!!.delete(gender)
    }

}
