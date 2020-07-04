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

package com.rpkit.chat.bukkit.chatchannel.directed.preformat

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.chat.bukkit.RPKChatBukkit
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPreFormatPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import com.rpkit.core.exception.UnregisteredServiceException
import com.rpkit.drink.bukkit.drink.RPKDrinkProvider
import org.bukkit.Bukkit
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs
import java.util.logging.Level

@SerializableAs("DrunkenSlurComponent")
class DrunkenSlurComponent(private val plugin: RPKChatBukkit, val drunkenness: Int): DirectedPreFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPreFormatMessageContext): DirectedPreFormatMessageContext {
        val minecraftProfile = context.senderMinecraftProfile ?: return context
        try {
            val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
            val drinkProvider = plugin.core.serviceManager.getServiceProvider(RPKDrinkProvider::class)
            val character = characterProvider.getActiveCharacter(minecraftProfile) ?: return context
            if (drinkProvider.getDrunkenness(character) >= drunkenness) {
                context.message = context.message.replace(Regex("s([^h])"), "sh$1")
            }
        } catch (exception: UnregisteredServiceException) {
            plugin.logger.log(Level.SEVERE, "Failed to retrieve drink provider. Is a plugin with drinks functionality installed?", exception)
        }
        return context
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
                Pair("drunkenness", drunkenness)
        )
    }

    companion object {
        @JvmStatic
        fun deserialize(serialized: MutableMap<String, Any>): DrunkenSlurComponent {
            return DrunkenSlurComponent(
                    Bukkit.getPluginManager().getPlugin("rpk-chat-bukkit") as RPKChatBukkit,
                    serialized["drunkenness"] as Int
            )
        }
    }

}