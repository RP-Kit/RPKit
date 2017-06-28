/*
 * Copyright 2016 Ross Binden
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

package com.rpkit.players.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*

/**
 * Player API v1 servlet.
 */
class PlayerAPIServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/api/v1/player/*"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val gson = Gson()
        if (req.pathInfo != null) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val player = playerProvider.getPlayer(req.pathInfo.drop(1))
            if (player != null) {
                resp.contentType = "application/json"
                resp.status = SC_OK
                resp.writer.write(
                        gson.toJson(
                                mapOf(
                                        Pair("id", player.id),
                                        Pair("name", player.name),
                                        Pair("minecraft_uuid", player.bukkitPlayer?.uniqueId?.toString()),
                                        Pair("irc_nick", player.ircNick),
                                        Pair("last_known_ip", player.lastKnownIP)
                                )
                        )
                )
            } else {
                resp.contentType = "application/json"
                resp.status = SC_NOT_FOUND
                resp.writer.write(
                        gson.toJson(
                                mapOf(
                                        Pair("message", "Not found")
                                )
                        )
                )
            }
        } else {
            resp.contentType = "application/json"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found")
                            )
                    )
            )
        }
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val gson = Gson()
        if (req.pathInfo != null) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
            val player = playerProvider.getPlayer(req.pathInfo.drop(1))
            if (player != null) {
                if (player.lastKnownIP == req.remoteAddr) {
                    resp.contentType = "application/json"
                    resp.status = SC_OK
                    val name = req.getParameter("name")
                    val ircNick = req.getParameter("irc_nick")
                    if (name != null) {
                        if (name.isNotBlank()) {
                            if (name.matches(Regex("[A-z0-9_]{3,16}"))) {
                                player.name = name
                            }
                        }
                    }
                    if (ircNick != null) {
                        if (ircNick.isNotBlank()) {
                            if (player.ircNick == null) {
                                val ircProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class)
                                val ircUser = ircProvider.getIRCUser(ircNick)
                                if (ircUser != null) {
                                    val existingIRCPlayer = playerProvider.getPlayer(ircUser)
                                    playerProvider.removePlayer(existingIRCPlayer)
                                    player.ircNick = ircNick
                                }
                            }
                        }
                    }
                    playerProvider.updatePlayer(player)
                    resp.writer.write(
                            gson.toJson(
                                    mapOf(
                                            Pair("message", "Profile updated successfully.")
                                    )
                            )
                    )
                } else {
                    resp.contentType = "application/json"
                    resp.status = SC_FORBIDDEN
                    resp.writer.write(
                            gson.toJson(
                                    mapOf(
                                            Pair("message", "Not authenticated")
                                    )
                            )
                    )
                }
            } else {
                resp.contentType = "application/json"
                resp.status = SC_NOT_FOUND
                resp.writer.write(
                        gson.toJson(
                                mapOf(
                                        Pair("message", "Not found")
                                )
                        )
                )
            }
        } else {
            resp.contentType = "application/json"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found")
                            )
                    )
            )
        }
    }

}
