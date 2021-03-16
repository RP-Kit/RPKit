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

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.chat.bukkit.chatchannel.pipeline.DirectedPreFormatPipelineComponent
import com.rpkit.chat.bukkit.context.DirectedPreFormatMessageContext
import com.rpkit.core.service.Services
import com.rpkit.drink.bukkit.drink.RPKDrinkService
import org.bukkit.configuration.serialization.ConfigurationSerializable
import org.bukkit.configuration.serialization.SerializableAs

@SerializableAs("DrunkenSlurComponent")
class DrunkenSlurComponent(val drunkenness: Int) : DirectedPreFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPreFormatMessageContext): DirectedPreFormatMessageContext {
        val minecraftProfile = context.senderMinecraftProfile ?: return context
        val characterService = Services[RPKCharacterService::class.java] ?: return context
        val drinkService = Services[RPKDrinkService::class.java] ?: return context
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return context
        if (drinkService.getDrunkenness(character) >= drunkenness) {
            context.message = context.message.replace(Regex("s([^h])"), "sh$1")
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
                    serialized["drunkenness"] as Int
            )
        }
    }

}