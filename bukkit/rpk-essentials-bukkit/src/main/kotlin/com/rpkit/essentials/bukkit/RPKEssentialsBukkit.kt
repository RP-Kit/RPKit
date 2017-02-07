package com.rpkit.essentials.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.essentials.bukkit.command.*
import com.rpkit.essentials.bukkit.dailyquote.RPKDailyQuoteProviderImpl
import com.rpkit.essentials.bukkit.database.table.*
import com.rpkit.essentials.bukkit.drink.RPKDrinkProviderImpl
import com.rpkit.essentials.bukkit.kit.RPKKitImpl
import com.rpkit.essentials.bukkit.kit.RPKKitProviderImpl
import com.rpkit.essentials.bukkit.listener.PlayerJoinListener
import com.rpkit.essentials.bukkit.listener.PlayerQuitListener
import com.rpkit.essentials.bukkit.listener.PlayerTeleportListener
import com.rpkit.essentials.bukkit.locationhistory.RPKLocationHistoryProviderImpl
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.essentials.bukkit.time.TimeSlowRunnable
import com.rpkit.essentials.bukkit.tracking.RPKTrackingProviderImpl
import com.rpkit.essentials.bukkit.warp.RPKWarpProviderImpl
import org.bukkit.configuration.serialization.ConfigurationSerialization


class RPKEssentialsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        ConfigurationSerialization.registerClass(RPKKitImpl::class.java)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKDailyQuoteProviderImpl(this),
                RPKDrinkProviderImpl(this),
                RPKKitProviderImpl(this),
                RPKLocationHistoryProviderImpl(this),
                RPKLogMessageProvider(this),
                RPKTrackingProviderImpl(this),
                RPKWarpProviderImpl(this)
        )
        TimeSlowRunnable(this).runTaskTimer(this, 100L, 100L)
    }

    override fun registerCommands() {
        getCommand("back").executor = BackCommand(this)
        getCommand("clone").executor = CloneCommand(this)
        getCommand("deletewarp").executor = DeleteWarpCommand(this)
        getCommand("distance").executor = DistanceCommand(this)
        getCommand("enchant").executor = EnchantCommand(this)
        getCommand("feed").executor = FeedCommand(this)
        getCommand("fly").executor = FlyCommand(this)
        getCommand("getbook").executor = GetBookCommand(this)
        getCommand("getsign").executor = GetSignCommand(this)
        getCommand("heal").executor = HealCommand(this)
        getCommand("inventory").executor = InventoryCommand(this)
        getCommand("item").executor = ItemCommand(this)
        getCommand("itemmeta").executor = ItemMetaCommand(this)
        getCommand("jump").executor = JumpCommand(this)
        getCommand("kit").executor = KitCommand(this)
        getCommand("repair").executor = RepairCommand(this)
        getCommand("runas").executor = RunAsCommand(this)
        getCommand("seen").executor = SeenCommand(this)
        getCommand("setspawn").executor = SetSpawnCommand(this)
        getCommand("setwarp").executor = SetWarpCommand(this)
        getCommand("smite").executor = SmiteCommand(this)
        getCommand("spawn").executor = SpawnCommand(this)
        getCommand("spawner").executor = SpawnerCommand(this)
        getCommand("spawnmob").executor = SpawnMobCommand(this)
        getCommand("speed").executor = SpeedCommand(this)
        getCommand("sudo").executor = SudoCommand(this)
        getCommand("togglelogmessages").executor = ToggleLogMessagesCommand(this)
        getCommand("toggletracking").executor = ToggleTrackingCommand(this)
        getCommand("track").executor = TrackCommand(this)
        getCommand("unsign").executor = UnsignCommand(this)
        getCommand("warp").executor = WarpCommand(this)
    }

    override fun registerListeners() {
        registerListeners(
                PlayerJoinListener(this),
                PlayerQuitListener(this),
                PlayerTeleportListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKDrunkennessTable(database, this))
        database.addTable(RPKLogMessagesEnabledTable(database, this))
        database.addTable(RPKPreviousLocationTable(database, this))
        database.addTable(RPKTrackingEnabledTable(database, this))
        database.addTable(RPKWarpTable(database, this))
    }

}