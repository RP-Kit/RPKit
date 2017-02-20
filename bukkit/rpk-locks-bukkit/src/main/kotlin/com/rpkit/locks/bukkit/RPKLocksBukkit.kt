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

    override fun setDefaultMessages() {
        messages.setDefault("block-locked", "&cThe \$block appears to be locked. You would need the key to get in.")
        messages.setDefault("crafting-no-keys", "&cYou may not use keys as a substitute for iron ingots.")
        messages.setDefault("keyring-invalid-item", "&cYou may not place non-key items on your keyring.")
        messages.setDefault("lock-successful", "&aBlock locked. You've been given the key. Please take good care of it.")
        messages.setDefault("lock-invalid-already-locked", "&cThat block is already locked.")
        messages.setDefault("unlock-successful", "&aBlock unlocked.")
        messages.setDefault("unlock-invalid-no-key", "&cYou must have the key to that block in order to unlock it.")
        messages.setDefault("unlock-invalid-not-locked", "&cThat block is not locked.")
        messages.setDefault("get-key-invalid-not-locked", "&cThat block is not locked.")
        messages.setDefault("get-key-successful", "&aHere is the key.")
        messages.setDefault("get-key-valid", "&aPlease interact with the block you would like the key for.")
        messages.setDefault("unlock-valid", "&aPlease interact with the block you would like to unlock.")
        messages.setDefault("not-from-console", "&cYou must be a player to perform this command.")
        messages.setDefault("no-character", "&cYou must have a character to perform this action.")
        messages.setDefault("no-permission-get-key", "&cYou do not have permission to get keys.")
        messages.setDefault("no-permission-keyring", "&cYou do not have permission to view your keyring.")
        messages.setDefault("no-permission-unlock", "&cYou do not have permission to remove locks.")
    }
    
}