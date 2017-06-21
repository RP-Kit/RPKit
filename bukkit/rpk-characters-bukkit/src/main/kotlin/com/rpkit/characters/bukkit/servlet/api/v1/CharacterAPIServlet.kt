package com.rpkit.characters.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.web.RPKServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import javax.servlet.http.HttpServletResponse.SC_OK


class CharacterAPIServlet(private val plugin: RPKCharactersBukkit): RPKServlet() {

    override val url = "/api/v1/character/*"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.setHeader("Access-Control-Allow-Origin", "*")
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
}