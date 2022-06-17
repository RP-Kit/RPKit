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
    val turnOrderUsage = get("turn-order-usage")
    val turnOrderAddUsage = get("turn-order-add-usage")
    val turnOrderAddInvalidTurnOrder = get("turn-order-add-invalid-turn-order")
    val turnOrderAddValid = get("turn-order-add-valid")
    val turnOrderAdvanceInvalidTurnOrder = get("turn-order-advance-invalid-turn-order")
    val turnOrderAdvanceValid = get("turn-order-advance-valid")
    val turnOrderCreateInvalidNameAlreadyExists = get("turn-order-create-name-already-exists")
    val turnOrderCreateValid = get("turn-order-create-valid")
    val turnOrderHideValid = get("turn-order-hide-valid")
    val turnOrderRemoveUsage = get("turn-order-remove-usage")
    val turnOrderRemoveInvalidTurnOrder = get("turn-order-remove-invalid-turn-order")
    val turnOrderRemoveValid = get("turn-order-remove-valid")
    val turnOrderShowUsage = get("turn-order-show-usage")
    val turnOrderShowInvalidTurnOrder = get("turn-order-show-invalid-turn-order")
    val turnOrderShowInvalidTarget = get("turn-order-show-invalid-target")
    val turnOrderShowValid = get("turn-order-show-valid")
    val noPermissionTurnOrderAdd = get("no-permission-turn-order-add")
    val noPermissionTurnOrderAdvance = get("no-permission-turn-order-advance")
    val noPermissionTurnOrderCreate = get("no-permission-turn-order-create")
    val noPermissionTurnOrderHide = get("no-permission-turn-order-hide")
    val noPermissionTurnOrderRemove = get("no-permission-turn-order-remove")
    val noPermissionTurnOrderShow = get("no-permission-turn-order-show")
    val notFromConsole = get("not-from-console")
    val noCharacter = get("no-character")
    val noMinecraftProfileSelf = get("no-minecraft-profile-self")
    val noMinecraftProfileOther = get("no-minecraft-profile-other")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noCharacterService = get("no-character-service")
    val noTurnOrderService = get("no-turn-order-service")
}