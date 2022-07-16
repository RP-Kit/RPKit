/*
 * Copyright 2022 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.essentials.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.location.RPKLocation
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.kit.bukkit.kit.RPKKit
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import java.text.DecimalFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

class EssentialsMessages(plugin: RPKEssentialsBukkit) : BukkitMessages(plugin) {

    class DistanceInvalidItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(requiredItem: ItemStack) = message.withParameters(
            "amount" to requiredItem.amount.toString(),
            "type" to requiredItem.type.name.lowercase().replace('_', ' ')
        )
    }

    class DistanceValidMessage(private val message: ParameterizedMessage) {
        private val decimalFormat = DecimalFormat("#.##")
        fun withParameters(
            character: RPKCharacter,
            player: RPKMinecraftProfile,
            distance: Double
        ) = message.withParameters(
            "character" to character.name,
            "player" to player.name,
            "distance" to decimalFormat.format(distance)
        )
    }

    class DistanceUntrackableNotificationMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            character: RPKCharacter,
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "character" to character.name,
            "player" to player.name
        )
    }

    class EnchantValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            item: ItemStack,
            enchantment: Enchantment,
            level: Int
        ) = message.withParameters(
            "amount" to item.amount.toString(),
            "type" to item.type.name.lowercase().replace('_', ' '),
            "enchantment" to enchantment.name.lowercase().replace('_', ' '),
            "level" to level.toString()
        )
    }

    class FeedValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class FlyEnableValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class FlyDisableValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class HealValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class InventoryValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class IssueSubmitValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            link: String
        ) = message.withParameters(
            "link" to link
        )
    }

    class ItemValidPluralMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            item: ItemStack
        ) = message.withParameters(
            "amount" to item.amount.toString(),
            "type" to item.type.name.lowercase().replace('_', ' ')
        )
    }

    class ItemValidSingularMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            item: ItemStack
        ) = message.withParameters(
            "type" to item.type.name.lowercase().replace('_', ' ')
        )
    }

    class ItemMetaSetNameValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            name: String
        ) = message.withParameters(
            "name" to name
        )
    }

    class ItemMetaAddLoreValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            lore: String
        ) = message.withParameters(
            "lore" to lore
        )
    }

    class ItemMetaRemoveLoreValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            lore: String
        ) = message.withParameters(
            "lore" to lore
        )
    }

    class ItemMetaCustomModelDataValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            customModelData: Int
        ) = message.withParameters(
            "custom_model_data" to customModelData.toString()
        )
    }

    class KitValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(kit: RPKKit) = message.withParameters(
            "kit" to kit.name.value
        )
    }

    class KitListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(kit: RPKKit) = message.withParameters(
            "kit" to kit.name.value
        )
    }

    class SaveItemValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(name: String) = message.withParameters(
            "name" to name
        )
    }

    class SeenOnlineMessage(private val message: ParameterizedMessage) {
        fun withParameters(player: RPKMinecraftProfile) = message.withParameters(
            "player" to player.name
        )
    }

    class SeenDateMessage(private val message: ParameterizedMessage) {
        private val dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
        fun withParameters(
            player: RPKMinecraftProfile,
            dateTime: ZonedDateTime
        ) = message.withParameters(
            "player" to player.name,
            "date" to dateFormat.format(dateTime),
            "time" to timeFormat.format(dateTime)
        )
    }

    class SeenDiffMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            days: Int,
            hours: Int,
            minutes: Int,
            seconds: Int
        ) = message.withParameters(
            "days" to days.toString(),
            "hours" to hours.toString(),
            "minutes" to minutes.toString(),
            "seconds" to seconds.toString()
        )
    }

    class SetSpawnValidMessage(private val message: ParameterizedMessage) {
        private val decimalFormat = DecimalFormat("#.##")
        fun withParameters(
            location: RPKLocation
        ) = message.withParameters(
            "world" to location.world,
            "x" to decimalFormat.format(location.x),
            "y" to decimalFormat.format(location.y),
            "z" to decimalFormat.format(location.z),
            "yaw" to decimalFormat.format(location.yaw),
            "pitch" to decimalFormat.format(location.pitch)
        )
    }

    class ShowItemValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(character: RPKCharacter, item: ItemStack): String {
            var itemName = item.type.toString().lowercase().replace('_', ' ')
            if (item.hasItemMeta()) {
                if (item.itemMeta?.hasDisplayName() == true) {
                    itemName = item.itemMeta?.displayName ?: item.type.toString().lowercase().replace('_', ' ')
                }
            }
            return message.withParameters(
                "character" to character.name,
                "item" to "${item.amount} x $itemName"
            )
        }
    }

    class SmiteValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class SpeedResetValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class SpeedResetNotificationMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class SpeedSetValidMessage(private val message: ParameterizedMessage) {
        private val decimalFormat = DecimalFormat("#.##")
        fun withParameters(
            player: RPKMinecraftProfile,
            speed: Double
        ) = message.withParameters(
            "player" to player.name,
            "speed" to decimalFormat.format(speed)
        )
    }

    class SpeedSetNotificationMessage(private val message: ParameterizedMessage) {
        private val decimalFormat = DecimalFormat("#.##")
        fun withParameters(
            player: RPKMinecraftProfile,
            speed: Double
        ) = message.withParameters(
            "player" to player.name,
            "speed" to decimalFormat.format(speed)
        )
    }

    class ToggleLogMessagesValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(enabled: String) = message.withParameters(
            "enabled" to enabled
        )
    }

    class TrackInvalidItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(requiredItem: ItemStack) = message.withParameters(
            "type" to requiredItem.type.name.lowercase().replace('_', ' '),
            "amount" to requiredItem.amount.toString()
        )
    }

    class TrackValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile,
            character: RPKCharacter
        ) = message.withParameters(
            "player" to player.name,
            "character" to character.name
        )
    }

    class TrackUntrackableNotification(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    class NoPermissionSudoMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            player: RPKMinecraftProfile
        ) = message.withParameters(
            "player" to player.name
        )
    }

    val backValid = get("back-valid")
    val backInvalidNoLocations = get("back-invalid-no-locations")
    val cloneValid = get("clone-valid")
    val cloneInvalidItem = get("clone-invalid-item")
    val distanceInvalidUntrackable = get("distance-invalid-untrackable")
    val distanceInvalidItem = getParameterized("distance-invalid-item")
        .let(::DistanceInvalidItemMessage)
    val distanceInvalidDistance = get("distance-invalid-distance")
    val distanceValid = getParameterized("distance-valid")
        .let(::DistanceValidMessage)
    val distanceUntrackableNotification = getParameterized("distance-untrackable-notification")
        .let(::DistanceUntrackableNotificationMessage)
    val distanceInvalidWorld = get("distance-invalid-world")
    val distanceInvalidPlayer = get("distance-invalid-player")
    val distanceUsage = get("distance-usage")
    val enchantValid = getParameterized("enchant-valid")
        .let(::EnchantValidMessage)
    val enchantInvalidLevel = get("enchant-invalid-level")
    val enchantInvalidEnchantment = get("enchant-invalid-enchantment")
    val enchantInvalidIllegal = get("enchant-invalid-illegal")
    val enchantInvalidItem = get("enchant-invalid-item")
    val enchantUsage = get("enchant-usage")
    val feedNotification = get("feed-notification")
    val feedValid = getParameterized("feed-valid")
        .let(::FeedValidMessage)
    val feedUsageConsole = get("feed-usage-console")
    val flyEnableNotification = get("fly-enable-notification")
    val flyEnableValid = getParameterized("fly-enable-valid")
        .let(::FlyEnableValidMessage)
    val flyDisableNotification = get("fly-disable-notification")
    val flyDisableValid = getParameterized("fly-disable-valid")
        .let(::FlyDisableValidMessage)
    val flyUsageConsole = get("fly-usage-console")
    val getBookValid = get("get-book-valid")
    val getSignValid = get("get-sign-valid")
    val healNotification = get("heal-notification")
    val healValid = getParameterized("heal-valid")
        .let(::HealValidMessage)
    val healUsageConsole = get("heal-usage-console")
    val inventoryValid = getParameterized("inventory-valid")
        .let(::InventoryValidMessage)
    val inventoryInvalidPlayer = get("inventory-invalid-player")
    val inventoryUsage = get("inventory-usage")
    val issueUsage = get("issue-usage")
    val issueSubmitInvalidBook = get("issue-submit-invalid-book")
    val issueSubmitInvalidTitle = get("issue-submit-invalid-title")
    val issueSubmitValid = getParameterized("issue-submit-valid")
        .let(::IssueSubmitValidMessage)
    val itemInvalidAmount = get("item-invalid-amount")
    val itemValidPlural = getParameterized("item-valid-plural")
        .let(::ItemValidPluralMessage)
    val itemValidSingular = getParameterized("item-valid-singular")
        .let(::ItemValidSingularMessage)
    val itemInvalidMaterial = get("item-invalid-material")
    val itemUsage = get("item-usage")
    val itemMetaSetNameValid = getParameterized("item-meta-set-name-valid")
        .let(::ItemMetaSetNameValidMessage)
    val itemMetaAddLoreValid = getParameterized("item-meta-add-lore-valid")
        .let(::ItemMetaAddLoreValidMessage)
    val itemMetaRemoveLoreValid = getParameterized("item-meta-remove-lore-valid")
        .let(::ItemMetaRemoveLoreValidMessage)
    val itemMetaRemoveLoreInvalidLoreItem = get("item-meta-remove-lore-invalid-lore-item")
    val itemMetaRemoveLoreInvalidLore = get("item-meta-remove-lore-invalid-lore")
    val itemMetaUsage = get("item-meta-usage")
    val itemMetaInvalidItem = get("item-meta-invalid-item")
    val itemMetaFailedToCreate = get("item-meta-failed-to-create")
    val itemMetaCustomModelDataInvalidCustomModelData = get("item-meta-custom-model-data-invalid-custom-model-data")
    val itemMetaCustomModelDataValid = getParameterized("item-meta-custom-model-data-valid")
        .let(::ItemMetaCustomModelDataValidMessage)
    val jumpValid = get("jump-valid")
    val jumpInvalidBlock = get("jump-invalid-block")
    val kitValid = getParameterized("kit-valid")
        .let(::KitValidMessage)
    val kitInvalidKit = get("kit-invalid-kit")
    val kitListTitle = get("kit-list-title")
    val kitListItem = getParameterized("kit-list-item")
        .let(::KitListItemMessage)
    val repairValid = get("repair-valid")
    val repairInvalidItem = get("repair-invalid-item")
    val runAsValid = get("run-as-valid")
    val runAsInvalidPlayer = get("run-as-invalid-player")
    val runAsUsage = get("run-as-usage")
    val saveItemUsage = get("save-item-usage")
    val saveItemValid = getParameterized("save-item-valid").let(::SaveItemValidMessage)
    val seenOnline = getParameterized("seen-online")
        .let(::SeenOnlineMessage)
    val seenDate = getParameterized("seen-date")
        .let(::SeenDateMessage)
    val seenDiff = getParameterized("seen-diff")
    val seenNever = get("seen-never")
    val seenUsage = get("seen-usage")
    val setSpawnValid = getParameterized("set-spawn-valid")
        .let(::SetSpawnValidMessage)
    val showItemUsage = get("show-item-usage")
    val showItemInvalidTarget = get("show-item-invalid-target")
    val showItemInvalidNoItem = get("show-item-invalid-no-item")
    val showItemValid = getParameterized("show-item-valid")
        .let(::ShowItemValidMessage)
    val smiteUsage = get("smite-usage")
    val smiteInvalidPlayer = get("smite-invalid-player")
    val smiteValid = getParameterized("smite-valid")
        .let(::SmiteValidMessage)
    val spawnValid = get("spawn-valid")
    val spawnerValid = get("spawner-valid")
    val spawnerInvalidEntity = get("spawner-invalid-entity")
    val spawnerInvalidBlock = get("spawner-invalid-block")
    val spawnerUsage = get("spawner-usage")
    val spawnMobValid = get("spawn-mob-valid")
    val spawnMobInvalidAmount = get("spawn-mob-invalid-amount")
    val spawnMobInvalidMob = get("spawn-mob-invalid-mob")
    val spawnMobUsage = get("spawn-mob-usage")
    val speedInvalidSpeedNumber = get("speed-invalid-speed-number")
    val speedResetValid = getParameterized("speed-reset-valid")
        .let(::SpeedResetValidMessage)
    val speedResetNotification = getParameterized("speed-reset-notification")
        .let(::SpeedResetNotificationMessage)
    val speedSetValid = getParameterized("speed-set-valid")
        .let(::SpeedSetValidMessage)
    val speedSetNotification = getParameterized("speed-set-notification")
        .let(::SpeedSetNotificationMessage)
    val speedInvalidSpeedBounds = get("speed-invalid-speed-bounds")
    val speedUsageConsole = get("speed-usage-console")
    val toggleLogMessagesValid = getParameterized("toggle-log-messages-valid")
        .let(::ToggleLogMessagesValidMessage)
    val toggleTrackingOnValid = get("toggle-tracking-on-valid")
    val toggleTrackingOffValid = get("toggle-tracking-off-valid")
    val trackInvalidUntrackable = get("track-invalid-untrackable")
    val trackInvalidItem = getParameterized("track-invalid-item")
        .let(::TrackInvalidItemMessage)
    val trackInvalidDistance = get("track-invalid-distance")
    val trackValid = getParameterized("track-valid")
        .let(::TrackValidMessage)
    val trackUntrackableNotification = getParameterized("track-untrackable-notification")
        .let(::TrackUntrackableNotification)
    val trackInvalidPlayer = get("track-invalid-player")
    val trackUsage = get("track-usage")
    val unsignValid = get("unsign-valid")
    val unsignInvalidBook = get("unsign-invalid-book")
    val noPermissionBack = get("no-permission-back")
    val noPermissionClone = get("no-permission-clone")
    val noPermissionDistance = get("no-permission-distance")
    val noPermissionEnchant = get("no-permission-enchant")
    val noPermissionFeed = get("no-permission-feed")
    val noPermissionFly = get("no-permission-fly")
    val noPermissionGetBook = get("no-permission-get-book")
    val noPermissionGetSign = get("no-permission-get-sign")
    val noPermissionHeal = get("no-permission-heal")
    val noPermissionInventory = get("no-permission-inventory")
    val noPermissionIssueSubmit = get("no-permission-issue-submit")
    val noPermissionItem = get("no-permission-item")
    val noPermissionItemMeta = get("no-permission-item-meta")
    val noPermissionJump = get("no-permission-jump")
    val noPermissionKit = get("no-permission-kit")
    val noPermissionRepair = get("no-permission-repair")
    val noPermissionRunAs = get("no-permission-run-as")
    val noPermissionSaveItem = get("no-permission-save-item")
    val noPermissionSeen = get("no-permission-seen")
    val noPermissionSetSpawn = get("no-permission-set-spawn")
    val noPermissionShowItem = get("no-permission-show-item")
    val noPermissionSmite = get("no-permission-smite")
    val noPermissionSpawn = get("no-permission-spawn")
    val noPermissionSpawner = get("no-permission-spawner")
    val noPermissionSpawnMob = get("no-permission-spawn-mob")
    val noPermissionSpeed = get("no-permission-speed")
    val noPermissionSudo = getParameterized("no-permission-sudo")
        .let(::NoPermissionSudoMessage)
    val noPermissionToggleLogMessages = get("no-permission-toggle-log-messages")
    val noPermissionToggleTracking = get("no-permission-toggle-tracking")
    val noPermissionTrack = get("no-permission-track")
    val noPermissionUnsign = get("no-permission-unsign")
    val notFromConsole = get("not-from-console")
    val noCharacterSelf = get("no-character-self")
    val noCharacterOther = get("no-character-other")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noProfileSelf = get("no-profile-self")
    val noGithubProfile = get("no-github-profile")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noLocationHistoryService = get("no-location-history-service")
    val noKitService = get("no-kit-service")
    val noLogMessageService = get("no-log-message-service")
    val noTrackingService = get("no-tracking-service")
    val noGithubProfileService = get("no-github-profile-service")
    val noGithubService = get("no-github-service")
}