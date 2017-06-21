package com.rpkit.banks.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.banks.bukkit.bank.RPKBankProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.web.RPKServlet
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND


class BankAPIServlet(private val plugin: RPKBanksBukkit): RPKServlet() {

    override val url = "/api/v1/bank/*"

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
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val balance = bankProvider.getBalance(character, currency)
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

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
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
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
        val toCharacterId = req.getParameter("to")?.toInt()
        if (toCharacterId == null) {
            resp.contentType = "application/json"
            resp.status = HttpServletResponse.SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "No character specified.")
                            )
                    )
            )
            return
        }
        val toCharacter = characterProvider.getCharacter(toCharacterId)
        if (toCharacter == null) {
            resp.contentType = "application/json"
            resp.status = HttpServletResponse.SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Character not found.")
                            )
                    )
            )
            return
        }
        val amount = req.getParameter("amount")?.toInt()
        if (amount == null) {
            resp.contentType = "application/json"
            resp.status = HttpServletResponse.SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "No amount specified")
                            )
                    )
            )
            return
        }
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val balance = bankProvider.getBalance(character, currency)
        if (amount > balance) {
            resp.contentType = "application/json"
            resp.status = HttpServletResponse.SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Amount is unaffordable.")
                            )
                    )
            )
            return
        }
        if (amount <= 0) {
            resp.contentType = "application/json"
            resp.status = HttpServletResponse.SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Amount may not be negative.")
                            )
                    )
            )
            return
        }
        bankProvider.withdraw(character, currency, amount)
        bankProvider.deposit(toCharacter, currency, amount)
        resp.contentType = "application/json"
        resp.status = HttpServletResponse.SC_OK
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("message", "Transfer completed successfully."),
                                Pair("balance", balance - amount)
                        )
                )
        )
    }

}