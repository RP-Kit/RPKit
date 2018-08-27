package com.rpkit.blocklog.bukkit

import com.rpkit.blocklog.bukkit.block.RPKBlockHistoryProviderImpl
import com.rpkit.blocklog.bukkit.command.HistoryCommand
import com.rpkit.blocklog.bukkit.command.InventoryHistoryCommand
import com.rpkit.blocklog.bukkit.command.RollbackCommand
import com.rpkit.blocklog.bukkit.database.table.RPKBlockChangeTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockHistoryTable
import com.rpkit.blocklog.bukkit.database.table.RPKBlockInventoryChangeTable
import com.rpkit.blocklog.bukkit.listener.*
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database


class RPKBlockLoggingBukkit : RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKBlockHistoryProviderImpl(this)
        )
    }

    override fun registerCommands() {
        getCommand("history").executor = HistoryCommand(this)
        getCommand("inventoryhistory").executor = InventoryHistoryCommand(this)
        getCommand("rollback").executor = RollbackCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                BlockBreakListener(this),
                BlockBurnListener(this),
                BlockFormListener(this),
                BlockIgniteListener(this),
                BlockPistonExtendListener(this),
                BlockPistonRetractListener(this),
                BlockPlaceListener(this),
                BlockSpreadListener(this),
                EntityBlockFormListener(this),
                EntityChangeBlockListener(this),
                EntityExplodeListener(this),
                InventoryClickListener(this),
                InventoryDragListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKBlockChangeTable(database, this))
        database.addTable(RPKBlockHistoryTable(database, this))
        database.addTable(RPKBlockInventoryChangeTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("not-from-console", "&cYou may not use this command from console.")
        messages.setDefault("no-permission-history", "&cYou do not have permission to view the history of blocks.")
        messages.setDefault("history-no-target-block", "&cYou must be looking at a block to view the history of.")
        messages.setDefault("history-no-changes", "&cThat block has no recorded changes.")
        messages.setDefault("history-change", "&f\$time - \$profile/\$minecraft-profile/\$character - \$from:\$from-data to \$to:\$to-data - \$reason")
        messages.setDefault("no-permission-inventory-history", "&cYou do not have permission to view the inventory history of blocks.")
        messages.setDefault("inventory-history-no-target-block", "&cYou must be looking at a block to view the history of its inventory.")
        messages.setDefault("inventory-history-no-changes", "&cThat block has no recorded inventory changes.")
        messages.setDefault("inventory-history-change", "&f\$time - \$profile/\$minecraft-profile/\$character - \$from to \$to - \$reason")
        messages.setDefault("no-permission-rollback", "&cYou do not have permission to rollback changes.")
        messages.setDefault("rollback-usage", "&cUsage: /rollback [radius] [minutes]")
        messages.setDefault("rollback-invalid-radius", "&cRadius must be a positive integer.")
        messages.setDefault("rollback-invalid-time", "&cTime must be a positive integer.")
        messages.setDefault("rollback-valid", "&aBlocks rolled back.")
    }

}