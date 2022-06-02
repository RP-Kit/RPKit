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

package com.rpkit.notifications.bukkit.command.notification

import com.rpkit.core.command.RPKCommandExecutor
import com.rpkit.core.command.result.CommandResult
import com.rpkit.core.command.result.CommandSuccess
import com.rpkit.core.command.result.MissingServiceFailure
import com.rpkit.core.command.result.NoPermissionFailure
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.RPKNotificationsBukkit
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.logging.Level

class NotificationListCommand(private val plugin: RPKNotificationsBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<out CommandResult> {
        if (!sender.hasPermission("rpkit.notifications.command.notification.list")) {
            sender.sendMessage(plugin.messages.noPermissionNotificationList)
            return completedFuture(NoPermissionFailure("rpkit.notifications.command.notification.list"))
        }
        if (sender !is RPKMinecraftProfile) {
            sender.sendMessage(plugin.messages.notFromConsole)
            return completedFuture(NotAPlayerFailure())
        }
        val profile = sender.profile
        if (profile !is RPKProfile) {
            sender.sendMessage(plugin.messages.noProfileSelf)
            return completedFuture(NoProfileSelfFailure())
        }
        val notificationService = Services[RPKNotificationService::class.java]
        if (notificationService == null) {
            sender.sendMessage(plugin.messages.noNotificationService)
            return completedFuture(MissingServiceFailure(RPKNotificationService::class.java))
        }
        return notificationService.getNotifications(profile).thenApply { notifications ->
            sender.sendMessage(plugin.messages.notificationListTitle)
            notifications.filter { notification -> !notification.read }
                .sortedByDescending { notification -> notification.time }
                .forEach { notification ->
                    val listItem = TextComponent(plugin.messages.notificationListItem.withParameters(notification))
                    listItem.hoverEvent = HoverEvent(SHOW_TEXT, Text(plugin.messages.notificationListItemHover))
                    listItem.clickEvent = ClickEvent(RUN_COMMAND, "/notification view ${notification.id?.value}")
                    sender.sendMessage(listItem)
                }
            return@thenApply CommandSuccess
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to list notifications", exception)
            throw exception
        }
    }
}