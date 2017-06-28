package com.rpkit.players.bukkit.servlet

import com.rpkit.core.web.Alert
import com.rpkit.core.web.Alert.Type.DANGER
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_OK


class ProfileSignInServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/profiles/signin/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val activeProfile = profileProvider.getActiveProfile(req)
        if (activeProfile != null) {
            resp.sendRedirect("/profiles/")
            return
        }
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/signin.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", listOf<Alert>())
        Velocity.evaluate(velocityContext, resp.writer, "/web/signin.html", templateBuilder.toString())
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val name = req.getParameter("name")
        val password = req.getParameter("password")
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getProfile(name)
        val alerts = mutableListOf<Alert>()
        if (profile != null) {
            if (profile.checkPassword(password.toCharArray())) {
                profileProvider.setActiveProfile(req, profile)
                resp.sendRedirect("/profiles/")
                return
            } else {
                alerts.add(Alert(DANGER, "Incorrect username or password."))
            }
        } else {
            alerts.add(Alert(DANGER, "Incorrect username or password."))
        }
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/signin.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", alerts)
        Velocity.evaluate(velocityContext, resp.writer, "/web/signin.html", templateBuilder.toString())
    }

}