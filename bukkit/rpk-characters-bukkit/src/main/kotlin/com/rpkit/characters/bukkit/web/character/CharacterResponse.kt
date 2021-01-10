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
    val raceId: Int?,
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
    id ?: 0,
    profile?.id,
    minecraftProfile?.id,
    name,
    gender,
    age,
    race?.id,
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