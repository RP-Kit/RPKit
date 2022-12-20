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

package com.rpkit.chat.bukkit.listener

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannel
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelMessageCallback
import com.rpkit.chat.bukkit.chatchannel.RPKChatChannelService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKThinProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

/**
 * Player chat listener.
 * Cancels normal message processing and passes the message to the appropriate chat channel.
 */
class AsyncPlayerChatListener(private val plugin: RPKChatBukkit) : Listener {

    private val conversationFactory = ConversationFactory(plugin)
        .withModality(true)
        .withFirstPrompt(ContinueMessagePrompt())
        .thatExcludesNonPlayersWithMessage(plugin.messages.notFromConsole)
        .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(plugin.messages.operationCancelled)
                }
            }
        }

    inner class ContinueMessagePrompt : StringPrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.continueWriting
        }

        override fun acceptInput(context: ConversationContext, input: String?): Prompt? {
            val suffix = plugin.config.getString("message-continuation.suffix")
            return when {
                input == null -> OperationCancelledPrompt()
                suffix != null && input.endsWith(suffix) -> {
                    context.setSessionData(
                        "message",
                        (context.getSessionData("message") as String? ?: "") + input.dropLast(suffix.length)
                    )
                    ContinueMessagePrompt()
                }
                else -> {
                    processMessage(context.forWhom as Player, (context.getSessionData("message") as String? ?: "") + input)
                    END_OF_CONVERSATION
                }
            }
        }
    }

    inner class OperationCancelledPrompt : MessagePrompt() {
        override fun getPromptText(context: ConversationContext): String {
            return plugin.messages.operationCancelled
        }

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return END_OF_CONVERSATION
        }

    }

    @EventHandler
    fun onAsyncPlayerChat(event: AsyncPlayerChatEvent) {
        event.isCancelled = true
        plugin.server.scheduler.runTask(plugin, Runnable {
            val continuationEnabled = plugin.config.getBoolean("message-continuation.enabled")
            val suffix = plugin.config.getString("message-continuation.suffix")
            if (continuationEnabled && suffix != null && event.message.endsWith(suffix)) {
                val conversation = conversationFactory.buildConversation(event.player)
                conversation.context.setSessionData("message", event.message.dropLast(suffix.length))
                conversation.begin()
            } else {
                processMessage(event.player, event.message)
            }
        })
    }

    private fun processMessage(player: Player, message: String) {
        val chatChannelService = Services[RPKChatChannelService::class.java] ?: return
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(player)
        if (minecraftProfile != null) {
            val profile = minecraftProfile.profile
            chatChannelService.getMinecraftProfileChannel(minecraftProfile).thenAcceptAsync { chatChannel ->
                val queuedMessages = mutableListOf<QueuedMessage>()
                var readMessageIndex = 0
                chatChannelService.matchPatterns
                    .map { matchPattern ->
                        val matches = matchPattern.regex.let(::Regex).findAll(message).toList()
                        matches to matchPattern
                    }
                    .flatMap { (matches, matchPattern) -> matches.associateWith { matchPattern }.toList() }
                    .sortedBy { (match, _) -> match.range.first }
                    .forEach { (match, matchPattern) ->
                        queuedMessages.add(
                            QueuedMessage(
                                chatChannel,
                                message.substring(readMessageIndex, match.range.first),
                                player,
                                profile,
                                minecraftProfile
                            )
                        )
                        match.groupValues.forEachIndexed { index, value ->
                            val otherChatChannel = matchPattern.groups[index]
                            if (otherChatChannel != null) {
                                queuedMessages.add(
                                    QueuedMessage(
                                        otherChatChannel,
                                        value,
                                        player,
                                        profile,
                                        minecraftProfile
                                    )
                                )
                            }
                        }
                        readMessageIndex = match.range.last + 1
                    }
                if (readMessageIndex < message.length) {
                    queuedMessages.add(
                        QueuedMessage(
                            chatChannel,
                            message.substring(readMessageIndex, message.length),
                            player,
                            profile,
                            minecraftProfile
                        )
                    )
                }
                sendMessages(queuedMessages)
            }
        } else {
            player.sendMessage(plugin.messages.noMinecraftProfile)
        }
    }

    private fun sendMessages(queue: List<QueuedMessage>) {
        if (queue.isNotEmpty()) {
            sendMessage(queue.first()) { sendMessages(queue.drop(1)) }
        }
    }

    private fun sendMessage(message: QueuedMessage, callback: RPKChatChannelMessageCallback? = null) {
        sendMessage(message.chatChannel, message.message, message.bukkitPlayer, message.profile, message.minecraftProfile, callback)
    }

    private fun sendMessage(
        chatChannel: RPKChatChannel?,
        message: String,
        bukkitPlayer: Player,
        profile: RPKThinProfile,
        minecraftProfile: RPKMinecraftProfile,
        callback: RPKChatChannelMessageCallback? = null
    ) {
        if (chatChannel != null) {
            plugin.server.scheduler.runTask(plugin, Runnable {
                chatChannel.listeners.thenAcceptAsync { listeners ->
                    if (!listeners.any { listenerMinecraftProfile ->
                            listenerMinecraftProfile.id == minecraftProfile.id
                        }) {
                        chatChannel.addListener(minecraftProfile).join()
                    }
                    if (message.isNotBlank()) {
                        chatChannel.sendMessage(
                            profile,
                            minecraftProfile,
                            message.trim(),
                            true,
                            callback
                        )
                    } else {
                        callback?.invoke()
                    }
                }
            })
        } else {
            bukkitPlayer.sendMessage(plugin.messages.noChatChannel)
        }
    }

    private data class QueuedMessage(
        val chatChannel: RPKChatChannel?,
        val message: String,
        val bukkitPlayer: Player,
        val profile: RPKThinProfile,
        val minecraftProfile: RPKMinecraftProfile
    )

}
