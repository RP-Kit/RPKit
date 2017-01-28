package com.rpkit.locks.bukkit.keyring

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.locks.bukkit.RPKLocksBukkit
import com.rpkit.locks.bukkit.database.table.RPKKeyringTable
import org.bukkit.inventory.ItemStack


class RPKKeyringProviderImpl(private val plugin: RPKLocksBukkit): RPKKeyringProvider {
    override fun getKeyring(character: RPKCharacter): MutableList<ItemStack> {
        return plugin.core.database.getTable(RPKKeyringTable::class).get(character)?.items?.toMutableList()?:mutableListOf<ItemStack>()
    }

    override fun setKeyring(character: RPKCharacter, items: MutableList<ItemStack>) {
        val keyringTable = plugin.core.database.getTable(RPKKeyringTable::class)
        var keyring = keyringTable.get(character)
        if (keyring == null) {
            keyring = RPKKeyring(character = character, items = items)
            keyringTable.insert(keyring)
        } else {
            keyring.items.clear()
            keyring.items.addAll(items)
            keyringTable.update(keyring)
        }
    }

}