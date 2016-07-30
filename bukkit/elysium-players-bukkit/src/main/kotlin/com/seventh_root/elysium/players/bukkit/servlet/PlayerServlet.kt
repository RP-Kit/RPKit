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

package com.seventh_root.elysium.players.bukkit.servlet

import com.seventh_root.elysium.core.web.Alert
import com.seventh_root.elysium.core.web.Alert.Type.DANGER
import com.seventh_root.elysium.core.web.Alert.Type.SUCCESS
import com.seventh_root.elysium.core.web.ElysiumServlet
import com.seventh_root.elysium.players.bukkit.ElysiumPlayersBukkit
import com.seventh_root.elysium.players.bukkit.player.ElysiumPlayerProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*


class PlayerServlet(private val plugin: ElysiumPlayersBukkit): ElysiumServlet() {

    override val url = "/player/*"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
        val playerName = req.pathInfo.drop(1)
        if (playerName.isNotEmpty()) {
            val player = playerProvider.getPlayer(playerName)
            if (player != null) {
                resp.contentType = "text/html"
                resp.status = SC_OK
                val templateBuilder = StringBuilder()
                val scanner = Scanner(javaClass.getResourceAsStream("/web/player.html"))
                while (scanner.hasNextLine()) {
                    templateBuilder.append(scanner.nextLine()).append('\n')
                }
                scanner.close()
                val velocityContext = VelocityContext()
                velocityContext.put("server", plugin.server.serverName)
                velocityContext.put("navigationBar", plugin.core.web.navigationBar)
                velocityContext.put("player", player)
                velocityContext.put("alerts", arrayOf<Alert>())
                Velocity.evaluate(velocityContext, resp.writer, "/web/player.html", templateBuilder.toString())
            } else {
                resp.contentType = "text/html"
                resp.status = SC_NOT_FOUND
                val templateBuilder = StringBuilder()
                val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
                while (scanner.hasNextLine()) {
                    templateBuilder.append(scanner.nextLine()).append('\n')
                }
                scanner.close()
                val velocityContext = VelocityContext()
                velocityContext.put("server", plugin.server.serverName)
                velocityContext.put("navigationBar", plugin.core.web.navigationBar)
                Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            }
        } else {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
        }
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        if (req.pathInfo != null) {
            val playerProvider = plugin.core.serviceManager.getServiceProvider(ElysiumPlayerProvider::class)
            val playerName = req.pathInfo.drop(1)
            if (playerName.isNotEmpty()) {
                val player = playerProvider.getPlayer(playerName)
                if (player != null) {
                    if (player.lastKnownIP == req.remoteAddr) {
                        val name = req.getParameter("name")
                        val ircNick = req.getParameter("irc_nick")
                        if (name != null && name.isNotBlank()) {
                            player.name = name
                        }
                        if (ircNick != null && ircNick.isNotBlank()) {
                            player.ircNick = ircNick
                        }
                        playerProvider.updatePlayer(player)
                        resp.contentType = "text/html"
                        resp.status = SC_OK
                        val templateBuilder = StringBuilder()
                        val scanner = Scanner(javaClass.getResourceAsStream("/web/player.html"))
                        while (scanner.hasNextLine()) {
                            templateBuilder.append(scanner.nextLine()).append('\n')
                        }
                        scanner.close()
                        val velocityContext = VelocityContext()
                        velocityContext.put("server", plugin.server.serverName)
                        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
                        velocityContext.put("player", player)
                        velocityContext.put("alerts", arrayOf(Alert(SUCCESS, "Profile successfully updated.")))
                        Velocity.evaluate(velocityContext, resp.writer, "/web/player.html", templateBuilder.toString())
                    } else {
                        resp.contentType = "text/html"
                        resp.status = SC_FORBIDDEN
                        val templateBuilder = StringBuilder()
                        val scanner = Scanner(javaClass.getResourceAsStream("/web/player.html"))
                        while (scanner.hasNextLine()) {
                            templateBuilder.append(scanner.nextLine()).append('\n')
                        }
                        scanner.close()
                        val velocityContext = VelocityContext()
                        velocityContext.put("server", plugin.server.serverName)
                        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
                        velocityContext.put("player", player)
                        velocityContext.put("alerts", arrayOf(Alert(DANGER, "You can not update other players' profiles.")))
                        Velocity.evaluate(velocityContext, resp.writer, "/web/player.html", templateBuilder.toString())
                    }
                } else {
                    resp.contentType = "text/html"
                    resp.status = SC_NOT_FOUND
                    val templateBuilder = StringBuilder()
                    val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
                    while (scanner.hasNextLine()) {
                        templateBuilder.append(scanner.nextLine()).append('\n')
                    }
                    scanner.close()
                    val velocityContext = VelocityContext()
                    velocityContext.put("server", plugin.server.serverName)
                    velocityContext.put("navigationBar", plugin.core.web.navigationBar)
                    Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
                }
            } else {
                resp.contentType = "text/html"
                resp.status = SC_NOT_FOUND
                val templateBuilder = StringBuilder()
                val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
                while (scanner.hasNextLine()) {
                    templateBuilder.append(scanner.nextLine()).append('\n')
                }
                scanner.close()
                val velocityContext = VelocityContext()
                velocityContext.put("server", plugin.server.serverName)
                velocityContext.put("navigationBar", plugin.core.web.navigationBar)
                Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            }
        } else {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
        }
    }

}