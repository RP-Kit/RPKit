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

package com.seventh_root.elysium.players.bukkit.servlet.api.v0_4

import com.google.gson.Gson
import com.seventh_root.elysium.core.web.ElysiumServlet
import com.seventh_root.elysium.players.bukkit.ElysiumPlayersBukkit
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*

/**
 * Player API v0.4 servlet.
 */
class PlayerAPIServlet(private val plugin: ElysiumPlayersBukkit): ElysiumServlet() {

    override val url = "/api/v0.4/player/*"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val gson = Gson()
        if (req.pathInfo != null) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
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
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
            val player = playerProvider.getPlayer(req.pathInfo.drop(1))
            if (player != null) {
                if (player.lastKnownIP == req.remoteAddr) {
                    resp.contentType = "application/json"
                    resp.status = SC_OK
                    val name = req.getParameter("name")
                    val ircNick = req.getParameter("irc_nick")
                    if (name != null && name.isNotBlank()) {
                        player.name = name
                    }
                    if (ircNick != null && ircNick.isNotBlank()) {
                        player.ircNick = ircNick
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
