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

package com.rpkit.notifications.bukkit.listener

import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.RPKNotificationsBukkit
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ComponentBuilder
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent

class PlayerJoinListener(private val plugin: RPKNotificationsBukkit) : Listener {
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val notificationService = Services[RPKNotificationService::class.java] ?: return
        minecraftProfileService.getMinecraftProfile(event.player).thenAccept { minecraftProfile ->
            if (minecraftProfile != null) {
                val profile = minecraftProfile.profile
                if (profile is RPKProfile) {
                    notificationService.getNotifications(profile).thenAccept { notifications ->
                        event.player.sendMessage(plugin.messages.notificationListTitle)
                        notifications.filter { notification -> !notification.read }
                            .sortedByDescending { notification -> notification.time }
                            .forEach { notification ->
                                val listItem = TextComponent(plugin.messages.notificationListItem.withParameters(notification))
                                listItem.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, ComponentBuilder(plugin.messages.notificationListItemHover).create())
                                listItem.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/notification view ${notification.id?.value}")
                                event.player.spigot().sendMessage(listItem)
                            }
                    }
                }
            }
        }
    }
}