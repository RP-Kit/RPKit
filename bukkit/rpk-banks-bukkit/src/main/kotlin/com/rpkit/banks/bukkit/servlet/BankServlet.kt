package com.rpkit.banks.bukkit.servlet

import com.rpkit.banks.bukkit.RPKBanksBukkit
import com.rpkit.banks.bukkit.bank.RPKBankProvider
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.web.Alert
import com.rpkit.core.web.RPKServlet
import com.rpkit.economy.bukkit.currency.RPKCurrencyProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import javax.servlet.http.HttpServletResponse.SC_OK

class BankServlet(private val plugin: RPKBanksBukkit): RPKServlet() {

    override val url = "/banks/bank/*"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        if (req.pathInfo == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            return
        }
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val currencyId = req.pathInfo.drop(1).dropLastWhile { it != '/' }.dropLast(1)
        if (currencyId.isEmpty()) {
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
            return
        }
        val currency = currencyProvider.getCurrency(currencyId.toInt())
        if (currency == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getActiveProfile(req)
        if (profile == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_FORBIDDEN
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/unauthorized.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/unauthorized.html", templateBuilder.toString())
            return
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val characterId = req.pathInfo.drop(1).dropWhile { it != '/'}.drop(1)
        val character = characterProvider.getCharacter(characterId.toInt())
        if (character == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            return
        }
        if (character.profile != profile) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_FORBIDDEN
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/unauthorized.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/unauthorized.html", templateBuilder.toString())
        }
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        val balance = bankProvider.getBalance(character, currency)
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/bank.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", listOf<Alert>())
        velocityContext.put("activeProfile", profile)
        velocityContext.put("top", bankProvider.getRichestCharacters(currency, 5)
                .map { Pair(it, bankProvider.getBalance(it, currency)) }
                .toMap())
        velocityContext.put("character", character)
        velocityContext.put("currency", currency)
        velocityContext.put("balance", balance)
        Velocity.evaluate(velocityContext, resp.writer, "/web/bank.html", templateBuilder.toString())
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        if (req.pathInfo == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            return
        }
        val currencyProvider = plugin.core.serviceManager.getServiceProvider(RPKCurrencyProvider::class)
        val currencyId = req.pathInfo.drop(1).dropLastWhile { it != '/' }.dropLast(1)
        if (currencyId.isEmpty()) {
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
            return
        }
        val currency = currencyProvider.getCurrency(currencyId.toInt())
        if (currency == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getActiveProfile(req)
        if (profile == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_FORBIDDEN
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/unauthorized.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/unauthorized.html", templateBuilder.toString())
            return
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val characterId = req.pathInfo.drop(1).dropWhile { it != '/'}.drop(1)
        val character = characterProvider.getCharacter(characterId.toInt())
        if (character == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
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
            return
        }
        if (character.profile != profile) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_FORBIDDEN
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/unauthorized.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/unauthorized.html", templateBuilder.toString())
            return
        }
        val amount = req.getParameter("amount").toInt()
        val bankProvider = plugin.core.serviceManager.getServiceProvider(RPKBankProvider::class)
        var balance = bankProvider.getBalance(character, currency)
        val toCharacterId = req.getParameter("character")
        val toCharacter = if (toCharacterId != null) characterProvider.getCharacter(toCharacterId.toInt()) else null
        val alerts = mutableListOf<Alert>()
        if (amount > balance) {
            alerts.add(Alert(Alert.Type.DANGER, "You do not have enough money to perform that transaction."))
        } else {
            if (toCharacter == null) {
                alerts.add(Alert(Alert.Type.DANGER, "You must set a character to transfer to."))
            } else {
                bankProvider.withdraw(character, currency, amount)
                bankProvider.deposit(toCharacter, currency, amount)
                balance -= amount
            }
        }
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/bank.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", alerts)
        velocityContext.put("activeProfile", profile)
        velocityContext.put("top", bankProvider.getRichestCharacters(currency, 5)
                .map { Pair(it, bankProvider.getBalance(it, currency)) }
                .toMap())
        velocityContext.put("character", character)
        velocityContext.put("currency", currency)
        velocityContext.put("balance", balance)
        Velocity.evaluate(velocityContext, resp.writer, "/web/bank.html", templateBuilder.toString())
    }

}
