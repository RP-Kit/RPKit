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
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_OK


class ProfilesServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/profiles/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/profiles.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val name = req.getParameter("name")
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = if (name == null) null else profileProvider.getProfile(name)
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.core.web.title)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("activeProfile", profileProvider.getActiveProfile(req))
        velocityContext.put("profile", profile)
        Velocity.evaluate(velocityContext, resp.writer, "/web/profiles.html", templateBuilder.toString())
    }

}