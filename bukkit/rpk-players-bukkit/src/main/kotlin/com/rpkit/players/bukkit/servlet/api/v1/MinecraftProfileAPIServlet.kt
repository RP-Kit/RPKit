package com.rpkit.players.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileImpl
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*

class MinecraftProfileAPIServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/api/v1/minecraftprofile/*"

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
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfiles = minecraftProfileProvider.getMinecraftProfiles(profile)
        resp.contentType = "application/json"
        resp.status = SC_OK
        resp.writer.write(
                gson.toJson(
                        minecraftProfiles.map { minecraftProfile ->
                            val minecraftProfileProfile = minecraftProfile.profile
                            if (minecraftProfileProfile != null) {
                                mapOf(
                                        Pair("id", minecraftProfile.id),
                                        Pair("profile_id", minecraftProfileProfile.id),
                                        Pair("name", plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID).name),
                                        Pair("uuid", minecraftProfile.minecraftUUID.toString())
                                )
                            } else {
                                mapOf(
                                        Pair("id", minecraftProfile.id),
                                        Pair("name", plugin.server.getOfflinePlayer(minecraftProfile.minecraftUUID).name),
                                        Pair("uuid", minecraftProfile.minecraftUUID.toString())
                                )
                            }
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
        if (profile != null) {
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
        var minecraftUUID = UUID.fromString(req.getParameter("uuid"))
        var name = req.getParameter("name")
        if (minecraftUUID == null && name == null) {
            resp.contentType = "application/json"
            resp.status = SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Missing either name or uuid parameter.")
                            )
                    )
            )
        }
        if (name == null) {
            name = plugin.server.getOfflinePlayer(minecraftUUID).name
        }
        if (minecraftUUID == null) {
            minecraftUUID = plugin.server.getOfflinePlayer(name).uniqueId
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        var minecraftProfile = minecraftProfileProvider.getMinecraftProfile(plugin.server.getOfflinePlayer(minecraftUUID))
        if (minecraftProfile == null) {
            minecraftProfile = RPKMinecraftProfileImpl(
                    profile = profile,
                    minecraftUUID = minecraftUUID
            )
            minecraftProfileProvider.addMinecraftProfile(minecraftProfile)
        }
    }

}
