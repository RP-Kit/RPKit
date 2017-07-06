package com.rpkit.essentials.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.essentials.bukkit.command.*
import com.rpkit.essentials.bukkit.dailyquote.RPKDailyQuoteProviderImpl
import com.rpkit.essentials.bukkit.database.table.RPKLogMessagesEnabledTable
import com.rpkit.essentials.bukkit.database.table.RPKPreviousLocationTable
import com.rpkit.essentials.bukkit.database.table.RPKTrackingEnabledTable
import com.rpkit.essentials.bukkit.kit.RPKKitImpl
import com.rpkit.essentials.bukkit.kit.RPKKitProviderImpl
import com.rpkit.essentials.bukkit.listener.PlayerJoinListener
import com.rpkit.essentials.bukkit.listener.PlayerQuitListener
import com.rpkit.essentials.bukkit.listener.PlayerTeleportListener
import com.rpkit.essentials.bukkit.locationhistory.RPKLocationHistoryProviderImpl
import com.rpkit.essentials.bukkit.logmessage.RPKLogMessageProvider
import com.rpkit.essentials.bukkit.time.TimeSlowRunnable
import com.rpkit.essentials.bukkit.tracking.RPKTrackingProviderImpl
import org.bukkit.configuration.serialization.ConfigurationSerialization


class RPKEssentialsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        ConfigurationSerialization.registerClass(RPKKitImpl::class.java)
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKDailyQuoteProviderImpl(this),
                RPKKitProviderImpl(this),
                RPKLocationHistoryProviderImpl(this),
                RPKLogMessageProvider(this),
                RPKTrackingProviderImpl(this)
        )
        TimeSlowRunnable(this).runTaskTimer(this, 100L, 100L)
    }

    override fun registerCommands() {
        getCommand("back").executor = BackCommand(this)
        getCommand("clone").executor = CloneCommand(this)
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
    }

    override fun registerListeners() {
        registerListeners(
                PlayerJoinListener(this),
                PlayerQuitListener(this),
                PlayerTeleportListener(this)
        )
    }

    override fun createTables(database: Database) {
        database.addTable(RPKLogMessagesEnabledTable(database, this))
        database.addTable(RPKPreviousLocationTable(database, this))
        database.addTable(RPKTrackingEnabledTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("back-valid", "&aTeleported to previous location.")
        messages.setDefault("back-invalid-no-locations", "&cYou have not teleported recently.")
        messages.setDefault("clone-valid", "&aItem cloned.")
        messages.setDefault("clone-invalid-item", "&cYou are not holding any item.")
        messages.setDefault("distance-invalid-untrackable", "&cThat player is not currently trackable.")
        messages.setDefault("distance-invalid-item", "&cYou require \$amount x \$type to use that command.")
        messages.setDefault("distance-invalid-distance", "&cYou are too far away to get the distance to that player.")
        messages.setDefault("distance-valid", "&aDistance to \$character/\$player: \$distance")
        messages.setDefault("distance-untrackable-notification", "&c\$player attempted to check their distance to you. If you wish for them to be able to find you, re-enable tracking with /toggletracking.")
        messages.setDefault("distance-invalid-world", "&cThat player is in a different world.")
        messages.setDefault("distance-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("distance-usage", "&cUsage: /distance [player]")
        messages.setDefault("enchant-valid", "&aSuccessfully enchanted \$amount x \$type with \$enchantment \$level")
        messages.setDefault("enchant-invalid-level", "&cThe enchantment level must be a number.")
        messages.setDefault("enchant-invalid-enchantment", "&cThere is no enchantment by that name.")
        messages.setDefault("enchant-invalid-illegal", "&cThat enchantment is illegal.")
        messages.setDefault("enchant-invalid-item", "&cYou must be holding something in order to enchant it.")
        messages.setDefault("enchant-usage", "&cUsage: /enchant [enchantment] [level]")
        messages.setDefault("feed-notification", "&aHunger refilled.")
        messages.setDefault("feed-valid", "&a\$player's hunger was refilled.")
        messages.setDefault("feed-usage-console", "&cWhen using this command from console, you must specify a player to feed.")
        messages.setDefault("fly-enable-notification", "&aFly mode enabled.")
        messages.setDefault("fly-enable-valid", "&aAllowed \$player to fly.")
        messages.setDefault("fly-disable-notification", "&aFly mode disabled.")
        messages.setDefault("fly-disable-valid", "&aDisallowed \$player to fly.")
        messages.setDefault("fly-usage-console", "&cWhen using this command from console, you must specify a player.")
        messages.setDefault("get-book-valid", "&aHere's a book.")
        messages.setDefault("get-sign-valid", "&aHere's a sign.")
        messages.setDefault("heal-notification", "&aHealed.")
        messages.setDefault("heal-valid", "&aHealed \$player.")
        messages.setDefault("heal-usage-console", "&cWhen using this command from console, you must specify a player to heal.")
        messages.setDefault("inventory-valid", "&aViewing \$player's inventory.")
        messages.setDefault("inventory-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("inventory-usage", "&cUsage: /inventory [player]")
        messages.setDefault("item-invalid-amount", "&cThe amount must be an integer.")
        messages.setDefault("item-valid-plural", "&aCreated \$amount \$types.")
        messages.setDefault("item-valid-singular", "&aCreated a \$type.")
        messages.setDefault("item-invalid-material", "&cCould not find a material by that name.")
        messages.setDefault("item-usage", "&cUsage: /item [type] [amount]")
        messages.setDefault("item-meta-set-name-valid", "&aSet item display name to \$name.")
        messages.setDefault("item-meta-add-lore-valid", "&aAdded lore: \"\$lore&a\".")
        messages.setDefault("item-meta-remove-lore-valid", "&cRemoved lore: \"\$lore&a\".")
        messages.setDefault("item-meta-remove-lore-invalid-lore-item", "&cThat item does not have that piece of lore.")
        messages.setDefault("item-meta-remove-lore-invalid-lore", "&cThat item does not have any lore.")
        messages.setDefault("item-meta-usage", "&cUsage: /itemmeta [setname|addlore|removelore] [name|lore]")
        messages.setDefault("item-meta-invalid-item", "&cYou must be holding an item to modify its item meta.")
        messages.setDefault("jump-valid", "&aTeleported to first block in line of sight.")
        messages.setDefault("jump-invalid-block", "&cNo block was found within 64 blocks of your line of sight.")
        messages.setDefault("kit-valid", "&aClaimed \$kit.")
        messages.setDefault("kit-invalid-kit", "&cThere is no kit by that name.")
        messages.setDefault("kit-list-title", "&fKit list:")
        messages.setDefault("kit-list-item", "&f- &7\$kit")
        messages.setDefault("repair-valid", "&aItem repaired.")
        messages.setDefault("repair-invalid-item", "&cYou must be holding an item in order to repair it.")
        messages.setDefault("run-as-valid", "&aDispatched command.")
        messages.setDefault("run-as-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("run-as-usage", "&cUsage: /runas [player] [command]")
        messages.setDefault("seen-online", "&a\$player is online now.")
        messages.setDefault("seen-date", "&a\$player was last seen on \$date at \$time.")
        messages.setDefault("seen-diff", "&a(That's \$days days, \$hours hours, \$minutes minutes, \$seconds seconds ago.)")
        messages.setDefault("seen-never", "&aThat player has never played on this server.")
        messages.setDefault("seen-usage", "&cUsage: /seen [player]")
        messages.setDefault("set-spawn-valid", "&aSpawn location of \$world set.")
        messages.setDefault("smite-usage", "&cUsage: /smite [player]")
        messages.setDefault("smite-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("smite-valid", "&aOpening the heavens on \$player.")
        messages.setDefault("spawn-valid", "&aTeleported to spawn.")
        messages.setDefault("spawner-valid", "&aSpawner type set.")
        messages.setDefault("spawner-invalid-entity", "&cThere is no entity by that name.")
        messages.setDefault("spawner-invalid-block", "&cThat's not a mob spawner.")
        messages.setDefault("spawner-usage", "&cUsage: /spawner [type]")
        messages.setDefault("spawn-mob-valid", "&aMobs spawned.")
        messages.setDefault("spawn-mob-invalid-amount", "&cThe amount of mobs must be an integer.")
        messages.setDefault("spawn-mob-invalid-mob", "&cThat entity type does not exist.")
        messages.setDefault("spawn-mob-usage", "&cUsage: /spawnmob [type] [amount]")
        messages.setDefault("speed-invalid-speed-number", "&cSpeed must be a number.")
        messages.setDefault("speed-reset-valid", "&aReset \$player's fly speed.")
        messages.setDefault("speed-reset-notification", "&aYour fly speed was reset by \$player.")
        messages.setDefault("speed-set-valid", "&aSet \$player's fly speed to \$speed.")
        messages.setDefault("speed-set-notification", "&aYour fly speed was set to \$speed by \$player.")
        messages.setDefault("speed-invalid-speed-bounds", "&cSpeed must be between -1.0 and 1.0")
        messages.setDefault("speed-usage-console", "&cWhen using this command from console, you must specify a player to change the fly speed of.")
        messages.setDefault("toggle-log-messages-valid", "&aLog messages \$enabled.")
        messages.setDefault("toggle-tracking-on-valid", "&aYou have allowed people to find you with /track and /distance.")
        messages.setDefault("toggle-tracking-off-valid", "&aYou have disallowed people to find you with /track and /distance.")
        messages.setDefault("track-invalid-untrackable", "&cThat player is not currently trackable.")
        messages.setDefault("track-invalid-item", "&cYou require \$amount x \$type to use that command.")
        messages.setDefault("track-invalid-distance", "&cYou are too far away to track that player.")
        messages.setDefault("track-valid", "&aNow tracking \$player/\$character.")
        messages.setDefault("track-untrackable-notification", "&c\$player attempted to track you. If you wish for them to be able to find you, re-enable tracking with /toggletracking.")
        messages.setDefault("track-invalid-player", "&cThere is no player by that name online.")
        messages.setDefault("track-usage", "&cUsage: /track [player]")
        messages.setDefault("unsign-valid", "&aBook unsigned.")
        messages.setDefault("unsign-invalid-book", "&cYou must be holding a written book to unsign.")
        messages.setDefault("no-permission-back", "&cYou do not have permission to go back.")
        messages.setDefault("no-permission-clone", "&cYou do not have permission to clone items.")
        messages.setDefault("no-permission-distance", "&cYou do not have permission to check your distance to players.")
        messages.setDefault("no-permission-enchant", "&cYou do not have permission to enchant items.")
        messages.setDefault("no-permission-feed", "&cYou do not have permission to feed players.")
        messages.setDefault("no-permission-fly", "&cYou do not have permission to toggle fly mode.")
        messages.setDefault("no-permission-get-book", "&cYou do not have permission to get books.")
        messages.setDefault("no-permission-get-sign", "&cYou do not have permission to get signs.")
        messages.setDefault("no-permission-heal", "&cYou do not have permission to heal players.")
        messages.setDefault("no-permission-inventory", "&cYou do not have permission to view other people's inventories.")
        messages.setDefault("no-permission-item", "&cYou do not have permission to spawn items.")
        messages.setDefault("no-permission-item-meta", "&cYou do not have permission to modify item meta.")
        messages.setDefault("no-permission-jump", "&cYou do not have permission to jump.")
        messages.setDefault("no-permission-kit", "&cYou do not have permission to claim kits.")
        messages.setDefault("no-permission-repair", "&cYou do not have permission to repair items.")
        messages.setDefault("no-permission-run-as", "&cYou do not have permission to run commands as other players.")
        messages.setDefault("no-permission-seen", "&cYou do not have permission to see when players were last online.")
        messages.setDefault("no-permission-set-spawn", "&cYou do not have permission to set the spawn.")
        messages.setDefault("no-permission-smite", "&cYou do not have permission to smite people.")
        messages.setDefault("no-permission-spawn", "&cYou do not have permission to teleport to spawn.")
        messages.setDefault("no-permission-spawner", "&cYou do not have permission to change spawner types.")
        messages.setDefault("no-permission-spawn-mob", "&cYou do not have permission to spawn mobs.")
        messages.setDefault("no-permission-speed", "&cYou do not have permission to set fly speed.")
        messages.setDefault("no-permission-sudo", "&c\$player is not in the sudoers file. This incident will be reported.")
        messages.setDefault("no-permission-toggle-log-messages", "&cYou do not have permission to toggle log messages.")
        messages.setDefault("no-permission-toggle-tracking", "&cYou do not have permission to toggle tracking.")
        messages.setDefault("no-permission-track", "&cYou do not have permission to track players.")
        messages.setDefault("no-permission-unsign", "&cYou do not have permission to unsign written books.")
        messages.setDefault("not-from-console", "&cYou must be a player to perform this command.")
        messages.setDefault("no-character-self", "&cYou must have a character to perform that command.")
        messages.setDefault("no-character-other", "&cThat player must have a character to perform that command.")
        messages.setDefault("no-minecraft-profile", "&cA Minecraft profile has not been created for you, or was unable to be retrieved. Please try relogging, and contact the server owner if this error persists.")
    }

}