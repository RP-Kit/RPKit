package com.rpkit.economy.bukkit.servlet.api

import com.google.gson.Gson
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.web.RPKServlet
import com.rpkit.economy.bukkit.RPKEconomyBukkit
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.economy.bukkit.economy.RPKEconomyProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND


class MoneyAPIServlet(private val plugin: RPKEconomyBukkit): RPKServlet() {

    override val url = "/api/v1/money/*"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val gson = Gson()
        if (req.pathInfo == null) {
            resp.contentType = "application/json"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found.")
                            )
                    )
            )
            return
        }
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val currencyId = req.pathInfo.drop(1).dropLastWhile { it != '/' }.dropLast(1)
        if (currencyId.isEmpty()) {
            resp.contentType = "application/json"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found.")
                            )
                    )
            )
            return
        }
        val currency = currencyProvider.getCurrency(currencyId.toInt())
        if (currency == null) {
            resp.contentType = "application/json"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found.")
                            )
                    )
            )
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getActiveProfile(req)
        if (profile == null) {
            resp.contentType = "application/json"
            resp.status = HttpServletResponse.SC_FORBIDDEN
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not authenticated.")
                            )
                    )
            )
            return
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val characterId = req.pathInfo.drop(1).dropWhile { it != '/'}.drop(1)
        val character = characterProvider.getCharacter(characterId.toInt())
        if (character == null) {
            resp.contentType = "application/json"
            resp.status = SC_NOT_FOUND
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not found.")
                            )
                    )
            )
            return
        }
        if (character.profile != profile) {
            resp.contentType = "application/json"
            resp.status = HttpServletResponse.SC_FORBIDDEN
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "You do not have permission.")
                            )
                    )
            )
            return
        }
        val economyProvider = plugin.core.serviceManager.getServiceProvider(RPKEconomyProvider::class)
        val balance = economyProvider.getBalance(character, currency)
        resp.contentType = "application/json"
        resp.status = HttpServletResponse.SC_OK
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("character_id", character.id),
                                Pair("currency_id", currency.id),
                                Pair("balance", balance)
                        )
                )
        )
    }

}