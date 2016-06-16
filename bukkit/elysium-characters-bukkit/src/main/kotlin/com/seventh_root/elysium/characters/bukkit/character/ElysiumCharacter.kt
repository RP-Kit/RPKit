package com.seventh_root.elysium.characters.bukkit.character

import com.seventh_root.elysium.characters.bukkit.gender.Gender
import com.seventh_root.elysium.characters.bukkit.race.Race
import com.seventh_root.elysium.core.database.TableRow
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayer

interface ElysiumCharacter : TableRow {

    var player: ElysiumPlayer?
    var name: String
    var gender: Gender?
    var age: Int
    var race: Race?
    var description: String
    var isDead: Boolean
    var health: Double
    var maxHealth: Double
    var mana: Int
    var maxMana: Int
    var foodLevel: Int
    var thirstLevel: Int

}
