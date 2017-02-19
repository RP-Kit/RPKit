package com.rpkit.essentials.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.essentials.bukkit.command.*
import com.rpkit.essentials.bukkit.dailyquote.RPKDailyQuoteProviderImpl
import com.rpkit.essentials.bukkit.database.table.RPKDrunkennessTable
import com.rpkit.essentials.bukkit.database.table.RPKLogMessagesEnabledTable
import com.rpkit.essentials.bukkit.database.table.RPKPreviousLocationTable
import com.rpkit.essentials.bukkit.database.table.RPKTrackingEnabledTable
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
        database.addTable(RPKDrunkennessTable(database, this))
        database.addTable(RPKLogMessagesEnabledTable(database, this))
        database.addTable(RPKPreviousLocationTable(database, this))
        database.addTable(RPKTrackingEnabledTable(database, this))
    }

    override fun setDefaultMessages() {
        core.messages.setDefault("back-valid", "&aTeleported to previous location.")
        core.messages.setDefault("back-invalid-no-locations", "&cYou have not teleported recently.")
        core.messages.setDefault("clone-valid", "&aItem cloned.")
        core.messages.setDefault("clone-invalid-item", "&cYou are not holding any item.")
        core.messages.setDefault("delete-warp-valid", "&aDeleted warp \$warp.")
        core.messages.setDefault("delete-warp-usage", "&cUsage: /deletewarp [warp]")
        core.messages.setDefault("distance-invalid-untrackable", "&cThat player is not currently trackable.")
        core.messages.setDefault("distance-invalid-item", "&cYou require \$amount x \$type to use that command.")
        core.messages.setDefault("distance-invalid-distance", "&cYou are too far away to get the distance to that player.")
        core.messages.setDefault("distance-valid", "&aDistance to \$character/\$player: \$distance")
        core.messages.setDefault("distance-untrackable-notification", "&c\$player attempted to check their distance to you. If you wish for them to be able to find you, re-enable tracking with /toggletracking.")
        core.messages.setDefault("distance-invalid-world", "&cThat player is in a different world.")
        core.messages.setDefault("distance-invalid-player", "&cThere is no player by that name online.")
        core.messages.setDefault("distance-usage", "&cUsage: /distance [player]")
        core.messages.setDefault("enchant-valid", "&aSuccessfully enchanted \$amount x \$type with \$enchantment \$level")
        core.messages.setDefault("enchant-invalid-level", "&cThe enchantment level must be a number.")
        core.messages.setDefault("enchant-invalid-enchantment", "&cThere is no enchantment by that name.")
        core.messages.setDefault("enchant-invalid-illegal", "&cThat enchantment is illegal.")
        core.messages.setDefault("enchant-invalid-item", "&cYou must be holding something in order to enchant it.")
        core.messages.setDefault("enchant-usage", "&cUsage: /enchant [enchantment] [level]")
        core.messages.setDefault("feed-notification", "&aHunger refilled.")
        core.messages.setDefault("feed-valid", "&a\$player's hunger was refilled.")
        core.messages.setDefault("feed-usage-console", "&cWhen using this command from console, you must specify a player to feed.")
        core.messages.setDefault("fly-enable-notification", "&aFly mode enabled.")
        core.messages.setDefault("fly-enable-valid", "&aAllowed \$player to fly.")
        core.messages.setDefault("fly-disable-notification", "&aFly mode disabled.")
        core.messages.setDefault("fly-disable-valid", "&aDisallowed \$player to fly.")
        core.messages.setDefault("fly-usage-console", "&cWhen using this command from console, you must specify a player.")
        core.messages.setDefault("get-book-valid", "&aHere's a book.")
        core.messages.setDefault("get-sign-valid", "&aHere's a sign.")
        core.messages.setDefault("heal-notification", "&aHealed.")
        core.messages.setDefault("heal-valid", "&aHealed \$player.")
        core.messages.setDefault("heal-usage-console", "&cWhen using this command from console, you must specify a player to heal.")
        core.messages.setDefault("inventory-valid", "&aViewing \$player's inventory.")
        core.messages.setDefault("inventory-invalid-player", "&cThere is no player by that name online.")
        core.messages.setDefault("inventory-usage", "&cUsage: /inventory [player]")
        core.messages.setDefault("item-invalid-amount", "&cThe amount must be an integer.")
        core.messages.setDefault("item-valid-plural", "&aCreated \$amount \$types.")
        core.messages.setDefault("item-valid-singular", "&aCreated a \$type.")
        core.messages.setDefault("item-invalid-material", "&cCould not find a material by that name.")
        core.messages.setDefault("item-usage", "&cUsage: /item [type] [amount]")
        core.messages.setDefault("item-meta-set-name-valid", "&aSet item display name to $name.")
        core.messages.setDefault("item-meta-add-lore-valid", "&aAdded lore: \"\$lore&a\".")
        core.messages.setDefault("item-meta-remove-lore-valid", "&cRemoved lore: \"\$lore&a\".")
        core.messages.setDefault("item-meta-remove-lore-invalid-lore-item", "&cThat item does not have that piece of lore.")
        core.messages.setDefault("item-meta-remove-lore-invalid-lore", "&cThat item does not have any lore.")
        core.messages.setDefault("item-meta-usage", "&cUsage: /itemmeta [setname|addlore|removelore] [name|lore]")
        core.messages.setDefault("item-meta-invalid-item", "&cYou must be holding an item to modify its item meta.")
        core.messages.setDefault("jump-valid", "&aTeleported to first block in line of sight.")
        core.messages.setDefault("jump-invalid-block", "&cNo block was found within 64 blocks of your line of sight.")
        core.messages.setDefault("kit-valid", "&aClaimed \$kit.")
        core.messages.setDefault("kit-invalid-kit", "&cThere is no kit by that name.")
        core.messages.setDefault("kit-list-title", "&fKit list:")
        core.messages.setDefault("kit-list-item", "&f- &7\$kit")
        core.messages.setDefault("repair-valid", "&aItem repaired.")
        core.messages.setDefault("repair-invalid-item", "&cYou must be holding an item in order to repair it.")
        core.messages.setDefault("run-as-valid", "&aDispatched command.")
        core.messages.setDefault("run-as-invalid-player", "&cThere is no player by that name online.")
        core.messages.setDefault("run-as-usage", "&cUsage: /runas [player] [command]")
        core.messages.setDefault("seen-online", "&a\$player is online now.")
        core.messages.setDefault("seen-date", "&a\$player was last seen on \$date at \$time.")
        core.messages.setDefault("seen-diff", "&a(That's \$days days, \$hours hours, \$minutes minutes, \$seconds seconds ago.)")
        core.messages.setDefault("seen-never", "&aThat player has never played on this server.")
        core.messages.setDefault("seen-usage", "&cUsage: /seen [player]")
        core.messages.setDefault("set-spawn-valid", "&aSpawn location of \$world set.")
        core.messages.setDefault("set-warp-valid", "&aWarp \$warp set in \$world at \$x, \$y, \$z")
        core.messages.setDefault("set-warp-usage", "&cUsage: /setwarp [name]")
        core.messages.setDefault("smite-usage", "&cUsage: /smite [player]")
        core.messages.setDefault("smite-invalid-player", "&cThere is no player by that name online.")
        core.messages.setDefault("smite-valid", "&aOpening the heavens on \$player.")
        core.messages.setDefault("spawn-valid", "&aTeleported to spawn.")
        core.messages.setDefault("spawner-valid", "&aSpawner type set.")
        core.messages.setDefault("spawner-invalid-entity", "&cThere is no entity by that name.")
        core.messages.setDefault("spawner-invalid-block", "&cThat's not a mob spawner.")
        core.messages.setDefault("spawner-usage", "&cUsage: /spawner [type]")
        core.messages.setDefault("spawn-mob-valid", "&aMobs spawned.")
        core.messages.setDefault("spawn-mob-invalid-amount", "&cThe amount of mobs must be an integer.")
        core.messages.setDefault("spawn-mob-invalid-mob", "&cThat entity type does not exist.")
        core.messages.setDefault("spawn-mob-usage", "&cUsage: /spawnmob [type] [amount]")
        core.messages.setDefault("speed-invalid-speed-number", "&cSpeed must be a number.")
        core.messages.setDefault("speed-reset-valid", "&aReset \$player's fly speed.")
        core.messages.setDefault("speed-reset-notification", "&aYour fly speed was reset by \$player.")
        core.messages.setDefault("speed-set-valid", "&aSet \$player's fly speed to \$speed.")
        core.messages.setDefault("speed-set-notification", "&aYour fly speed was set to \$speed by \$player.")
        core.messages.setDefault("speed-invalid-speed-bounds", "&cSpeed must be between -1.0 and 1.0")
        core.messages.setDefault("speed-usage-console", "&cWhen using this command from console, you must specify a player to change the fly speed of.")
        core.messages.setDefault("toggle-log-messages-valid", "&aLog messages \$enabled.")
        core.messages.setDefault("toggle-tracking-on-valid", "&aYou have allowed people to find you with /track and /distance.")
        core.messages.setDefault("toggle-tracking-off-valid", "&aYou have disallowed people to find you with /track and /distance.")
        core.messages.setDefault("track-invalid-untrackable", "&cThat player is not currently trackable.")
        core.messages.setDefault("track-invalid-item", "&cYou require \$amount x \$type to use that command.")
        core.messages.setDefault("track-invalid-distance", "&cYou are too far away to track that player.")
        core.messages.setDefault("track-valid", "&aNow tracking \$player/\$character.")
        core.messages.setDefault("track-untrackable-notification", "&c\$player attempted to track you. If you wish for them to be able to find you, re-enable tracking with /toggletracking.")
        core.messages.setDefault("track-invalid-player", "&cThere is no player by that name online.")
        core.messages.setDefault("track-usage", "&cUsage: /track [player]")
        core.messages.setDefault("unsign-valid", "&aBook unsigned.")
        core.messages.setDefault("unsign-invalid-book", "&cYou must be holding a written book to unsign.")
        core.messages.setDefault("warp-valid", "&aWarped to \$warp.")
        core.messages.setDefault("warp-invalid-warp", "&cThere is no warp by that name.")
        core.messages.setDefault("warp-list-title", "&fWarps")
        core.messages.setDefault("warp-list-item", "&7\$warps")
        core.messages.setDefault("warp-list-invalid-empty", "&cNo warps are currently set. Set one using /setwarp [name].")
        core.messages.setDefault("no-permission-back", "&cYou do not have permission to go back.")
        core.messages.setDefault("no-permission-clone", "&cYou do not have permission to clone items.")
        core.messages.setDefault("no-permission-delete-warp", "&cYou do not have permission to delete warps.")
        core.messages.setDefault("no-permission-distance", "&cYou do not have permission to check your distance to players.")
        core.messages.setDefault("no-permission-enchant", "&cYou do not have permission to enchant items.")
        core.messages.setDefault("no-permission-feed", "&cYou do not have permission to feed players.")
        core.messages.setDefault("no-permission-fly", "&cYou do not have permission to toggle fly mode.")
        core.messages.setDefault("no-permission-get-book", "&cYou do not have permission to get books.")
        core.messages.setDefault("no-permission-get-sign", "&cYou do not have permission to get signs.")
        core.messages.setDefault("no-permission-heal", "&cYou do not have permission to heal players.")
        core.messages.setDefault("no-permission-inventory", "&cYou do not have permission to view other people's inventories.")
        core.messages.setDefault("no-permission-item", "&cYou do not have permission to spawn items.")
        core.messages.setDefault("no-permission-item-meta", "&cYou do not have permission to modify item meta.")
        core.messages.setDefault("no-permission-jump", "&cYou do not have permission to jump.")
        core.messages.setDefault("no-permission-kit", "&cYou do not have permission to claim kits.")
        core.messages.setDefault("no-permission-repair", "&cYou do not have permission to repair items.")
        core.messages.setDefault("no-permission-run-as", "&cYou do not have permission to run commands as other players.")
        core.messages.setDefault("no-permission-seen", "&cYou do not have permission to see when players were last online.")
        core.messages.setDefault("no-permission-set-spawn", "&cYou do not have permission to set the spawn.")
        core.messages.setDefault("no-permission-set-warp", "&cYou do not have permission to set warps.")
        core.messages.setDefault("no-permission-smite", "&cYou do not have permission to smite people.")
        core.messages.setDefault("no-permission-spawn", "&cYou do not have permission to teleport to spawn.")
        core.messages.setDefault("no-permission-spawner", "&cYou do not have permission to change spawner types.")
        core.messages.setDefault("no-permission-spawn-mob", "&cYou do not have permission to spawn mobs.")
        core.messages.setDefault("no-permission-speed", "&cYou do not have permission to set fly speed.")
        core.messages.setDefault("no-permission-sudo", "&c\$player is not in the sudoers file. This incident will be reported.")
        core.messages.setDefault("no-permission-toggle-log-messages", "&cYou do not have permission to toggle log messages.")
        core.messages.setDefault("no-permission-toggle-tracking", "&cYou do not have permission to toggle tracking.")
        core.messages.setDefault("no-permission-track", "&cYou do not have permission to track players.")
        core.messages.setDefault("no-permission-unsign", "&cYou do not have permission to unsign written books.")
        core.messages.setDefault("no-permission-warp", "&cYou do not have permission to warp.")
        core.messages.setDefault("not-from-console", "&cYou must be a player to perform this command.")
        core.messages.setDefault("no-character-self", "&cYou must have a character to perform that command.")
        core.messages.setDefault("no-character-other", "&cThat player must have a character to perform that command.")
    }

}