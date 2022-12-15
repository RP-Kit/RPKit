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
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.core.service.Services
import com.rpkit.permissions.bukkit.group.hasPermission
import com.rpkit.players.bukkit.profile.RPKProfile
import com.rpkit.players.bukkit.profile.RPKProfileDiscriminator
import com.rpkit.players.bukkit.profile.RPKProfileName
import com.rpkit.players.bukkit.profile.RPKProfileService
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletableFuture.completedFuture
import java.util.concurrent.CompletableFuture.supplyAsync


class ProfileField(private val plugin: RPKCharactersBukkit) : SettableCharacterCardField, HideableCharacterCardField {

    override val name = "profile"
    override fun get(character: RPKCharacter): CompletableFuture<String> {
        return isHidden(character).thenApply { hidden ->
            if (hidden) {
                "[HIDDEN]"
            } else {
                val profile = character.profile ?: return@thenApply "unset"
                profile.name.value
            }
        }
    }

    override fun get(character: RPKCharacter, viewer: RPKProfile): CompletableFuture<String> {
        return isHidden(character).thenApplyAsync { hidden ->
            if (viewer.hasPermission("rpkit.characters.command.character.card.bypasshidden").join() || !hidden) {
                val profile = character.profile ?: return@thenApplyAsync "unset"
                return@thenApplyAsync profile.name.value
            } else {
                return@thenApplyAsync "[HIDDEN]"
            }
        }
    }

    override fun set(character: RPKCharacter, value: String): CompletableFuture<CharacterCardFieldSetResult> {
        val characterService = Services[RPKCharacterService::class.java] ?: return completedFuture(
            CharacterCardFieldSetFailure(plugin.messages.noCharacterService)
        )
        val profileService = Services[RPKProfileService::class.java] ?: return completedFuture(CharacterCardFieldSetFailure(plugin.messages.noProfileService))
        if (!value.contains("#")) {
            return completedFuture(CharacterCardFieldSetFailure(plugin.messages.characterSetProfileInvalidNoDiscriminator))
        }
        val (name, discriminatorString) = value.split("#")
        val discriminator = discriminatorString.toIntOrNull()
        if (discriminator == null) {
            return completedFuture(CharacterCardFieldSetFailure(plugin.messages.characterSetProfileInvalidDiscriminator))
        }
        return supplyAsync {
            val profile = profileService.getProfile(RPKProfileName(name), RPKProfileDiscriminator(discriminator)).join()
                ?: return@supplyAsync CharacterCardFieldSetFailure(plugin.messages.characterSetProfileInvalidProfile)
            character.profile = profile
            characterService.updateCharacter(character).join()
            return@supplyAsync CharacterCardFieldSetSuccess
        }
    }

    override fun isHidden(character: RPKCharacter): CompletableFuture<Boolean> {
        return completedFuture(character.isProfileHidden)
    }

    override fun setHidden(character: RPKCharacter, hidden: Boolean): CompletableFuture<Void> {
        character.isProfileHidden = hidden
        return Services[RPKCharacterService::class.java]?.updateCharacter(character)
            ?.thenApply { null }
            ?: completedFuture(null)
    }

}