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

package com.rpkit.characters.bukkit.web.character

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.character.RPKCharacterId
import com.rpkit.characters.bukkit.character.RPKCharacterService
import com.rpkit.characters.bukkit.protocol.reloadPlayer
import com.rpkit.characters.bukkit.race.RPKRaceName
import com.rpkit.characters.bukkit.species.RPKSpeciesName
import com.rpkit.characters.bukkit.species.RPKSpeciesService
import com.rpkit.characters.bukkit.web.ErrorResponse
import com.rpkit.characters.bukkit.web.authenticatedProfile
import com.rpkit.core.service.Services
import com.rpkit.players.bukkit.profile.RPKProfileId
import com.rpkit.players.bukkit.profile.RPKProfileService
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status.Companion.FORBIDDEN
import org.http4k.core.Status.Companion.INTERNAL_SERVER_ERROR
import org.http4k.core.Status.Companion.NOT_FOUND
import org.http4k.core.Status.Companion.NO_CONTENT
import org.http4k.core.Status.Companion.OK
import org.http4k.core.with
import org.http4k.lens.Path
import org.http4k.lens.Query
import org.http4k.lens.int

class CharacterHandler(private val plugin: RPKCharactersBukkit) {

    val idLens = Path.int().of("id")

    val profileIdLens = Query.int().required("profileId")

