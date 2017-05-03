package com.rpkit.players.bukkit.servlet.api.v1

import com.google.gson.Gson
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKGitHubProfileImpl
import com.rpkit.players.bukkit.profile.RPKGitHubProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.*


class GitHubProfileAPIServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/api/v1/githubprofile/*"

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
        val githubProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKGitHubProfileProvider::class)
        val githubProfiles = githubProfileProvider.getGitHubProfiles(profile)
        resp.contentType = "application/json"
        resp.status = SC_OK
        resp.writer.write(
                gson.toJson(
                        githubProfiles.map { githubProfile ->
                            mapOf(
                                    Pair("id", githubProfile.id),
                                    Pair("profile_id", githubProfile.profile.id),
                                    Pair("name", githubProfile.name)
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
        val name = req.getParameter("name")
        if (name == null) {
            resp.contentType = "application/json"
            resp.status = SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Missing name parameter.")
                            )
                    )
            )
            return
        }
        val oauthToken = req.getParameter("oauth_token")
        if (oauthToken == null) {
            resp.contentType = "application/json"
            resp.status = SC_BAD_REQUEST
            resp.writer.write(
                    gson.toJson(
                            mapOf(
                                    Pair("message", "Missing oauth token parameter.")
                            )
                    )
            )
            return
        }
        val githubProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKGitHubProfileProvider::class)
        val githubProfile = RPKGitHubProfileImpl(
                profile = profile,
                name = name,
                oauthToken = oauthToken
        )
        githubProfileProvider.addGitHubProfile(githubProfile)
        resp.contentType = "application/json"
        resp.status = SC_OK
        resp.writer.write(
                gson.toJson(
                        mapOf(
                                Pair("message", "GitHub profile added successfully.")
                        )
                )
        )
    }

}