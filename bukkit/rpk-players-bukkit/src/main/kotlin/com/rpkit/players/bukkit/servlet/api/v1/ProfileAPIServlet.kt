package com.rpkit.players.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*


class ProfileAPIServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/api/v1/profile/*"

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
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getProfile(req.pathInfo.drop(1))
        if (profile == null) {
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
        resp.contentType = "application/json"
        resp.status = HttpServletResponse.SC_OK
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("id", profile.id),
                                Pair("name", profile.name)
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
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getProfile(req.pathInfo.drop(1))
        if (profile == null) {
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
        val activeProfile = profileProvider.getActiveProfile(req)
        if (activeProfile == null) {
            resp.contentType = "application/json"
            resp.status = SC_FORBIDDEN
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Not authenticated.")
                            )
                    )
            )
            return
        }
        if (activeProfile != profile) {
            resp.contentType = "application/json"
            resp.status = SC_FORBIDDEN
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "You do not have permission.")
                            )
                    )
            )
            return
        }
        resp.contentType = "application/json"
        resp.status = SC_OK
        val name = req.getParameter("name")
        if (name != null) {
            if (name.matches(Regex("[A-z0-9_]{3,16}"))) {
                profile.name = name
            }
        }
        profileProvider.updateProfile(profile)
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("message", "Profile updated successfully.")
                        )
                )
        )
    }

}