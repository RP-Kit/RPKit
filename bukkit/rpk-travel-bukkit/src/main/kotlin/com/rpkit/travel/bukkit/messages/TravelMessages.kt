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

package com.rpkit.travel.bukkit.messages

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.core.bukkit.message.BukkitMessages
import com.rpkit.core.message.ParameterizedMessage
import com.rpkit.core.message.to
import com.rpkit.travel.bukkit.RPKTravelBukkit
import com.rpkit.warp.bukkit.warp.RPKWarp
import org.bukkit.World
import org.bukkit.entity.LivingEntity

class TravelMessages(plugin: RPKTravelBukkit) : BukkitMessages(plugin) {

    class DeleteWarpValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(warp: RPKWarp) = message.withParameters("warp" to warp.name.value)
    }

    class SetWarpValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(warp: RPKWarp, world: World, x: Int, y: Int, z: Int) = message.withParameters(
            "warp" to warp.name.value,
            "world" to world.name,
            "x" to x.toString(),
            "y" to y.toString(),
            "z" to z.toString()
        )
    }

    class WarpValidMessage(private val message: ParameterizedMessage) {
        fun withParameters(warp: RPKWarp) = message.withParameters(
            "warp" to warp.name.value
        )
    }

    class WarpListItemMessage(private val message: ParameterizedMessage) {
        fun withParameters(warps: String) = message.withParameters(
            "warps" to warps
        )
    }

    class TamedByMessage(private val message: ParameterizedMessage) {
        fun withParameters(entity: LivingEntity, character: RPKCharacter) = message.withParameters(
            "entity" to entity.type.name.lowercase().replace('_', ' '),
            "character" to character.name
        )
    }

    val noMinecraftProfile = get("no-minecraft-profile")
    val deleteWarpValid = getParameterized("delete-warp-valid").let(::DeleteWarpValidMessage)
    val deleteWarpUsage = get("delete-warp-usage")
    val deleteWarpInvalidWarp = get("delete-warp-invalid-warp")
    val setWarpInvalidNameAlreadyInUse = get("set-warp-invalid-name-already-in-use")
    val setWarpValid = getParameterized("set-warp-valid").let(::SetWarpValidMessage)
    val setWarpUsage = get("set-warp-usage")
    val warpValid = getParameterized("warp-valid").let(::WarpValidMessage)
    val warpInvalidWarp = get("warp-invalid-warp")
    val warpListTitle = get("warp-list-title")
    val warpListItem = getParameterized("warp-list-item").let(::WarpListItemMessage)
    val warpListInvalidEmpty = get("warp-list-invalid-empty")
    val warpSignInvalidWarp = get("warp-sign-invalid-warp")
    val warpSignValid = get("warp-sign-valid")
    val untameSelectCreatureToUntame = get("untame-select-creature-to-untame")
    val untameValid = get("untame-valid")
    val untameCancelled = get("untame-cancelled")
    val noPermissionDeleteWarp = get("no-permission-delete-warp")
    val noPermissionSetWarp = get("no-permission-set-warp")
    val noPermissionWarpSignCreate = get("no-permission-warp-sign-create")
    val noPermissionWarp = get("no-permission-warp")
    val noPermissionUntame = get("no-permission-untame")
    val noWarpService = get("no-warp-service")
    val noMinecraftProfileService = get("no-minecraft-profile-service")
    val noUntamerService = get("no-untamer-service")
    val notFromConsole = get("not-from-console")
    val tamedBy = getParameterized("tamed-by").let(::TamedByMessage)

}