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

package com.rpkit.classes.bukkit.placeholder

import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.classes.bukkit.RPKClassesBukkit
import com.rpkit.classes.bukkit.classes.RPKClassService
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfileService
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player

class RPKClassesPlaceholderExpansion(private val plugin: RPKClassesBukkit) : PlaceholderExpansion() {

    override fun persist() = true
    override fun canRegister() = true
    override fun getIdentifier() = "rpkclasses"
    override fun getAuthor() = plugin.description.authors.joinToString()
    override fun getVersion() = plugin.description.version

    override fun onPlaceholderRequest(player: Player, params: String): String? {
        val minecraftProfileService = Services[RPKMinecraftProfileService::class.java] ?: return null
        val characterService = Services[RPKCharacterService::class.java] ?: return null
        val classesService = Services[RPKClassService::class.java] ?: return null
        val minecraftProfile = player.let { minecraftProfileService.getPreloadedMinecraftProfile(it) }
        val character = minecraftProfile?.let { characterService.getPreloadedActiveCharacter(it) }
        val `class` = character?.let { classesService.getPreloadedClass(it) }
        val classLevel = if (character != null && `class` != null) { classesService.getPreloadedLevel(character, `class`) } else null
        val classExperience = if (character != null && `class` != null) { classesService.getPreloadedExperience(character, `class`) } else null
        return when (params.lowercase()) {
            "class" -> `class`?.name?.value
            "classlevel" -> classLevel?.toString()
            "classexperience" -> classExperience?.toString()
            else -> null
        }
    }

}