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

package com.rpkit.characters.bukkit.character.field

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import me.clip.placeholderapi.PlaceholderAPI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture

class PlaceholderAPIField(private val plugin: RPKCharactersBukkit, override val name: String, private val format: String) : CharacterCardField {
    override fun get(character: RPKCharacter): CompletableFuture<String> {
        val minecraftProfile = character.minecraftProfile
        val minecraftUuid = minecraftProfile?.minecraftUUID ?: return completedFuture("")
        val player = plugin.server.getOfflinePlayer(minecraftUuid)
        val text = PlaceholderAPI.setPlaceholders(player, format)
        return completedFuture(text)
    }
}