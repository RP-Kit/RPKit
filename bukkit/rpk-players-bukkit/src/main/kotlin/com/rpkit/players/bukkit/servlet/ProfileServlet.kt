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
import org.passay.*
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
            velocityContext.put("server", plugin.server.serverName)
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
        val givenMinecraftProfileToken = req.getParameter("minecraft_profile_token")
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
                    val passwordRuleResult = plugin.passwordValidator.validate(PasswordData(profile.name, password))
                    if (passwordRuleResult.isValid) {
                        profile.setPassword(password.toCharArray())
                        profileProvider.updateProfile(profile)
                        alerts.add(Alert(SUCCESS, "Password successfully changed."))
                    } else {
                        passwordRuleResult.details.forEach { ruleResultDetail ->
                            alerts.add(Alert(DANGER, when (ruleResultDetail.errorCode) {
                                LengthRule.ERROR_CODE_MIN -> "Password must be at least ${ruleResultDetail.parameters["minimumLength"]} characters long."
                                LengthRule.ERROR_CODE_MAX -> "Password may not be longer than ${ruleResultDetail.parameters["maximumLength"]} characters long."
                                EnglishCharacterData.UpperCase.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} upper case characters."
                                EnglishCharacterData.LowerCase.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} lower case characters."
                                EnglishCharacterData.Digit.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} digits."
                                EnglishCharacterData.Special.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} special characters."
                                DictionarySubstringRule.ERROR_CODE -> "Password may not contain words. Found: ${ruleResultDetail.parameters["matchingWord"]}."
                                EnglishSequenceData.Alphabetical.errorCode -> "Password must not contain alphabetical sequences."
                                EnglishSequenceData.Numerical.errorCode -> "Password must not contain numerical sequences."
                                EnglishSequenceData.USQwerty.errorCode -> "Password must not contain sequences of keyboard letters."
                                UsernameRule.ERROR_CODE -> "Password must not contain your username."
                                UsernameRule.ERROR_CODE_REVERSED -> "Password must not contain your username reversed."
                                RepeatCharacterRegexRule.ERROR_CODE -> "Password must not contain repeated characters."
                                else -> "Password does not meet complexity rules: ${ruleResultDetail.errorCode}"
                            }))
                        }
                    }
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
                    val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(bukkitPlayer)
                    if (minecraftProfile != null) {
                        val minecraftProfileToken = minecraftProfileProvider.getMinecraftProfileToken(minecraftProfile)
                        if (minecraftProfileToken != null) {
                            if (minecraftProfileToken.token == givenMinecraftProfileToken) {
                                minecraftProfile.profile = profile
                                minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
                                minecraftProfileProvider.removeMinecraftProfileToken(minecraftProfileToken)
                                alerts.add(Alert(SUCCESS, "Minecraft profile successfully linked."))
                            } else {
                                alerts.add(Alert(DANGER, "The specified Minecraft profile token was invalid. Be sure it exactly matches what was given to you upon login."))
                            }
                        } else {
                            alerts.add(Alert(DANGER, "The specified Minecraft profile does not have any tokens. Is it linked to another profile?"))
                        }
                    } else {
                        alerts.add(Alert(DANGER, "There is no Minecraft profile by that name. Please make sure your username is entered correctly, and you have logged into the Minecraft server at least once."))
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
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("profile", profile)
        velocityContext.put("minecraftProfiles", minecraftProfileProvider.getMinecraftProfiles(profile))
        velocityContext.put("githubProfiles", githubProfileProvider.getGitHubProfiles(profile))
        velocityContext.put("ircProfiles", ircProfileProvider.getIRCProfiles(profile))
        velocityContext.put("alerts", alerts)
        Velocity.evaluate(velocityContext, resp.writer, "/web/profile_owner.html", templateBuilder.toString())
    }

}