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

package com.rpkit.economy.bukkit.servlet

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.web.RPKServlet
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND


class CurrencyServlet(private val plugin: RPKEconomyBukkit): RPKServlet() {
    override val url = "/money/currency/*"
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val activeProfile = profileProvider.getActiveProfile(req)
        val currencyName = req.pathInfo?.drop(1)
        if (currencyName == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val currency = currencyProvider.getCurrency(currencyName)
        if (currency == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val characters = if (activeProfile == null) emptyList() else characterProvider.getCharacters(activeProfile)
        val top = economyProvider.getRichestCharacters(currency, 5)
        resp.contentType = "text/html"
        resp.status = SC_NOT_FOUND
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/currency.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.core.web.title)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("characters", characters
                .map { character -> Pair(character, economyProvider.getBalance(character, currency)) }
                .toMap())
        velocityContext.put("top", top
                .map { character -> Pair(character, economyProvider.getBalance(character, currency)) }
                .toMap())
        Velocity.evaluate(velocityContext, resp.writer, "/web/currency.html", templateBuilder.toString())
    }
}