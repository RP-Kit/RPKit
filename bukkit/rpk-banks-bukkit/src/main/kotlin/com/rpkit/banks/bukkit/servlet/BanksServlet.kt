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

package com.rpkit.banks.bukkit.servlet

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.core.web.RPKServlet
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_OK

class BanksServlet(private val plugin: RPKBanksBukkit): RPKServlet() {

    override val url = "/banks/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/banks.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.core.web.title)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("activeProfile", profileProvider.getActiveProfile(req))
        velocityContext.put("currencies", currencyProvider.currencies)
        Velocity.evaluate(velocityContext, resp.writer, "/web/banks.html", templateBuilder.toString())
    }

}
