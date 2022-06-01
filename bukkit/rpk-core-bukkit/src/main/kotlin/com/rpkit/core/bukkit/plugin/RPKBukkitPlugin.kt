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

package com.rpkit.core.bukkit.plugin

import com.rpkit.core.plugin.RPKPlugin
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

/**
 * Represents an RPK plugin.
 * Provides convenience methods for registering listeners, commands, database tables, and provides service services and
 * servlets.
 */
@Deprecated("Extend JavaPlugin as normal and implement RPKPlugin where necessary. This class is no longer required.")
abstract class RPKBukkitPlugin : JavaPlugin(), RPKPlugin {

    @Deprecated(message = "Replace with extension, stop extending from RPKBukkitPlugin", replaceWith = ReplaceWith("registerListeners", "com.rpkit.core.bukkit.listener.registerListeners"))
    fun registerListeners(vararg listeners: Listener) {
        for (listener in listeners) {
            server.pluginManager.registerEvents(listener, this)
        }
    }

}
