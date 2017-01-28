package com.rpkit.locks.bukkit.keyring

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.service.ServiceProvider
import org.bukkit.inventory.ItemStack


interface RPKKeyringProvider: ServiceProvider {

    fun getKeyring(character: RPKCharacter): MutableList<ItemStack>
    fun setKeyring(character: RPKCharacter, items: MutableList<ItemStack>)

}