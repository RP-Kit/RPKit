package com.rpkit.players.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKIRCProfileImpl
import com.rpkit.players.bukkit.profile.RPKIRCProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*


class IRCProfileAPIServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/api/v1/ircprofile/*"

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
        val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
        val ircProfiles = ircProfileProvider.getIRCProfiles(profile)
        resp.contentType = "application/json"
        resp.status = SC_OK
        resp.writer.write(
                gson.toJson(
                        ircProfiles.map { ircProfile ->
                            mapOf(
                                    Pair("id", ircProfile.id),
                                    Pair("profile_id", ircProfile.profile.id),
                                    Pair("nick", ircProfile.nick)
                            )
                        }
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
        val nick = req.getParameter("nick")
        if (nick == null) {
            resp.contentType = "application/json"
            resp.status = SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Missing nick parameter.")
                            )
                    )
            )
            return
        }
        val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
        val ircProfile = RPKIRCProfileImpl(
                profile = profile,
                nick = nick
        )
        ircProfileProvider.addIRCProfile(ircProfile)
        resp.contentType = "application/json"
        resp.status = SC_OK
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("message", "IRC profile added successfully")
                        )
                )
        )
    }

}