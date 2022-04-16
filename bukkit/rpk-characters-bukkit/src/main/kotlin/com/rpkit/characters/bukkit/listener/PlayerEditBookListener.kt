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

package com.rpkit.characters.bukkit.listener

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerEditBookEvent

class PlayerEditBookListener(private val plugin: RPKCharactersBukkit) : Listener {

    @EventHandler
    fun onPlayerEditBook(event: PlayerEditBookEvent) {
        if (!event.isSigning) return
        if (!plugin.config.getBoolean("books.change-author-on-sign")) return
        val meta = event.newBookMeta
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return
        val characterService = Services[RPKCharacterService::class.java] ?: return
        val minecraftProfile = minecraftProfileService.getPreloadedMinecraftProfile(event.player) ?: return
        val character = characterService.getPreloadedActiveCharacter(minecraftProfile) ?: return
        var newAuthor = plugin.config.getString("books.author-format") ?: "\${player}"
        newAuthor = ChatColor.translateAlternateColorCodes('&', newAuthor)
        newAuthor = newAuthor.replace("\${player}", minecraftProfile.name)
        newAuthor = newAuthor.replace("\${character}", character.name)
        meta.author = newAuthor
        event.newBookMeta = meta
    }

}