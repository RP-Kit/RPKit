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

package com.rpkit.chat.bukkit

import com.rpkit.chat.bukkit.vault.RPKChatVaultChat
import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import net.milkbowl.vault.chat.Chat
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.ServicePriority.Normal

/**
 * Class to allow chat lib to load as a plugin.
 * This allows plugins requiring chat or implementing chat to depend on the plugin.
 * With this plugin loaded, a Vault chat service is added for chat plugins on [ServicePriority.Normal].
 * If chat plugins wish to provide their own chat service, they should register on [ServicePriority.High]
 * or [ServicePriority.Highest].
 */
class RPKChatLibBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        serviceProviders = arrayOf()
        if (server.pluginManager.getPlugin("Vault") != null) {
            server.servicesManager.register(Chat::class.java, RPKChatVaultChat(this), this, Normal)
        }
    }

}