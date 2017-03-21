package com.rpkit.players.bukkit.servlet

import com.rpkit.core.web.Alert
import com.rpkit.core.web.Alert.Type.DANGER
import com.rpkit.core.web.RPKServlet
import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.passay.*
import org.passay.EnglishCharacterData.*
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class ProfileSignUpServlet(private val plugin: RPKPlayersBukkit): RPKServlet() {

    override val url = "/profiles/signup/"

    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val activeProfile = profileProvider.getActiveProfile(req)
        if (activeProfile != null) {
            resp.sendRedirect("/profiles/")
            return
        }
        resp.contentType = "text/html"
        resp.status = HttpServletResponse.SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/signup.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", listOf<Alert>())
        Velocity.evaluate(velocityContext, resp.writer, "/web/signup.html", templateBuilder.toString())
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val name = req.getParameter("name")
        val password = req.getParameter("password")
        val confirmPassword = req.getParameter("confirm_password")
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        var profile = profileProvider.getProfile(name)
        val alerts = mutableListOf<Alert>()
        if (profile == null) {
            if (password == confirmPassword) {
                if (name.matches(Regex("[A-z0-9_]{3,16}"))) {
                    val passwordRuleResult = plugin.passwordValidator.validate(PasswordData(name, password))
                    if (passwordRuleResult.isValid) {
                        profile = RPKProfileImpl(
                                name = name,
                                password = password
                        )
                        profileProvider.addProfile(profile)
                        profileProvider.setActiveProfile(req, profile)
                        resp.sendRedirect("/profiles/")
                        return
                    } else {
                        passwordRuleResult.details.forEach { ruleResultDetail ->
                            alerts.add(Alert(DANGER, when (ruleResultDetail.errorCode) {
                                LengthRule.ERROR_CODE_MIN -> "Password must be at least ${ruleResultDetail.parameters["minimumLength"]} characters long."
                                LengthRule.ERROR_CODE_MAX -> "Password may not be longer than ${ruleResultDetail.parameters["maximumLength"]} characters long."
                                UpperCase.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} upper case characters."
                                LowerCase.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} lower case characters."
                                Digit.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} digits."
                                Special.errorCode -> "Password must contain at least ${ruleResultDetail.parameters["minimumRequired"]} special characters."
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
                    alerts.add(Alert(DANGER, "Your name must be between 3 and 16 characters, including alphanumerics and underscores only."))
                }
            } else {
                alerts.add(Alert(DANGER, "The passwords you entered do not match."))
            }
        } else {
            alerts.add(Alert(DANGER, "A user by that name already exists."))
        }
        resp.contentType = "text/html"
        resp.status = HttpServletResponse.SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/signup.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        scanner.close()
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", alerts)
        Velocity.evaluate(velocityContext, resp.writer, "/web/signup.html", templateBuilder.toString())
    }

}