package com.seventh_root.elysium.api.character

import com.seventh_root.elysium.api.player.ElysiumPlayer
import com.seventh_root.elysium.core.database.TableRow

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
