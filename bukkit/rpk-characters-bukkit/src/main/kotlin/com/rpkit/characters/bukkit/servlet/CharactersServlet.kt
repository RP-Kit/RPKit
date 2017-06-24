package com.rpkit.characters.bukkit.servlet

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_FORBIDDEN
import javax.servlet.http.HttpServletResponse.SC_OK


class CharactersServlet(private val plugin: RPKCharactersBukkit): RPKServlet() {
    override val url = "/characters/"
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val activeProfile = profileProvider.getActiveProfile(req)
        if (activeProfile == null) {
            resp.contentType = "text/html"
            resp.status = SC_FORBIDDEN
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/characters.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("activeProfile", null)
            Velocity.evaluate(velocityContext, resp.writer, "/web/characters.html", templateBuilder.toString())
            return
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val characters = characterProvider.getCharacters(activeProfile)
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/characters.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("activeProfile", activeProfile)
        velocityContext.put("characters", characters)
        Velocity.evaluate(velocityContext, resp.writer, "/web/characters.html", templateBuilder.toString())
    }
}