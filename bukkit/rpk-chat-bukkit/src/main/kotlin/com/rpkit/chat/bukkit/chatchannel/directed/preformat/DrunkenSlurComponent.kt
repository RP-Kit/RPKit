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
import java.util.concurrent.CompletableFuture

@SerializableAs("DrunkenSlurComponent")
class DrunkenSlurComponent(val drunkenness: Int) : DirectedPreFormatPipelineComponent, ConfigurationSerializable {

    override fun process(context: DirectedPreFormatMessageContext): CompletableFuture<DirectedPreFormatMessageContext> {
        val minecraftProfile = context.senderMinecraftProfile ?: return CompletableFuture.completedFuture(context)
        val characterService = Services[RPKCharacterService::class.java] ?: return CompletableFuture.completedFuture(context)
        val drinkService = Services[RPKDrinkService::class.java] ?: return CompletableFuture.completedFuture(context)
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return CompletableFuture.completedFuture(context)
        return CompletableFuture.supplyAsync {
            if (drinkService.getDrunkenness(character).join() >= drunkenness) {
                context.message = context.message.replace(Regex("s([^h])"), "sh$1")
            }
            return@supplyAsync context
        }
    }

    override fun serialize(): MutableMap<String, Any> {
        return mutableMapOf(
            "drunkenness" to drunkenness
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