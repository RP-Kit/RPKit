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

package com.rpkit.professions.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.professions.bukkit.RPKProfessionsBukkit
import com.rpkit.professions.bukkit.profession.RPKProfession
import org.bukkit.Material
import org.bukkit.entity.Player

class ProfessionsMessages(plugin: RPKProfessionsBukkit) : BukkitMessages(plugin) {

    inner class ProfessionListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(profession: RPKProfession) = message.withParameters(
            "profession" to profession.name.value
        )
    }

    inner class NoMinecraftProfileOtherMessage(private val message: ParameterizedMessage) {
        fun withParameters(player: Player) = message.withParameters(
            "player" to player.name
        )
    }

    inner class NoCharacterOtherMessage(private val message: ParameterizedMessage) {
        fun withParameters(player: RPKMinecraftProfile) = message.withParameters(
            "player" to player.name
        )
    }

    inner class ProfessionSetValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(profession: RPKProfession) = message.withParameters(
            "profession" to profession.name.value
        )
    }

    inner class ProfessionViewValidItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(level: Int, profession: RPKProfession, experience: Int, nextLevelExperience: Int) = message.withParameters(
            "level" to level.toString(),
            "profession" to profession.name.value,
            "experience" to experience.toString(),
            "next_level_experience" to nextLevelExperience.toString()
        )
    }

    inner class ProfessionExperienceSetValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            minecraftProfile: RPKMinecraftProfile,
            character: RPKCharacter,
            profession: RPKProfession,
            totalExperience: Int
        ) = message.withParameters(
            "player" to minecraftProfile.name,
            "character" to character.name,
            "profession" to profession.name.value,
            "total_experience" to totalExperience.toString()
        )
    }

    inner class ProfessionExperienceViewValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            profession: RPKProfession,
            experience: Int,
            nextLevelExperience: Int,
            level: Int,
            totalExperience: Int
        ) = message.withParameters(
            "profession" to profession.name.value,
            "experience" to experience.toString(),
            "next_level_experience" to nextLevelExperience.toString(),
            "level" to level.toString(),
            "total_experience" to totalExperience.toString()
        )
    }

    inner class MineExperienceMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            receivedExperience: Int,
            profession: RPKProfession,
            level: Int,
            experience: Int,
            nextLevelExperience: Int,
            totalExperience: Int,
            totalNextLevelExperience: Int,
            material: Material
        ) = message.withParameters(
            "received_experience" to receivedExperience.toString(),
            "profession" to profession.name.value,
            "level" to level.toString(),
            "experience" to experience.toString(),
            "next_level_experience" to nextLevelExperience.toString(),
            "total_experience" to totalExperience.toString(),
            "total_next_level_experience" to totalNextLevelExperience.toString(),
            "material" to material.toString().toLowerCase().replace('_', ' ')
        )
    }

    inner class CraftExperienceMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            receivedExperience: Int,
            profession: RPKProfession,
            level: Int,
            experience: Int,
            nextLevelExperience: Int,
            totalExperience: Int,
            totalNextLevelExperience: Int,
            material: Material
        ) = message.withParameters(
            "received_experience" to receivedExperience.toString(),
            "profession" to profession.name.value,
            "level" to level.toString(),
            "experience" to experience.toString(),
            "next_level_experience" to nextLevelExperience.toString(),
            "total_experience" to totalExperience.toString(),
            "total_next_level_experience" to totalNextLevelExperience.toString(),
            "material" to material.toString().toLowerCase().replace('_', ' ')
        )
    }

    inner class SmeltExperienceMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            receivedExperience: Int,
            profession: RPKProfession,
            level: Int,
            experience: Int,
            nextLevelExperience: Int,
            totalExperience: Int,
            totalNextLevelExperience: Int,
            material: Material
        ) = message.withParameters(
            "received_experience" to receivedExperience.toString(),
            "profession" to profession.name.value,
            "level" to level.toString(),
            "experience" to experience.toString(),
            "next_level_experience" to nextLevelExperience.toString(),
            "total_experience" to totalExperience.toString(),
            "total_next_level_experience" to totalNextLevelExperience.toString(),
            "material" to material.toString().toLowerCase().replace('_', ' ')
        )
    }

    val professionUsage = get("profession-usage")
    val professionExperienceUsage = get("profession-experience-usage")
    val noPermissionProfessionList = get("no-permission-profession-list")
    val professionListTitle = get("profession-list-title")
    val professionListItem = getParameterized("profession-list-item")
        .let(::ProfessionListItemMessage)
    val noPermissionProfessionSet = get("no-permission-profession-set")
    val professionSetInvalidPlayerNotOnline = get("profession-set-invalid-player-not-online")
    val professionSetInvalidPlayerPleaseSpecifyFromConsole = get("profession-set-invalid-player-please-specify-from-console")
    val professionSetUsage = get("profession-set-usage")
    val noMinecraftProfileSelf = get("no-minecraft-profile-self")
    val noMinecraftProfileOther = getParameterized("no-minecraft-profile-other")
        .let(::NoMinecraftProfileOtherMessage)
    val noCharacterSelf = get("no-character-self")
    val noCharacterOther = getParameterized("no-character-other")
        .let(::NoCharacterOtherMessage)
    val noPreloadedProfessions = get("no-preloaded-professions")
    val professionSetInvalidProfession = get("profession-set-invalid-profession")
    val professionSetInvalidAlreadyUsingProfession = get("profession-set-invalid-already-using-profession")
    val professionSetInvalidTooManyProfessions = get("profession-set-invalid-too-many-professions")
    val professionSetValid = getParameterized("profession-set-valid")
        .let(::ProfessionSetValidMessage)
    val noPermissionProfessionUnset = get("no-permission-profession-unset")
    val professionUnsetInvalidPlayerNotOnline = get("profession-unset-invalid-player-not-online")
    val professionUnsetInvalidPlayerPleaseSpecifyFromConsole = get("profession-unset-invalid-player-please-specify-from-console")
    val professionUnsetUsage = get("profession-unset-usage")
    val professionUnsetInvalidProfession = get("profession-unset-invalid-profession")
    val professionUnsetInvalidNotUsingProfession = get("profession-unset-invalid-not-using-profession")
    val professionUnsetInvalidOnCooldown = get("profession-unset-invalid-on-cooldown")
    val professionUnsetValid = get("profession-unset-valid")
    val noPermissionProfessionView = get("no-permission-profession-view")
    val professionViewInvalidPlayerNotOnline = get("profession-view-invalid-player-not-online")
    val professionViewInvalidPlayerPleaseSpecifyFromConsole = get("profession-view-invalid-player-please-specify-from-console")
    val professionViewValidTitle = get("profession-view-valid-title")
    val professionViewValidItem = getParameterized("profession-view-valid-item")
        .let(::ProfessionViewValidItemMessage)
    val noPermissionProfessionExperienceAdd = get("no-permission-profession-experience-add")
    val professionExperienceAddInvalidPlayerNotOnline = get("profession-experience-add-invalid-player-not-online")
    val professionExperienceAddInvalidPlayerPleaseSpecifyFromConsole = get("profession-experience-add-invalid-player-please-specify-from-console")
    val professionExperienceSetUsage = get("profession-experience-set-usage")
    val professionExperienceSetInvalidExpNotANumber = get("profession-experience-set-invalid-exp-not-a-number")
    val professionExperienceSetInvalidProfession = get("profession-experience-set-invalid-profession")
    val professionExperienceSetValid = getParameterized("profession-experience-set-valid")
        .let(::ProfessionExperienceSetValidMessage)
    val noPermissionProfessionExperienceView = get("no-permission-profession-experience-view")
    val professionExperienceViewInvalidPlayerNotOnline = get("profession-experience-view-invalid-player-not-online")
    val professionExperienceViewInvalidPlayerPleaseSpecifyFromConsole = get("profession-experience-view-invalid-player-please-specify-from-console")
    val professionExperienceViewUsage = get("profession-experience-view-usage")
    val professionExperienceViewInvalidProfession = get("profession-experience-view-invalid-profession")
    val professionExperienceViewValid = getParameterized("profession-experience-view-valid")
        .let(::ProfessionExperienceViewValidMessage)
    val mineExperience = getParameterized("mine-experience")
        .let(::MineExperienceMessage)
    val craftExperience = getParameterized("craft-experience")
        .let(::CraftExperienceMessage)
    val smeltExperience = getParameterized("smelt-experience")
        .let(::SmeltExperienceMessage)
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noProfessionService = get("no-profession-service")

}