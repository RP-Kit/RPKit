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

package com.rpkit.auctions.bukkit.command.auction

import com.rpkit.auctions.bukkit.RPKAuctionsBukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

/**
 * Represents the auction command.
 * Parent for all auction management commands.
 * Currently, the only accepted operation is to create an auction by specifying 'create' or 'new'.
 */
class AuctionCommand(private val plugin: RPKAuctionsBukkit) : CommandExecutor {

    private val auctionCreateCommand = AuctionCreateCommand(plugin)

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isNotEmpty()) {
            val newArgs = args.drop(1).toTypedArray()
            if (args[0].equals("create", ignoreCase = true) || args[0].equals("new", ignoreCase = true)) {
                return auctionCreateCommand.onCommand(sender, command, label, newArgs)
            } else {
                sender.sendMessage(plugin.messages.auctionUsage)
            }
        } else {
            sender.sendMessage(plugin.messages.auctionUsage)
        }
        return true
    }

}