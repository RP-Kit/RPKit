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

package com.rpkit.players.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

/**
 * Player join listener for creating player instance.
 */
class PlayerJoinListener(private val plugin: RPKPlayersBukkit) : Listener {

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getMinecraftProfile(event.player) ?: return
        val minecraftProfileLinkRequests = minecraftProfileService.getMinecraftProfileLinkRequests(minecraftProfile)
        if (minecraftProfileLinkRequests.isEmpty()) return
        minecraftProfileLinkRequests.forEach {
            val messageComponent = TextComponent.fromLegacyText(plugin.messages["profile-link-request", mapOf(
                    "profile" to it.profile.name
            )])
            val yesComponent = TextComponent(plugin.messages["yes"])
            yesComponent.color = GREEN
            yesComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/account confirmlink minecraft ${it.profile.id}")
            yesComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to link account to ${it.profile.name}"))
            val noComponent = TextComponent(plugin.messages["no"])
            noComponent.color = RED
            noComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/account denylink minecraft ${it.profile.id}")
            noComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Click to cancel linking to ${it.profile.name}"))
            event.player.spigot().sendMessage(*messageComponent)
            event.player.spigot().sendMessage(yesComponent)
            event.player.spigot().sendMessage(noComponent)
        }
    }

}
