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

package com.rpkit.trade.bukkit

import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.trade.bukkit.listener.BlockBreakListener
import com.rpkit.trade.bukkit.listener.PlayerInteractListener
import com.rpkit.trade.bukkit.listener.SignChangeListener
import com.rpkit.trade.bukkit.messages.TradeMessages
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin

/**
 * RPK trade plugin default implementation.
 */
class RPKTradeBukkit : JavaPlugin(), RPKPlugin {

    lateinit var messages: TradeMessages

    override fun onEnable() {
        Metrics(this, 4423)
        saveDefaultConfig()

        messages = TradeMessages(this)

        registerListeners()
    }

    fun registerListeners() {
        registerListeners(
                BlockBreakListener(this),
                SignChangeListener(this),
                PlayerInteractListener(this)
        )
    }

}