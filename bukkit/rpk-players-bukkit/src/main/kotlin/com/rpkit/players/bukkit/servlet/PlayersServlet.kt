/*
 * Copyright 2019 Ren Binden
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

package com.rpkit.players.bukkit.servlet

import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.net.InetAddress
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_OK

/**
 * Players servlet.
 * Serves player list page.
 */
class PlayersServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/players/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/players.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
        velocityContext.put("server", plugin.core.web.title)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("player", playerProvider.getPlayer(InetAddress.getByName(req.remoteAddr)))
        velocityContext.put("onlinePlayers", plugin.server.onlinePlayers.map { player -> playerProvider.getPlayer(player) }.toTypedArray())
        Velocity.evaluate(velocityContext, resp.writer, "/web/players.html", templateBuilder.toString())
    }

}
