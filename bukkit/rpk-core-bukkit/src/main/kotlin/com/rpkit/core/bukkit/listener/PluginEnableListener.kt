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

package com.rpkit.core.bukkit.listener

import com.rpkit.core.bukkit.RPKCoreBukkit
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginEnableEvent

/**
 * Plugin enable listener for registering RPK plugins
 */
class PluginEnableListener(private val plugin: RPKCoreBukkit) : Listener {

    @EventHandler
    fun onPluginEnable(event: PluginEnableEvent) {
        if (event.plugin !== plugin) {
            if (event.plugin is RPKBukkitPlugin) {
                val rpkitBukkitPlugin = event.plugin as RPKBukkitPlugin
                plugin.initializePlugin(rpkitBukkitPlugin)
            }
        }
    }

}
