package com.rpkit.locks.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.locks.bukkit.command.GetKeyCommand
import com.rpkit.locks.bukkit.command.KeyringCommand
import com.rpkit.locks.bukkit.command.UnlockCommand
import com.rpkit.locks.bukkit.database.table.RPKKeyringTable
import com.rpkit.locks.bukkit.database.table.RPKLockedBlockTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerGettingKeyTable
import com.rpkit.locks.bukkit.database.table.RPKPlayerUnclaimingTable
import com.rpkit.locks.bukkit.keyring.RPKKeyringProviderImpl
import com.rpkit.locks.bukkit.listener.CraftItemListener
import com.rpkit.locks.bukkit.listener.InventoryClickListener
import com.rpkit.locks.bukkit.listener.InventoryCloseListener
import com.rpkit.locks.bukkit.listener.PlayerInteractListener
import com.rpkit.locks.bukkit.lock.RPKLockProviderImpl
import org.bukkit.Material.IRON_BLOCK
import org.bukkit.Material.IRON_INGOT
import org.bukkit.inventory.ShapedRecipe

class RPKLocksBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        val lockProvider = RPKLockProviderImpl(this)
        serviceProviders = arrayOf(
                RPKKeyringProviderImpl(this),
                lockProvider
        )
        val lockRecipe = ShapedRecipe(lockProvider.lockItem)
        lockRecipe.shape("I", "B").setIngredient('I', IRON_INGOT).setIngredient('B', IRON_BLOCK)
        server.addRecipe(lockRecipe)
    }

    override fun registerCommands() {
        getCommand("getkey").executor = GetKeyCommand(this)
        getCommand("keyring").executor = KeyringCommand(this)
        getCommand("unlock").executor = UnlockCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                CraftItemListener(this),
                InventoryClickListener(this),
                InventoryCloseListener(this),
                PlayerInteractListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKKeyringTable(database, this))
        database.addTable(RPKLockedBlockTable(database, this))
        database.addTable(RPKPlayerGettingKeyTable(database, this))
        database.addTable(RPKPlayerUnclaimingTable(database, this))
    }

}