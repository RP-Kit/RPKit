/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.players.bukkit.listener

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Player join listener for creating player instance.
 */
class PlayerJoinListener(private val plugin: RPKPlayersBukkit): Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        // If RPK doesn't have a player for Bukkit's player, create it here so that the player can log on through
        // web UI. If RPK DOES have a player, update the last known IP.
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        val player = playerProvider.getPlayer(event.player)
        player.lastKnownIP = event.player.address?.address?.hostAddress
        playerProvider.updatePlayer(player)

        // If the player's Minecraft profile is not linked to a profile, run the player through linking their profile.
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(event.player)
        if (minecraftProfile != null) { // Minecraft profile should never be null due to the PlayerLoginListener creating these, but it doesn't hurt to be safe
            val minecraftProfileLinkRequests = minecraftProfileProvider.getMinecraftProfileLinkRequests(minecraftProfile)
            if (!minecraftProfileLinkRequests.isEmpty()) {
                minecraftProfileLinkRequests.forEach {
                    val messageComponent = TextComponent.fromLegacyText(plugin.messages["profile-link-request", mapOf(
                            Pair("profile", it.profile.name)
                    )])
                    val yesComponent = TextComponent(plugin.messages["yes"])
                    yesComponent.color = GREEN
                    yesComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/account confirmlink minecraft ${it.profile.id}")
                    yesComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("Click to link account to ${it.profile.name}")))
                    val noComponent = TextComponent(plugin.messages["no"])
                    noComponent.color = RED
                    noComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/account denylink minecraft ${it.profile.id}")
                    noComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(TextComponent("Click to cancel linking to ${it.profile.name}")))
                    event.player.spigot().sendMessage(*messageComponent)
                    event.player.spigot().sendMessage(yesComponent)
                    event.player.spigot().sendMessage(noComponent)
                }
            }
        }
    }

}