    fun get(request: Request): Response {
        val id = idLens(request)
        val characterService = Services[RPKCharacterService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Character service not found"))
        val character = characterService.getCharacter(RPKCharacterId(id)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Character not found"))
        return Response(OK)
            .with(CharacterResponse.lens of character.toCharacterResponse())
    }

    fun put(request: Request): Response {
        val id = idLens(request)
        val characterPutRequest = CharacterPutRequest.lens(request)
        val characterService = Services[RPKCharacterService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Character service not found"))
        val speciesService = Services[RPKSpeciesService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Species service not found"))
        val character = characterService.getCharacter(RPKCharacterId(id)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Character not found"))
        if (character.profile?.id != request.authenticatedProfile?.id) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You can not edit characters you do not own"))
        }
        character.name = characterPutRequest.name
        character.gender = characterPutRequest.gender
        character.age = characterPutRequest.age
        character.species = characterPutRequest.species?.let(::RPKSpeciesName)?.let(speciesService::getSpecies) ?: characterPutRequest.race?.let(::RPKRaceName)?.let(speciesService::getRace)
        character.description = characterPutRequest.description
        character.isDead = characterPutRequest.isDead
        character.isProfileHidden = characterPutRequest.isProfileHidden
        character.isNameHidden = characterPutRequest.isNameHidden
        character.isGenderHidden = characterPutRequest.isGenderHidden
        character.isAgeHidden = characterPutRequest.isAgeHidden
        character.isSpeciesHidden = characterPutRequest.isSpeciesHidden ?: characterPutRequest.isRaceHidden ?: plugin.config.getBoolean("characters.defaults.species-hidden")
        character.isDescriptionHidden = characterPutRequest.isDescriptionHidden
        characterService.updateCharacter(character).join()
        if (plugin.config.getBoolean("characters.set-player-nameplate")
            && plugin.server.pluginManager.getPlugin("ProtocolLib") != null) {
            val minecraftProfile = character.minecraftProfile
            if (minecraftProfile?.isOnline == true) {
                val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
                if (bukkitPlayer != null) {
                    reloadPlayer(bukkitPlayer, character, plugin.server.onlinePlayers.filter { it.uniqueId != bukkitPlayer.uniqueId })
                }
            }
        }
        return Response(NO_CONTENT)
    }

    fun patch(request: Request): Response {
        val id = idLens(request)
        val characterPatchRequest = CharacterPatchRequest.lens(request)
        val characterService = Services[RPKCharacterService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Character service not found"))
        val speciesService = Services[RPKSpeciesService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Species service not found"))
        val character = characterService.getCharacter(RPKCharacterId(id)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Character not found"))
        if (character.profile?.id != request.authenticatedProfile?.id) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You can not edit characters you do not own"))
        }
        character.name = characterPatchRequest.name ?: character.name
        character.gender = characterPatchRequest.gender ?: character.gender
        character.age = characterPatchRequest.age ?: character.age
        character.species = characterPatchRequest.species?.let(::RPKSpeciesName)?.let(speciesService::getSpecies) ?: character.species
        character.description = characterPatchRequest.description ?: character.description
        character.isDead = characterPatchRequest.isDead ?: character.isDead
        character.isProfileHidden = characterPatchRequest.isProfileHidden ?: character.isProfileHidden
        character.isNameHidden = characterPatchRequest.isNameHidden ?: character.isNameHidden
        character.isGenderHidden = characterPatchRequest.isGenderHidden ?: character.isGenderHidden
        character.isAgeHidden = characterPatchRequest.isAgeHidden ?: character.isAgeHidden
        character.isSpeciesHidden = characterPatchRequest.isSpeciesHidden ?: characterPatchRequest.isRaceHidden ?: character.isSpeciesHidden
        character.isDescriptionHidden = characterPatchRequest.isDescriptionHidden ?: character.isDescriptionHidden
        characterService.updateCharacter(character).join()
        if (plugin.config.getBoolean("characters.set-player-nameplate")
            && plugin.server.pluginManager.getPlugin("ProtocolLib") != null) {
            val minecraftProfile = character.minecraftProfile
            if (minecraftProfile?.isOnline == true) {
                val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
                if (bukkitPlayer != null) {
                    reloadPlayer(bukkitPlayer, character, plugin.server.onlinePlayers.filter { it.uniqueId != bukkitPlayer.uniqueId })
                }
            }
        }
        return Response(NO_CONTENT)
    }

    fun delete(request: Request): Response {
        val id = idLens(request)
        val characterService = Services[RPKCharacterService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Character service not found"))
        val character = characterService.getCharacter(RPKCharacterId(id)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Character not found"))
        if (character.profile?.id != request.authenticatedProfile?.id) {
            return Response(FORBIDDEN)
                .with(ErrorResponse.lens of ErrorResponse("You can not delete characters you do not own"))
        }
        characterService.removeCharacter(character).join()
        return Response(NO_CONTENT)
    }

    fun post(request: Request): Response {
        val characterPostRequest = CharacterPostRequest.lens(request)
        val characterService = Services[RPKCharacterService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Character service not found"))
        val speciesService = Services[RPKSpeciesService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Species service not found"))
        val character = characterService.createCharacter(
            request.authenticatedProfile,
            characterPostRequest.name,
            characterPostRequest.gender,
            characterPostRequest.age,
            characterPostRequest.species?.let(::RPKSpeciesName)?.let(speciesService::getSpecies)
                ?: characterPostRequest.race?.let(::RPKRaceName)?.let(speciesService::getRace),
            characterPostRequest.description,
            characterPostRequest.isDead,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            characterPostRequest.isProfileHidden,
            characterPostRequest.isNameHidden,
            characterPostRequest.isGenderHidden,
            characterPostRequest.isAgeHidden,
            characterPostRequest.isRaceHidden,
            characterPostRequest.isDescriptionHidden
        ).join()
        return Response(OK)
            .with(CharacterResponse.lens of character.toCharacterResponse())
    }

    fun list(request: Request): Response {
        val profileId = profileIdLens(request)
        val profileService = Services[RPKProfileService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Profile service not found"))
        val characterService = Services[RPKCharacterService::class.java]
            ?: return Response(INTERNAL_SERVER_ERROR)
                .with(ErrorResponse.lens of ErrorResponse("Character service not found"))
        val profile = profileService.getProfile(RPKProfileId(profileId)).join()
            ?: return Response(NOT_FOUND)
                .with(ErrorResponse.lens of ErrorResponse("Profile not found"))
        val characters = characterService.getCharacters(profile).join()
        return Response(OK)
            .with(CharacterResponse.listLens of characters.map(RPKCharacter::toCharacterResponse))
    }

}