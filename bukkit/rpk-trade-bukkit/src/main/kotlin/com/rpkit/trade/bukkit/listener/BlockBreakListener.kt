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

package com.rpkit.trade.bukkit.listener

import com.rpkit.trade.bukkit.RPKTradeBukkit
import org.bukkit.ChatColor.GREEN
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent

/**
 * Block break listener for trader signs.
 */
class BlockBreakListener(private val plugin: RPKTradeBukkit): Listener {

    @EventHandler
    fun onBlockBreak(event: BlockBreakEvent) {
        if (event.block != null) {
            if (event.block.state != null) {
                if (event.block.state is Sign) {
                    val sign = event.block.state as Sign
                    if (sign.getLine(0) == "$GREEN[trader]") {
                        if (!event.player.hasPermission("rpkit.trade.sign.trader.destroy")) {
                            event.isCancelled = true
                            event.player.sendMessage(plugin.messages["no-permission-trader-destroy"])
                        }
                    }
                }
            }
        }
    }

}