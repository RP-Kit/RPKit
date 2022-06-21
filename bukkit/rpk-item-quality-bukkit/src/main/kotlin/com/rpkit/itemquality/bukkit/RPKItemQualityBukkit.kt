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

package com.rpkit.itemquality.bukkit

import com.rpkit.core.bukkit.listener.registerListeners
import com.rpkit.core.plugin.RPKPlugin
import com.rpkit.core.service.Services
import com.rpkit.itemquality.bukkit.command.itemquality.ItemQualityCommand
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityService
import com.rpkit.itemquality.bukkit.itemquality.RPKItemQualityServiceImpl
import com.rpkit.itemquality.bukkit.listener.PlayerItemDamageListener
import com.rpkit.itemquality.bukkit.messages.ItemQualityMessages
import org.bstats.bukkit.Metrics
import org.bukkit.plugin.java.JavaPlugin


class RPKItemQualityBukkit : JavaPlugin(), RPKPlugin {

    lateinit var messages: ItemQualityMessages

    override fun onEnable() {
        Metrics(this, 6658)
        saveDefaultConfig()

        messages = ItemQualityMessages(this)

        Services[RPKItemQualityService::class.java] = RPKItemQualityServiceImpl(this)

        registerListeners()
        registerCommands()
    }

    fun registerListeners() {
        registerListeners(
                PlayerItemDamageListener(this)
        )
    }

    fun registerCommands() {
        getCommand("itemquality")?.setExecutor(ItemQualityCommand(this))
    }

}