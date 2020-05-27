/*
 * Copyright 2020 Ren Binden
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.rpkit.players.bukkit.servlet

import com.rpkit.chat.bukkit.irc.RPKIRCProvider
import com.rpkit.core.web.Alert
import com.rpkit.core.web.Alert.Type.DANGER
import com.rpkit.core.web.Alert.Type.SUCCESS
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.*
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.io.IOException
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_FORBIDDEN
import javax.servlet.http.HttpServletResponse.SC_OK


class ProfileServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/profile/*"

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
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profileName = req.pathInfo.drop(1)
        if (profileName.isEmpty()) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val profile = profileProvider.getProfile(profileName)
        if (profile == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        if (profile == profileProvider.getActiveProfile(req)) {
            resp.contentType = "text/html"
            resp.status = SC_OK
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/profile_owner.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val githubProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKGitHubProfileProvider::class)
            val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("profile", profile)
            velocityContext.put("minecraftProfiles", minecraftProfileProvider.getMinecraftProfiles(profile))
            velocityContext.put("githubProfiles", githubProfileProvider.getGitHubProfiles(profile))
            velocityContext.put("ircProfiles", ircProfileProvider.getIRCProfiles(profile))
            velocityContext.put("alerts", listOf<Alert>())
            Velocity.evaluate(velocityContext, resp.writer, "/web/profile_owner.html", templateBuilder.toString())
        } else {
            resp.contentType = "text/html"
            resp.status = SC_OK
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/profile.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
            val githubProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKGitHubProfileProvider::class)
            val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("profile", profile)
            velocityContext.put("minecraftProfiles", minecraftProfileProvider.getMinecraftProfiles(profile))
            velocityContext.put("githubProfiles", githubProfileProvider.getGitHubProfiles(profile))
            velocityContext.put("ircProfiles", ircProfileProvider.getIRCProfiles(profile))
            velocityContext.put("alerts", listOf<Alert>())
            Velocity.evaluate(velocityContext, resp.writer, "/web/profile.html", templateBuilder.toString())
        }
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
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profileName = req.pathInfo.drop(1)
        if (profileName.isEmpty()) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val profile = profileProvider.getProfile(profileName)
        if (profile == null) {
            resp.contentType = "text/html"
            resp.status = HttpServletResponse.SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        if (profile != profileProvider.getActiveProfile(req)) {
            resp.contentType = "text/html"
            resp.status = SC_FORBIDDEN
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/profile.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            scanner.close()
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.core.web.title)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("profile", profile)
            velocityContext.put("alerts", listOf(Alert(Alert.Type.DANGER, "You may only modify your own profile.")))
            Velocity.evaluate(velocityContext, resp.writer, "/web/profile.html", templateBuilder.toString())
            return
        }
        val alerts = mutableListOf<Alert>()
        val name = req.getParameter("name")
        val password = req.getParameter("password")
        val confirmPassword = req.getParameter("confirm_password")
        val minecraftUsername = req.getParameter("minecraft_username")
        val deleteMinecraftProfileId = req.getParameter("delete_minecraft_profile_id")
        val githubOauthToken = req.getParameter("github_oauth_token")
        val deleteGitHubProfileId = req.getParameter("delete_github_profile_id")
        val ircNick = req.getParameter("irc_nick")
        val deleteIrcProfileId = req.getParameter("delete_irc_profile_id")
        if (name != null) {
            if (name.isNotBlank()) {
                if (name.matches(Regex("[A-z0-9_]{3,16}"))) {
                    profile.name = name
                    profileProvider.updateProfile(profile)
                    alerts.add(Alert(SUCCESS, "Name successfully changed."))
                } else {
                    alerts.add(Alert(DANGER, "Name must consist of only alphanumerics and underscores, and must be between 3 and 16 characters."))
                }
            }
        }
        if (password != null) {
            if (password.isNotBlank()) {
                if (password == confirmPassword) {
                    profile.setPassword(password.toCharArray())
                    profileProvider.updateProfile(profile)
                    alerts.add(Alert(SUCCESS, "Password successfully changed."))
                } else {
                    alerts.add(Alert(DANGER, "Passwords do not match."))
                }
            }
        }
        if (minecraftUsername != null) {
            if (minecraftUsername.isNotBlank()) {
                if (minecraftUsername.matches(Regex("[A-z0-9_]{3,16}"))) {
                    val bukkitPlayer = plugin.server.getOfflinePlayer(minecraftUsername)
                    val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                    val existingMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                    if (existingMinecraftProfile == null) {
                        val minecraftProfile = RPKMinecraftProfileImpl(profile = RPKThinProfileImpl(minecraftUsername), minecraftUUID = bukkitPlayer.uniqueId)
                        minecraftProfileProvider.addMinecraftProfile(minecraftProfile)
                        val minecraftProfileLinkRequest = RPKMinecraftProfileLinkRequestImpl(profile = profile, minecraftProfile = minecraftProfile)
                        minecraftProfileProvider.addMinecraftProfileLinkRequest(minecraftProfileLinkRequest)
                        alerts.add(Alert(SUCCESS, "Link request sent. Log in to the account and approve it to link the account."))
                    } else {
                        if (existingMinecraftProfile.profile !is RPKProfile) {
                            val minecraftProfileLinkRequest = RPKMinecraftProfileLinkRequestImpl(profile = profile, minecraftProfile = existingMinecraftProfile)
                            minecraftProfileProvider.addMinecraftProfileLinkRequest(minecraftProfileLinkRequest = minecraftProfileLinkRequest)
                            alerts.add(Alert(SUCCESS, "Link request sent. Log in to the account and approve it to link the account."))
                        } else {
                            alerts.add(Alert(DANGER, "That Minecraft profile is already linked to a profile."))
                        }
                    }
                } else {
                    alerts.add(Alert(DANGER, "The specified Minecraft username is invalid. Please make sure it is typed correctly."))
                }
            }
        }
        if (deleteMinecraftProfileId != null) {
            try {
                val deleteMinecraftProfileIdInt = deleteMinecraftProfileId.toInt()
                val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
                val deleteMinecraftProfile = minecraftProfileProvider.getMinecraftProfile(deleteMinecraftProfileIdInt)
                if (deleteMinecraftProfile != null) {
                    if (deleteMinecraftProfile.profile == profile) {
                        minecraftProfileProvider.removeMinecraftProfile(deleteMinecraftProfile)
                        alerts.add(Alert(SUCCESS, "Minecraft profile successfully deleted."))
                    } else {
                        alerts.add(Alert(DANGER, "You may not delete other people's Minecraft profiles."))
                    }
                } else {
                    alerts.add(Alert(DANGER, "The Minecraft profile does not exist. It may already have been deleted."))
                }
            } catch (exception: NumberFormatException) {
                alerts.add(Alert(DANGER, "Request to delete Minecraft profile was malformed."))
            }
        }
        if (githubOauthToken != null) {
            if (githubOauthToken.isNotBlank()) {
                val githubProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKGitHubProfileProvider::class)
                try {
                    val githubProfile = RPKGitHubProfileImpl(
                            profile = profile,
                            oauthToken = githubOauthToken
                    )
                    githubProfileProvider.addGitHubProfile(githubProfile)
                    alerts.add(Alert(SUCCESS, "GitHub profile successfully linked."))
                } catch (exception: IOException) {
                    alerts.add(Alert(DANGER, "Failed to connect to GitHub. Are you sure your OAuth token is correct?"))
                }
            }
        }
        if (deleteGitHubProfileId != null) {
            try {
                val deleteGitHubProfileIdInt = deleteGitHubProfileId.toInt()
                val githubProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKGitHubProfileProvider::class)
                val deleteGithubProfile = githubProfileProvider.getGitHubProfile(deleteGitHubProfileIdInt)
                if (deleteGithubProfile != null) {
                    if (deleteGithubProfile.profile == profile) {
                        githubProfileProvider.removeGitHubProfile(deleteGithubProfile)
                        alerts.add(Alert(SUCCESS, "GitHub profile successfully deleted."))
                    } else {
                        alerts.add(Alert(DANGER, "You may not delete other people's GitHub profiles."))
                    }
                } else {
                    alerts.add(Alert(DANGER, "The GitHub profile does not exist. It may already have been deleted."))
                }
            } catch (exception: NumberFormatException) {
                alerts.add(Alert(DANGER, "Request to delete GitHub profile was malformed."))
            }
        }
        if (ircNick != null) {
            if (ircNick.isNotBlank()) {
                val ircProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProvider::class)
                val ircUser = ircProvider.getIRCUser(ircNick)
                if (ircUser != null) {
                    val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
                    var ircProfile = ircProfileProvider.getIRCProfile(ircUser)
                    if (ircProfile == null) {
                        ircProfile = RPKIRCProfileImpl(
                                profile = profile,
                                nick = ircNick
                        )
                        ircProfileProvider.addIRCProfile(ircProfile)
                        alerts.add(Alert(SUCCESS, "IRC profile successfully linked."))
                    } else {
                        alerts.add(Alert(DANGER, "That IRC account is already linked to a profile."))
                    }
                } else {
                    alerts.add(Alert(DANGER, "Please connect to one of the server's IRC channels."))
                }
            }
        }
        if (deleteIrcProfileId != null) {
            try {
                val deleteIrcProfileIdInt = deleteIrcProfileId.toInt()
                val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
                val deleteIrcProfile = ircProfileProvider.getIRCProfile(deleteIrcProfileIdInt)
                if (deleteIrcProfile != null) {
                    if (deleteIrcProfile.profile == profile) {
                        ircProfileProvider.removeIRCProfile(deleteIrcProfile)
                        alerts.add(Alert(SUCCESS, "IRC profile successfully deleted."))
                    } else {
                        alerts.add(Alert(DANGER, "You may not delete other people's IRC profiles."))
                    }
                } else {
                    alerts.add(Alert(DANGER, "The IRC profile does not exist. It may already have been deleted."))
                }
            } catch (exception: NumberFormatException) {
                alerts.add(Alert(DANGER, "Request to delete IRC profile was malformed."))
            }
        }
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/profile_owner.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val githubProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKGitHubProfileProvider::class)
        val ircProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKIRCProfileProvider::class)
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.core.web.title)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("profile", profile)
        velocityContext.put("minecraftProfiles", minecraftProfileProvider.getMinecraftProfiles(profile))
        velocityContext.put("githubProfiles", githubProfileProvider.getGitHubProfiles(profile))
        velocityContext.put("ircProfiles", ircProfileProvider.getIRCProfiles(profile))
        velocityContext.put("alerts", alerts)
        Velocity.evaluate(velocityContext, resp.writer, "/web/profile_owner.html", templateBuilder.toString())
    }

}