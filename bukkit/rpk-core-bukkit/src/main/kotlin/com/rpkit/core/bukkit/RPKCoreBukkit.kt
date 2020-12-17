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

package com.rpkit.core.bukkit

import com.rpkit.core.bukkit.listener.PluginEnableListener
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.bukkit.service.BukkitServicesDelegate
import com.rpkit.core.service.Services
import org.bstats.bukkit.Metrics

/**
 * RPK's core, Bukkit implementation.
 * Allows RPK to function on Bukkit.
 */
class RPKCoreBukkit : RPKBukkitPlugin() {

    override fun onEnable() {
        System.getProperties().setProperty("org.jooq.no-logo", "true")
        Metrics(this, 4371)
        Services.delegate = BukkitServicesDelegate()
        saveDefaultConfig()
        registerCommands()
        registerListeners()
    }

    override fun registerListeners() {
        registerListeners(PluginEnableListener(this))
    }

    /**
     * Initializes an RPK plugin.
     *
     * @param rpkitBukkitPlugin The plugin to initialize
     */
    fun initializePlugin(rpkitBukkitPlugin: RPKBukkitPlugin) {
        rpkitBukkitPlugin.messages = BukkitMessages(rpkitBukkitPlugin)
        rpkitBukkitPlugin.setDefaultMessages()
        rpkitBukkitPlugin.registerCommands()
        rpkitBukkitPlugin.registerListeners()
    }

}
