package com.rpkit.characters.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import com.rpkit.core.web.RPKServlet
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*


class CharacterAPIServlet(private val plugin: RPKCharactersBukkit): RPKServlet() {

    override val url = "/api/v1/character/*"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val gson = Gson()
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val id = req.getParameter("id")?.toInt()
        if (id != null) {
            val character = characterProvider.getCharacter(id)
            if (character != null) {
                resp.contentType = "application/json"
                resp.status = SC_OK
                resp.writer.write(
                        gson.toJson(
                                mapOf(
                                        Pair("id", character.id),
                                        Pair("player_id", character.player?.id),
                                        Pair("profile_id", character.profile?.id),
                                        Pair("minecraft_profile_id", character.minecraftProfile?.id),
                                        Pair("name", character.name),
                                        Pair("gender_id", character.gender?.id),
                                        Pair("age", character.age),
                                        Pair("race", character.race?.id),
                                        Pair("description", character.description),
                                        Pair("dead", character.isDead),
                                        Pair("location", character.location.serialize()),
                                        Pair("inventory_contents", character.inventoryContents
                                                .map { item ->
                                                    item.serialize()
                                                }
                                        ),
                                        Pair("helmet", character.helmet?.serialize()),
                                        Pair("chestplate", character.chestplate?.serialize()),
                                        Pair("leggings", character.leggings?.serialize()),
                                        Pair("boots", character.boots?.serialize()),
                                        Pair("health", character.health),
                                        Pair("max_health", character.maxHealth),
                                        Pair("mana", character.mana),
                                        Pair("max_mana", character.maxMana),
                                        Pair("food_level", character.foodLevel),
                                        Pair("thirst_level", character.thirstLevel),
                                        Pair("player_hidden", character.isPlayerHidden),
                                        Pair("profile_hidden", character.isProfileHidden),
                                        Pair("name_hidden", character.isNameHidden),
                                        Pair("gender_hidden", character.isGenderHidden),
                                        Pair("age_hidden", character.isAgeHidden),
                                        Pair("race_hidden", character.isRaceHidden),
                                        Pair("description_hidden", character.isDescriptionHidden)
                                )
                        )
                )
                return
            }
        }
        val name = req.getParameter("name")
        if (name != null) {
            val characters = characterProvider.getCharacters(name)
            resp.contentType = "application/json"
            resp.status = SC_OK
            resp.writer.write(
                    gson.toJson(
                            characters.map { character ->
                                mapOf(
                                        Pair("id", character.id),
                                        Pair("player_id", character.player?.id),
                                        Pair("profile_id", character.profile?.id),
                                        Pair("minecraft_profile_id", character.minecraftProfile?.id),
                                        Pair("name", character.name),
                                        Pair("gender_id", character.gender?.id),
                                        Pair("age", character.age),
                                        Pair("race", character.race?.id),
                                        Pair("description", character.description),
                                        Pair("dead", character.isDead),
                                        Pair("location", character.location.serialize()),
                                        Pair("inventory_contents", character.inventoryContents
                                                .map { item ->
                                                    item?.serialize()
                                                }
                                        ),
                                        Pair("helmet", character.helmet?.serialize()),
                                        Pair("chestplate", character.chestplate?.serialize()),
                                        Pair("leggings", character.leggings?.serialize()),
                                        Pair("boots", character.boots?.serialize()),
                                        Pair("health", character.health),
                                        Pair("max_health", character.maxHealth),
                                        Pair("mana", character.mana),
                                        Pair("max_mana", character.maxMana),
                                        Pair("food_level", character.foodLevel),
                                        Pair("thirst_level", character.thirstLevel),
                                        Pair("player_hidden", character.isPlayerHidden),
                                        Pair("profile_hidden", character.isProfileHidden),
                                        Pair("name_hidden", character.isNameHidden),
                                        Pair("gender_hidden", character.isGenderHidden),
                                        Pair("age_hidden", character.isAgeHidden),
                                        Pair("race_hidden", character.isRaceHidden),
                                        Pair("description_hidden", character.isDescriptionHidden)
                                )
                            }
                    )
            )
            return
        }
        resp.contentType = "application/json"
        resp.status = SC_NOT_FOUND
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("message", "Not found.")
                        )
                )
        )
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val gson = Gson()
        val characterId = req.pathInfo?.drop(1)?.toIntOrNull()
        if (characterId == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found.")
                            )
                    )
            )
            return
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(characterId)
        if (character == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found.")
                            )
                    )
            )
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getActiveProfile(req)
        if (profile == null) {
            resp.contentType = "text/html"
            resp.status = SC_FORBIDDEN
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not authenticated.")
                            )
                    )
            )
            return
        }
        if (profile != character.profile) {
            resp.contentType = "text/html"
            resp.status = SC_OK
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "You do not own that character.")
                            )
                    )
            )
            return
        }
        val permissionsProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
        val name = req.getParameter("name")
        if (name != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.name")) {
                character.name = name
            }
        }
        val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
        val genderId = req.getParameter("gender")?.toInt()
        if (genderId != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.gender")) {
                val gender = genderProvider.getGender(genderId)
                character.gender = gender
            }
        }
        val age = req.getParameter("age")?.toInt()
        if (age != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.age")) {
                character.age = age
            }
        }
        val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
        val raceId = req.getParameter("race")?.toInt()
        if (raceId != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.race")) {
                val race = raceProvider.getRace(raceId)
                character.race = race
            }
        }
        val description = req.getParameter("description")
        if (description != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.description")) {
                character.description = description
            }
        }
        val dead = req.getParameter("dead")
        if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.dead")) {
            if ((dead != null && permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.dead.yes")
                    || (dead == null && permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.dead.no")))) {
                character.isDead = dead != null
            }
        }
        characterProvider.updateCharacter(character)
        resp.contentType = "text/html"
        resp.status = SC_OK
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("message", "Character updated successfully.")
                        )
                )
        )
    }

}