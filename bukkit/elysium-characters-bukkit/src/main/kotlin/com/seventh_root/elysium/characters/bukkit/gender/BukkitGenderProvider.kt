package com.seventh_root.elysium.characters.bukkit.gender

import com.seventh_root.elysium.api.character.GenderProvider
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit
import com.seventh_root.elysium.characters.bukkit.database.table.BukkitGenderTable
import com.seventh_root.elysium.core.database.use
import java.sql.SQLException
import java.util.*

class BukkitGenderProvider(private val plugin: ElysiumCharactersBukkit) : GenderProvider<BukkitGender> {

    override fun getGender(id: Int): BukkitGender? {
        return plugin.core!!.database.getTable(BukkitGender::class.java)!![id]
    }

    override fun getGender(name: String): BukkitGender? {
        val table = plugin.core!!.database.getTable(BukkitGender::class.java)
        if (table is BukkitGenderTable) {
            return table[name]
        }
        return null
    }

    override val genders: Collection<BukkitGender>
        get() {
            try {
                var genders: MutableList<BukkitGender> = ArrayList()
                plugin.core!!.database.createConnection().use { connection ->
                    connection.prepareStatement(
                            "SELECT id, name FROM bukkit_gender").use({ statement ->
                        val resultSet = statement.executeQuery()
                        while (resultSet.next()) {
                            genders.add(BukkitGender(resultSet.getInt("id"), resultSet.getString("name")))
                        }
                    })
                }
                return genders
            } catch (exception: SQLException) {
                exception.printStackTrace()
            }
            return emptyList()
        }

    override fun addGender(gender: BukkitGender) {
        plugin.core!!.database.getTable(BukkitGender::class.java)!!.insert(gender)
    }

    override fun removeGender(gender: BukkitGender) {
        plugin.core!!.database.getTable(BukkitGender::class.java)!!.delete(gender)
    }

}
