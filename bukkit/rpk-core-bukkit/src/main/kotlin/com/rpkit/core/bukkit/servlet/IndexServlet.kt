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

package com.rpkit.core.bukkit.servlet

import com.rpkit.core.bukkit.RPKCoreBukkit
import com.rpkit.core.web.RPKServlet
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_OK

/**
 * Servlet for RPK index page.
 */
class IndexServlet(private val plugin: RPKCoreBukkit): RPKServlet() {

    override val url = "/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/index.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        Velocity.evaluate(velocityContext, resp.writer, "/web/index.html", templateBuilder.toString())
    }

}