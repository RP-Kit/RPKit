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

package com.rpkit.characters.bukkit.gender

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.database.table.RPKGenderTable
import com.rpkit.core.database.use
import java.sql.SQLException
import java.util.*

/**
 * Gender provider implementation.
 */
class RPKGenderProviderImpl(private val plugin: RPKCharactersBukkit): RPKGenderProvider {

    override fun getGender(id: Int): RPKGender? {
        return plugin.core.database.getTable(RPKGenderTable::class)[id]
    }

    override fun getGender(name: String): RPKGender? {
        val table = plugin.core.database.getTable(RPKGenderTable::class)
        if (table is RPKGenderTable) {
            return table[name]
        }
        return null
    }

    override val genders: Collection<RPKGender>
        get() {
            try {
                val genders: MutableList<RPKGender> = ArrayList()
                plugin.core.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM rpkit_gender").use { statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            genders.add(RPKGenderImpl(resultSet.getInt("id"), resultSet.getString("name")))
                        }
                    }
                }
                return genders
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            return emptyList()
        }

    override fun addGender(gender: RPKGender) {
        plugin.core.database.getTable(RPKGenderTable::class).insert(gender)
    }

    override fun removeGender(gender: RPKGender) {
        plugin.core.database.getTable(RPKGenderTable::class).delete(gender)
    }

}
