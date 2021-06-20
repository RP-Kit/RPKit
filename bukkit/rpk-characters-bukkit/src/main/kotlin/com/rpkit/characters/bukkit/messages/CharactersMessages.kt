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

package com.rpkit.characters.bukkit.messages

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.characters.bukkit.race.RPKRace
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.players.bukkit.profile.RPKProfile

class CharactersMessages(plugin: RPKCharactersBukkit) : BukkitMessages(plugin) {

    class CharacterCardOwnerMessage(val messages: List<ParameterizedMessage>) {
        fun withParameters(
            name: String,
            profile: RPKProfile,
            gender: String,
            age: Int,
            race: RPKRace,
            description: String,
            dead: Boolean,
            health: Double,
            maxHealth: Double,
            food: Int,
            maxFood: Int,
            thirst: Int,
            maxThirst: Int
        ) = messages.map { message ->
            message.withParameters(
                "name" to name,
                "profile" to profile.name + profile.discriminator,
                "gender" to gender,
                "age" to age.toString(),
                "race" to race.name.value,
                "description" to description,
                "dead" to if (dead) "Yes" else "No",
                "health" to health.toString(),
                "max_health" to maxHealth.toString(),
                "food" to food.toString(),
                "max_food" to maxFood.toString(),
                "thirst" to thirst.toString(),
                "max_thirst" to maxThirst.toString()
            )
        }
    }

    class CharacterCardNotOwnerMessage(val messages: List<ParameterizedMessage>) {
        fun withParameters(
            name: String,
            profile: RPKProfile,
            gender: String,
            age: Int,
            race: RPKRace,
            description: String,
            dead: Boolean,
            health: Double,
            maxHealth: Double,
            food: Int,
            maxFood: Int,
            thirst: Int,
            maxThirst: Int
        ) = messages.map { message ->
            message.withParameters(
                "name" to name,
                "profile" to profile.name + profile.discriminator,
                "gender" to gender,
                "age" to age.toString(),
                "race" to race.name.value,
                "description" to description,
                "dead" to if (dead) "Yes" else "No",
                "health" to health.toString(),
                "max_health" to maxHealth.toString(),
                "food" to food.toString(),
                "max_food" to maxFood.toString(),
                "thirst" to thirst.toString(),
                "max_thirst" to maxThirst.toString()
            )
        }
    }

    class CharacterListItem(private val message: ParameterizedMessage) {
        fun withParameters(character: RPKCharacter) = message.withParameters(
            "character" to character.name
        )
    }

    class RaceListItem(private val message: ParameterizedMessage) {
        fun withParameters(race: RPKRace) = message.withParameters(
            "race" to race.name.value
        )
    }

