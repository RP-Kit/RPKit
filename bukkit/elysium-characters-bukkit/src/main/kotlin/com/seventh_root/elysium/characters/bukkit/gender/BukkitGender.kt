package com.seventh_root.elysium.characters.bukkit.gender

import com.seventh_root.elysium.api.character.Gender

class BukkitGender(override var id: Int, override val name: String) : Gender {

    constructor(name: String) : this(0, name) {
    }

}
