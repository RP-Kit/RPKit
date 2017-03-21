package com.rpkit.players.bukkit.servlet

import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ProfileSignOutServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/profiles/signout/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        profileProvider.setActiveProfile(req, null)
        resp.sendRedirect("/profiles/")
    }

}