    val characterUsage = get("character-usage")
    val characterSetUsage = get("character-set-usage")
    val characterSetAgePrompt = get("character-set-age-prompt")
    val characterSetAgeInvalidValidation = get("character-set-age-invalid-validation")
    val characterSetAgeInvalidNumber = get("character-set-age-invalid-number")
    val characterSetAgeValid = get("character-set-age-valid")
    val characterSetDeadPrompt = get("character-set-dead-prompt")
    val characterSetDeadInvalidBoolean = get("character-set-dead-invalid-boolean")
    val characterSetDeadValid = get("character-set-dead-valid")
    val characterSetProfilePrompt = get("character-set-profile-prompt")
    val characterSetProfileInvalidNoDiscriminator = get("character-set-profile-invalid-no-discriminator")
    val characterSetProfileInvalidDiscriminator = get("character-set-profile-invalid-discriminator")
    val characterSetProfileValid = get("character-set-profile-valid")
    val characterSetGenderPrompt = get("character-set-gender-prompt")
    val characterSetGenderNotSet = get("character-set-gender-not-set")
    val characterSetGenderValid = get("character-set-gender-valid")
    val characterSetRacePrompt = get("character-set-race-prompt")
    val characterSetRaceInvalidRace = get("character-set-race-invalid-race")
    val characterSetRaceValid = get("character-set-race-valid")
    val characterHideUsage = get("character-hide-usage")
    val characterHideAgeValid = get("character-hide-age-valid")
    val characterHideDescriptionValid = get("character-hide-description-valid")
    val characterHideProfileValid = get("character-hide-profile-valid")
    val characterHideNameValid = get("character-hide-name-valid")
    val characterHideGenderValid = get("character-hide-gender-valid")
    val characterHideRaceValid = get("character-hide-race-valid")
    val characterUnhideUsage = get("character-unhide-usage")
    val characterUnhideAgeValid = get("character-unhide-age-valid")
    val characterUnhideDescriptionValid = get("character-unhide-description-valid")
    val characterUnhideProfileValid = get("character-unhide-profile-valid")
    val characterUnhideNameValid = get("character-unhide-name-valid")
    val characterUnhideGenderValid = get("character-unhide-gender-valid")
    val characterUnhideRaceValid = get("character-unhide-race-valid")
    val characterCardOwner = getParameterizedList("character-card-owner").let(::CharacterCardOwnerMessage)
    val characterCardNotOwner = getParameterizedList("character-card-not-owner").let(::CharacterCardNotOwnerMessage)
    val characterListTitle = get("character-list-title")
    val characterListItem = getParameterized("character-list-item").let(::CharacterListItem)
    val characterSwitchPrompt = get("character-switch-prompt")
    val characterSwitchInvalidCharacter = get("character-switch-invalid-character")
    val characterSwitchInvalidCharacterOtherAccount = get("character-switch-invalid-character-other-account")
    val characterSwitchValid = get("character-switch-valid")
    val characterSwitchUsage = get("character-switch-usage")
    val characterNewValid = get("character-new-valid")
    val characterNewInvalidCooldown = get("character-new-invalid-cooldown")
    val characterDeletePrompt = get("character-delete-prompt")
    val characterDeleteInvalidCharacter = get("character-delete-invalid-character")
    val characterDeleteConfirmation = get("character-delete-confirmation")
    val characterDeleteConfirmationInvalidBoolean = get("character-delete-confirmation-invalid-boolean")
    val characterDeleteValid = get("character-delete-valid")
    val characterDeleteUsage = get("character-delete-usage")
    val raceUsage = get("race-usage")
    val raceAddPrompt = get("race-add-prompt")
    val raceAddInvalidRace = get("race-add-invalid-race")
    val raceAddValid = get("race-add-valid")
    val raceRemovePrompt = get("race-remove-prompt")
    val raceRemoveInvalidRace = get("race-remove-invalid-race")
    val raceRemoveValid = get("race-remove-valid")
    val raceListTitle = get("race-list-title")
    val raceListItem = getParameterized("race-list-item").let(::RaceListItem)
    val notFromConsole = get("not-from-console")
    val operationCancelled = get("operation-cancelled")
    val noCharacter = get("no-character")
    val noCharacterOther = get("no-character-other")
    val noProfile = get("no-profile")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noPermissionCharacterCardSelf = get("no-permission-character-card-self")
    val noPermissionCharacterCardOther = get("no-permission-character-card-other")
    val noPermissionCharacterList = get("no-permission-character-list")
    val noPermissionCharacterNew = get("no-permission-character-new")
    val noPermissionCharacterSetAge = get("no-permission-character-set-age")
    val noPermissionCharacterSetDead = get("no-permission-character-set-dead")
    val noPermissionCharacterSetDeadYes = get("no-permission-character-set-dead-yes")
    val noPermissionCharacterSetDeadNo = get("no-permission-character-set-dead-no")
    val noPermissionCharacterSetDescription = get("no-permission-character-set-description")
    val noPermissionCharacterSetGender = get("no-permission-character-set-gender")
    val noPermissionCharacterSetName = get("no-permission-character-set-name")
    val noPermissionCharacterSetRace = get("no-permission-character-set-race")
    val noPermissionCharacterHideAge = get("no-permission-character-hide-age")
    val noPermissionCharacterHideDescription = get("no-permission-character-hide-description")
    val noPermissionCharacterHideGender = get("no-permission-character-hide-gender")
    val noPermissionCharacterHideName = get("no-permission-character-hide-name")
    val noPermissionCharacterHideProfile = get("no-permission-character-hide-profile")
    val noPermissionCharacterHideRace = get("no-permission-character-hide-race")
    val noPermissionCharacterUnhideAge = get("no-permission-character-unhide-age")
    val noPermissionCharacterUnhideDescription = get("no-permission-character-unhide-description")
    val noPermissionCharacterUnhideGender = get("no-permission-character-unhide-gender")
    val noPermissionCharacterUnhideName = get("no-permission-character-unhide-name")
    val noPermissionCharacterUnhideProfile = get("no-permission-character-unhide-profile")
    val noPermissionCharacterUnhideRace = get("no-permission-character-unhide-race")
    val noPermissionCharacterSwitch = get("no-permission-character-switch")
    val noPermissionCharacterDelete = get("no-permission-character-delete")
    val noPermissionRaceAdd = get("no-permission-race-add")
    val noPermissionRaceRemove = get("no-permission-race-remove")
    val noPermissionRaceList = get("no-permission-race-list")
    val deadCharacter = get("dead-character")
    val noProfileService = get("no-profile-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noCharacterCardFieldService = get("no-character-card-field-service")
    val noNewCharacterCooldownService = get("no-new-character-cooldown-service")
    val noRaceService = get("no-race-service")
}