/*
 * Copyright 2020 Ren Binden
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

package com.rpkit.essentials.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.core.util.MathUtils.fastSqrt
import com.rpkit.essentials.bukkit.RPKEssentialsBukkit
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import com.rpkit.tracking.bukkit.tracking.RPKTrackingService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class DistanceCommand(private val plugin: RPKEssentialsBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (!sender.hasPermission("rpkit.essentials.command.distance")) {
            sender.sendMessage(plugin.messages["no-permission-distance"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["distance-usage"])
            return true
        }
        val bukkitPlayer = plugin.server.getPlayer(args[0])
        if (bukkitPlayer == null) {
            sender.sendMessage(plugin.messages["distance-invalid-offline"])
            return true
        }
        if (bukkitPlayer.world !== sender.world) {
            sender.sendMessage(plugin.messages["distance-invalid-world"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val characterService = Services[RPKCharacterService::class.java]
        if (characterService == null) {
            sender.sendMessage(plugin.messages["no-character-service"])
            return true
        }
        val trackingService = Services[RPKTrackingService::class.java]
        if (trackingService == null) {
            sender.sendMessage(plugin.messages["no-tracking-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(bukkitPlayer)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val character = characterService.getActiveCharacter(minecraftProfile)
        if (character == null) {
            sender.sendMessage(plugin.messages["no-character"])
            return true
        }
        if (!trackingService.isTrackable(character)) {
            sender.sendMessage(plugin.messages["distance-invalid-untrackable"])
            bukkitPlayer.sendMessage(plugin.messages["distance-untrackable-notification", mapOf(
                    "player" to minecraftProfile.name
            )])
            return true
        }
        val itemRequirement = plugin.config.getItemStack("distance-command.item-requirement")
        if (itemRequirement != null && !bukkitPlayer.inventory.containsAtLeast(itemRequirement, itemRequirement.amount)) {
                sender.sendMessage(plugin.messages["distance-invalid-item", mapOf(
                        "amount" to itemRequirement.amount.toString(),
                        "type" to itemRequirement.type.toString().toLowerCase().replace('_', ' ')
                )])
                return true
            }
        val maximumDistance = plugin.config.getInt("distance-command.maximum-distance")
        val distance = fastSqrt(bukkitPlayer.location.distanceSquared(sender.location))
        if (maximumDistance >= 0 && distance > maximumDistance) {
            sender.sendMessage(plugin.messages["distance-invalid-distance"])
            return true
        }
        sender.sendMessage(plugin.messages["distance-valid", mapOf(
                "character" to character.name,
                "player" to minecraftProfile.name,
                "distance" to distance.toString()
        )])
        return true
    }

}
