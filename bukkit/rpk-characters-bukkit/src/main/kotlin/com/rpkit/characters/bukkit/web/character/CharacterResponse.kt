/*
 * Copyright 2021 Ren Binden
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

import com.rpkit.characters.bukkit.character.RPKCharacter
import org.bukkit.inventory.ItemStack
import org.http4k.core.Body
import org.http4k.format.Gson.auto

data class CharacterResponse(
    val id: Int,
    val profileId: Int?,
    val minecraftProfileId: Int?,
    val name: String,
    val gender: String?,
    val age: Int,
    val race: String?,
    val description: String,
    val isDead: Boolean,
    val world: String?,
    val x: Double,
    val y: Double,
    val z: Double,
    val yaw: Float,
    val pitch: Float,
    val inventoryContents: List<Map<String, Any>?>,
    val helmet: Map<String, Any>?,
    val chestplate: Map<String, Any>?,
    val leggings: Map<String, Any>?,
    val boots: Map<String, Any>?,
    val health: Double,
    val maxHealth: Double,
    val mana: Int,
    val maxMana: Int,
    val foodLevel: Int,
    val thirstLevel: Int,
    val isProfileHidden: Boolean,
    val isNameHidden: Boolean,
    val isGenderHidden: Boolean,
    val isAgeHidden: Boolean,
    val isRaceHidden: Boolean,
    val isDescriptionHidden: Boolean
) {
    companion object {
        val lens = Body.auto<CharacterResponse>().toLens()
        val listLens = Body.auto<List<CharacterResponse>>().toLens()
    }
}

fun RPKCharacter.toCharacterResponse() = CharacterResponse(
    id?.value ?: 0,
    profile?.id?.value,
    minecraftProfile?.id?.value,
    name,
    gender,
    age,
    race?.name?.value,
    description,
    isDead,
    location.world?.name,
    location.x,
    location.y,
    location.z,
    location.yaw,
    location.pitch,
    inventoryContents.map(ItemStack::serialize),
    helmet?.serialize(),
    chestplate?.serialize(),
    leggings?.serialize(),
    boots?.serialize(),
    health,
    maxHealth,
    mana,
    maxMana,
    foodLevel,
    thirstLevel,
    isProfileHidden,
    isNameHidden,
    isGenderHidden,
    isAgeHidden,
    isRaceHidden,
    isDescriptionHidden
)