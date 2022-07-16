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

package com.rpkit.players.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.md_5.bungee.api.ChatColor.GREEN
import net.md_5.bungee.api.ChatColor.RED
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
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
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player) ?: return
        minecraftProfileService.getMinecraftProfileLinkRequests(minecraftProfile).thenAccept { minecraftProfileLinkRequests ->
            if (minecraftProfileLinkRequests.isEmpty()) return@thenAccept
            minecraftProfileLinkRequests.forEach {
                val messageComponent = TextComponent.fromLegacyText(
                    plugin.messages.profileLinkRequest.withParameters(profile = it.profile)
                )
                val yesComponent = TextComponent(plugin.messages.yes)
                yesComponent.color = GREEN
                yesComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile confirmlink minecraft ${it.profile.id}")
                yesComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder().appendLegacy("Click to link account to ${it.profile.name}").create())
                val noComponent = TextComponent(plugin.messages.no)
                noComponent.color = RED
                noComponent.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/profile denylink minecraft ${it.profile.id}")
                noComponent.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder().appendLegacy("Click to cancel linking to ${it.profile.name}").create())
                event.player.spigot().sendMessage(*messageComponent)
                event.player.spigot().sendMessage(yesComponent)
                event.player.spigot().sendMessage(noComponent)
            }
        }

    }

}
