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

import com.rpkit.core.web.Alert
import com.rpkit.core.web.Alert.Type.DANGER
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ProfileSignUpServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/profiles/signup/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val activeProfile = profileProvider.getActiveProfile(req)
        if (activeProfile != null) {
            resp.sendRedirect("/profiles/")
            return
        }
        resp.contentType = "text/html"
        resp.status = HttpServletResponse.SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/signup.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.core.web.title)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", listOf<Alert>())
        Velocity.evaluate(velocityContext, resp.writer, "/web/signup.html", templateBuilder.toString())
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val name = req.getParameter("name")
        val password = req.getParameter("password")
        val confirmPassword = req.getParameter("confirm_password")
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        var profile = profileProvider.getProfile(name)
        val alerts = mutableListOf<Alert>()
        if (profile == null) {
            if (password == confirmPassword) {
                if (name.matches(Regex("[A-z0-9_]{3,16}"))) {
                    profile = RPKProfileImpl(
                            name = name,
                            password = password
                    )
                    profileProvider.addProfile(profile)
                    profileProvider.setActiveProfile(req, profile)
                    resp.sendRedirect("/profile/${profile.name}")
                    return
                } else {
                    alerts.add(Alert(DANGER, "Your name must be between 3 and 16 characters, including alphanumerics and underscores only."))
                }
            } else {
                alerts.add(Alert(DANGER, "The passwords you entered do not match."))
            }
        } else {
            alerts.add(Alert(DANGER, "A user by that name already exists."))
        }
        resp.contentType = "text/html"
        resp.status = HttpServletResponse.SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/signup.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.core.web.title)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", alerts)
        Velocity.evaluate(velocityContext, resp.writer, "/web/signup.html", templateBuilder.toString())
    }

}