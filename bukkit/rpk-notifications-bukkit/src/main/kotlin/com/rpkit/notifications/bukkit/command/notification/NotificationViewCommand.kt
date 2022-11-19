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
import com.rpkit.core.command.result.*
import com.rpkit.core.command.sender.RPKCommandSender
import com.rpkit.core.service.Services
import com.rpkit.notifications.bukkit.RPKNotificationsBukkit
import com.rpkit.notifications.bukkit.notification.RPKNotificationId
import com.rpkit.notifications.bukkit.notification.RPKNotificationService
import com.rpkit.players.bukkit.command.result.NoProfileSelfFailure
import com.rpkit.players.bukkit.command.result.NotAPlayerFailure
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import net.md_5.bungee.api.ChatColor.GRAY
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.ClickEvent.Action.RUN_COMMAND
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.logging.Level

class NotificationViewCommand(private val plugin: RPKNotificationsBukkit) : RPKCommandExecutor {
    override fun onCommand(sender: RPKCommandSender, args: Array<out String>): CompletableFuture<CommandResult> {
        if (!sender.hasPermission("rpkit.notifications.command.notification.view")) {
            sender.sendMessage(plugin.messages.noPermissionNotificationView)
            return completedFuture(NoPermissionFailure("rpkit.notifications.command.notification.view"))
        }
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages.notificationDismissUsage)
            return completedFuture(IncorrectUsageFailure())
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
        val notificationId = try {
            args[0].toInt()
        } catch (exception: NumberFormatException) {
            sender.sendMessage(plugin.messages.notificationDismissInvalidNotificationIdNotANumber)
            return completedFuture(NotificationDismissCommand.InvalidNotificationIdNotANumberFailure())
        }.let(::RPKNotificationId)
        val notificationService = Services[RPKNotificationService::class.java]
        if (notificationService == null) {
            sender.sendMessage(plugin.messages.noNotificationService)
            return completedFuture(MissingServiceFailure(RPKNotificationService::class.java))
        }
        val page = if (args.size > 1) {
            args[1].toIntOrNull() ?: 1
        } else {
            1
        }
        return notificationService.getNotification(notificationId).thenApplyAsync { notification ->
            if (notification == null) {
                sender.sendMessage(plugin.messages.notificationViewInvalidNotification)
                return@thenApplyAsync NotificationDismissCommand.InvalidNotificationFailure()
            }
            if (notification.recipient.id != profile.id) {
                sender.sendMessage(plugin.messages.notificationViewInvalidRecipient)
                return@thenApplyAsync NotificationDismissCommand.InvalidNotificationRecipientFailure()
            }
            notification.read = true
            return@thenApplyAsync notificationService.updateNotification(notification).thenApply {
                plugin.messages.notificationViewValid
                    .withParameters(notification = notification)
                    .forEach(sender::sendMessage)
                sender.sendMessage(
                    *TextComponent.fromLegacyText(plugin.messages.notificationViewViewList).map {
                        it.apply {
                            hoverEvent = HoverEvent(SHOW_TEXT, Text(TextComponent.fromLegacyText(plugin.messages.notificationViewViewListHover)))
                            clickEvent = ClickEvent(RUN_COMMAND, "/notification list $page")
                        }
                    }.toTypedArray(),
                    TextComponent(" - ").apply { color = GRAY },
                    *TextComponent.fromLegacyText(plugin.messages.notificationViewDismiss).map {
                        it.apply {
                            hoverEvent = HoverEvent(SHOW_TEXT, Text(TextComponent.fromLegacyText(plugin.messages.notificationViewDismissHover)))
                            clickEvent = ClickEvent(RUN_COMMAND, "/notification dismiss ${notification.id?.value}")
                        }
                    }.toTypedArray()
                )
                return@thenApply CommandSuccess
            }.join()
        }.exceptionally { exception ->
            plugin.logger.log(Level.SEVERE, "Failed to view notification", exception)
            throw exception
        }
    }
}