package com.seventh_root.elysium.characters.bukkit.race

import com.seventh_root.elysium.characters.bukkit.race.Race

class BukkitRace : Race {

    override var id: Int = 0
    override val name: String

    constructor(id: Int, name: String) {
        this.id = id
        this.name = name
    }

    constructor(name: String) {
        this.name = name
    }

}
