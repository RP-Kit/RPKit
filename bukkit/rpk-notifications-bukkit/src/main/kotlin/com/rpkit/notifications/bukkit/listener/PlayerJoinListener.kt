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
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
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
                        val unreadNotifications = notifications.filter { notification -> !notification.read }
                        if (unreadNotifications.isNotEmpty()) {
                            event.player.spigot().sendMessage(
                                TextComponent(plugin.messages.newNotifications.withParameters(amount = unreadNotifications.size)).apply {
                                    hoverEvent = HoverEvent(SHOW_TEXT, Text(plugin.messages.newNotificationsHover))
                                    clickEvent = ClickEvent(RUN_COMMAND, "/notification list")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}