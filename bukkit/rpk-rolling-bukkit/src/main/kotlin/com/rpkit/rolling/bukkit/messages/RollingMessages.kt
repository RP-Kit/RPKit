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

package com.rpkit.rolling.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.players.bukkit.profile.minecraft.RPKMinecraftProfile
import com.rpkit.rolling.bukkit.RPKRollingBukkit

class RollingMessages(plugin: RPKRollingBukkit) : BukkitMessages(plugin) {
    class RollMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            character: RPKCharacter,
            player: RPKMinecraftProfile,
            roll: String,
            dice: String
        ) = message.withParameters(
            "character" to character.name,
            "player" to player.name,
            "roll" to roll,
            "dice" to dice
        )
    }

    class PrivateRollMessage(private val message: ParameterizedMessage) {
        fun withParameters(
            roll: String,
            dice: String
        ) = message.withParameters(
            "roll" to roll,
            "dice" to dice
        )
    }

    val roll = getParameterized("roll")
        .let(::RollMessage)
    val rollInvalidParse = get("roll-invalid-parse")
    val rollUsage = get("roll-usage")
    val privateRoll = getParameterized("private-roll")
        .let(::PrivateRollMessage)
    val privateRollUsage = get("private-roll-usage")
    val notFromConsole = get("not-from-console")
    val noCharacter = get("no-character")
    val noMinecraftProfile = get("no-minecraft-profile")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService=  get("no-character-service")
}