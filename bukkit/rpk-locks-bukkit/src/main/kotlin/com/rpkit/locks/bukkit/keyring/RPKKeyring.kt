package com.rpkit.locks.bukkit.keyring

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.database.Entity
import org.bukkit.inventory.ItemStack


class RPKKeyring(
        override var id: Int = 0,
        val character: RPKCharacter,
        val items: MutableList<ItemStack>
) : Entity