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

package com.rpkit.chat.bukkit.command.setchatnamecolor

import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.database.table.RPKChatNameColorTable
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileId
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.runAsync

/**
 * SetChatNameColor command.
 * Sets the chat color name for a player.
 */
class SetChatNameColorCommand(private val plugin: RPKChatBukkit) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("rpkit.chat.command.setchatnamecolor")) {
            sender.sendMessage(plugin.messages["no-permission-setchatnamecolor"])
            return true
        }
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java]
        if (minecraftProfileService == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile-service"])
            return true
        }
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }

        // if no arguments
        if (args.isEmpty()) {
            sender.sendMessage(plugin.messages["setchatnamecolor-usage"])
            return true
        }

        // retrieve desired chat name color
        val chatNameColor = args[0]
        if (isInputTooLong(chatNameColor)) {
            sender.sendMessage(plugin.messages["setchatnamecolor-too-long"])
            return true
        }

        if (!isHexColorCodeValid(chatNameColor)) {
            sender.sendMessage(plugin.messages["setchatnamecolor-invalid-color-code"])
            return true
        }

        val minecraftProfileId = minecraftProfile.id
        if (minecraftProfileId == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }

        setChatNameColorAsync(minecraftProfileId, chatNameColor).thenRun {
            sender.sendMessage(plugin.messages["setchatnamecolor-chat-name-color-has-been-set"])
        }.exceptionally { exception ->
            plugin.logger.severe("Failed to set chat name color for ${minecraftProfile.name}")
            plugin.logger.severe(exception.message)
            sender.sendMessage(plugin.messages["setchatnamecolor-something-went-wrong"])
            return@exceptionally null
        }
        return true
    }

    /**
     * Checks if the input is too long. The maximum length for a chat name color is 16 characters.
     * @param name the input to check
     * @return true if the input is too long, false otherwise
     */
    private fun isInputTooLong(name: String): Boolean {
        return name.length > 16
    }

    /**
     * Checks if the input is a valid hex color code. A valid hex color code is a string that starts with a '#' followed by 6 characters that are either digits or letters from a to f.
     * @param colorCode the color code to check
     * @return true if the color code is valid, false otherwise
     */
    private fun isHexColorCodeValid(colorCode: String): Boolean {
        return colorCode.matches(Regex("#[0-9a-fA-F]{6}"))
    }

    /**
     * Sets the chat name color for a player asynchronously.
     * @param minecraftProfileId the ID of the player's Minecraft profile
     * @param chatNameColor the chat name color to set
     * @return a string indicating the result of the operation
     */
    private fun setChatNameColorAsync(minecraftProfileId: RPKMinecraftProfileId, chatNameColor: String) : CompletableFuture<Void> {
        return runAsync {
            val recordExists = plugin.database.getTable(RPKChatNameColorTable::class.java)[minecraftProfileId].join() != null
            if (recordExists) {
                plugin.database.getTable(RPKChatNameColorTable::class.java).update(minecraftProfileId, chatNameColor)
            } else {
                plugin.database.getTable(RPKChatNameColorTable::class.java).insert(minecraftProfileId, chatNameColor)
            }
        }
    }
}