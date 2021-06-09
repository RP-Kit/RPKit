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

package com.rpkit.monsters.bukkit.messages

import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.monsters.bukkit.RPKMonstersBukkit
import org.bukkit.World

class MonstersMessages(plugin: RPKMonstersBukkit) : BukkitMessages(plugin) {

    inner class MonsterSpawnAreaCreateValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            world: World,
            minX: Int,
            minY: Int,
            minZ: Int,
            maxX: Int,
            maxY: Int,
            maxZ: Int
        ) = message.withParameters(
            "world" to world.name,
            "min_x" to minX.toString(),
            "min_y" to minY.toString(),
            "min_z" to minZ.toString(),
            "max_x" to maxX.toString(),
            "max_y" to maxY.toString(),
            "max_z" to maxZ.toString()
        )
    }

    inner class ExperienceGainedMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            experienceGained: Int,
            experience: Int,
            requiredExperience: Int
        ) = message.withParameters(
            "experience_gained" to experienceGained.toString(),
            "experience" to experience.toString(),
            "required_experience" to requiredExperience.toString()
        )
    }

    val monsterSpawnAreaUsage = get("monster-spawn-area-usage")
    val notFromConsole = get("not-from-console")
    val monsterSpawnAreaInvalidArea = get("monster-spawn-area-invalid-area")
    val noPermissionMonsterSpawnAreaAddMonster = get("no-permission-monster-spawn-area-add-monster")
    val monsterSpawnAreaAddMonsterUsage = get("monster-spawn-area-add-monster-usage")
    val monsterSpawnAreaAddMonsterInvalidMonsterType = get("monster-spawn-area-add-monster-invalid-monster-type")
    val monsterSpawnAreaAddMonsterInvalidMinLevel = get("monster-spawn-area-add-monster-invalid-min-level")
    val monsterSpawnAreaAddMonsterInvalidMaxLevel = get("monster-spawn-area-add-monster-invalid-max-level")
    val monsterSpawnAreaAddMonsterInvalidArea = get("monster-spawn-area-add-monster-invalid-area")
    val monsterSpawnAreaAddMonsterValid = get("monster-spawn-area-add-monster-valid")
    val noMinecraftProfileSelf = get("no-minecraft-profile-self")
    val noSelection = get("no-selection")
    val noPermissionMonsterSpawnAreaCreate = get("no-permission-monster-spawn-area-create")
    val monsterSpawnAreaCreateValid = getParameterized("monster-spawn-area-create-valid").let(::MonsterSpawnAreaCreateValidMessage)
    val noPermissionMonsterSpawnAreaDelete = get("no-permission-monster-spawn-area-delete")
    val monsterSpawnAreaDeleteInvalidArea = get("monster-spawn-area-delete-invalid-area")
    val monsterSpawnAreaDeleteValid = get("monster-spawn-area-delete-valid")
    val noPermissionMonsterSpawnAreaRemoveMonster = get("no-permission-monster-spawn-area-remove-monster")
    val monsterSpawnAreaRemoveMonsterUsage = get("monster-spawn-area-remove-monster-usage")
    val monsterSpawnAreaRemoveMonsterInvalidMonsterType = get("monster-spawn-area-remove-monster-invalid-monster-type")
    val monsterSpawnAreaRemoveMonsterInvalidArea = get("monster-spawn-area-remove-monster-invalid-area")
    val monsterSpawnAreaRemoveMonsterValid = get("monster-spawn-area-remove-monster-valid")
    val experienceGained = getParameterized("experience-gained").let(::ExperienceGainedMessage)
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noSelectionService = get("no-selection-service")
    val noMonsterSpawnAreaService = get("no-monster-spawn-area-service")

}