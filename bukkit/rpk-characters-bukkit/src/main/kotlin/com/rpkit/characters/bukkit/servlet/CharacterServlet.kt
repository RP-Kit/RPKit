package com.rpkit.characters.bukkit.servlet

import com.rpkit.characters.bukkit.RPKCharactersBukkit
import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.characters.bukkit.gender.RPKGenderProvider
import com.rpkit.characters.bukkit.race.RPKRaceProvider
import com.rpkit.core.web.Alert
import com.rpkit.core.web.RPKServlet
import com.rpkit.permissions.bukkit.group.RPKGroupProvider
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponse.SC_NOT_FOUND
import javax.servlet.http.HttpServletResponse.SC_OK


class CharacterServlet(private val plugin: RPKCharactersBukkit): RPKServlet() {
    override val url = "/characters/character/*"
    override fun doGet(req: HttpServletRequest, resp: HttpServletResponse) {
        val characterId = req.pathInfo?.drop(1)?.toIntOrNull()
        if (characterId == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(characterId)
        if (character == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getActiveProfile(req)
        if (profile == null) {
            resp.contentType = "text/html"
            resp.status = SC_OK
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/character.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("character", character)
            Velocity.evaluate(velocityContext, resp.writer, "/web/character.html", templateBuilder.toString())
            return
        }
        if (profile != character.profile) {
            resp.contentType = "text/html"
            resp.status = SC_OK
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/character.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("character", character)
            Velocity.evaluate(velocityContext, resp.writer, "/web/character.html", templateBuilder.toString())
            return
        }
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/character_owner.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
        val genders = genderProvider.genders
        val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
        val races = raceProvider.races
        val minAge = plugin.config.getInt("characters.min-age")
        val maxAge = plugin.config.getInt("characters.max-age")
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("character", character)
        velocityContext.put("genders", genders)
        velocityContext.put("minAge", minAge)
        velocityContext.put("maxAge", maxAge)
        velocityContext.put("races", races)
        Velocity.evaluate(velocityContext, resp.writer, "/web/character_owner.html", templateBuilder.toString())
    }

    override fun doPost(req: HttpServletRequest, resp: HttpServletResponse) {
        val characterId = req.pathInfo?.drop(1)?.toIntOrNull()
        if (characterId == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
        val character = characterProvider.getCharacter(characterId)
        if (character == null) {
            resp.contentType = "text/html"
            resp.status = SC_NOT_FOUND
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/404.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            Velocity.evaluate(velocityContext, resp.writer, "/web/404.html", templateBuilder.toString())
            return
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val profile = profileProvider.getActiveProfile(req)
        if (profile == null) {
            resp.contentType = "text/html"
            resp.status = SC_OK
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/character.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("alerts", listOf(Alert(Alert.Type.DANGER, "You have been logged out. Please log in again before trying to modify your character.")))
            velocityContext.put("character", character)
            Velocity.evaluate(velocityContext, resp.writer, "/web/character.html", templateBuilder.toString())
            return
        }
        if (profile != character.profile) {
            resp.contentType = "text/html"
            resp.status = SC_OK
            val templateBuilder = StringBuilder()
            val scanner = Scanner(javaClass.getResourceAsStream("/web/character.html"))
            while (scanner.hasNextLine()) {
                templateBuilder.append(scanner.nextLine()).append('\n')
            }
            val velocityContext = VelocityContext()
            velocityContext.put("server", plugin.server.serverName)
            velocityContext.put("navigationBar", plugin.core.web.navigationBar)
            velocityContext.put("alerts", listOf(Alert(Alert.Type.DANGER, "You do not own that character.")))
            velocityContext.put("character", character)
            Velocity.evaluate(velocityContext, resp.writer, "/web/character.html", templateBuilder.toString())
            return
        }
        val alerts = mutableListOf<Alert>()
        val permissionsProvider = plugin.core.serviceManager.getServiceProvider(RPKGroupProvider::class)
        val name = req.getParameter("name")
        if (name != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.name")) {
                character.name = name
            } else {
                alerts.add(Alert(Alert.Type.DANGER, "You do not have permission to set your character's name."))
            }
        }
        val genderProvider = plugin.core.serviceManager.getServiceProvider(RPKGenderProvider::class)
        val genderId = req.getParameter("gender")?.toInt()
        if (genderId != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.gender")) {
                val gender = genderProvider.getGender(genderId)
                character.gender = gender
            } else {
                alerts.add(Alert(Alert.Type.DANGER, "You do not have permission to set your character's gender."))
            }
        }
        val age = req.getParameter("age")?.toInt()
        if (age != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.character.set.age")) {
                character.age = age
            } else {
                alerts.add(Alert(Alert.Type.DANGER, "You do not have permission to set your character's age."))
            }
        }
        val raceProvider = plugin.core.serviceManager.getServiceProvider(RPKRaceProvider::class)
        val raceId = req.getParameter("race")?.toInt()
        if (raceId != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.set.race")) {
                val race = raceProvider.getRace(raceId)
                character.race = race
            } else {
                alerts.add(Alert(Alert.Type.DANGER, "You do not have permission to set your character's race."))
            }
        }
        val description = req.getParameter("description")
        if (description != null) {
            if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.set.description")) {
                character.description = description
            } else {
                alerts.add(Alert(Alert.Type.DANGER, "You do not have permission to set your character's description."))
            }
        }
        val dead = req.getParameter("dead")
        if (permissionsProvider.hasPermission(profile, "rpkit.characters.command.set.dead")) {
            if ((dead != null && permissionsProvider.hasPermission(profile, "rpkit.characters.command.set.dead.yes")
                    || (dead == null && permissionsProvider.hasPermission(profile, "rpkit.characters.command.set.dead.no")))) {
                character.isDead = dead != null
            } else {
                alerts.add(Alert(Alert.Type.DANGER, "You do not have permission to set your character to be ${if (dead == null) "not" else ""} dead."))
            }
        } else {
            alerts.add(Alert(Alert.Type.DANGER, "You do not have permission to set whether your character is dead."))
        }
        characterProvider.updateCharacter(character)
        resp.contentType = "text/html"
        resp.status = SC_OK
        val templateBuilder = StringBuilder()
        val scanner = Scanner(javaClass.getResourceAsStream("/web/character_owner.html"))
        while (scanner.hasNextLine()) {
            templateBuilder.append(scanner.nextLine()).append('\n')
        }
        alerts.add(Alert(Alert.Type.SUCCESS, "Character successfully updated."))
        val genders = genderProvider.genders
        val races = raceProvider.races
        val minAge = plugin.config.getInt("characters.min-age")
        val maxAge = plugin.config.getInt("characters.max-age")
        val velocityContext = VelocityContext()
        velocityContext.put("server", plugin.server.serverName)
        velocityContext.put("navigationBar", plugin.core.web.navigationBar)
        velocityContext.put("alerts", alerts)
        velocityContext.put("character", character)
        velocityContext.put("genders", genders)
        velocityContext.put("minAge", minAge)
        velocityContext.put("maxAge", maxAge)
        velocityContext.put("races", races)
        Velocity.evaluate(velocityContext, resp.writer, "/web/character_owner.html", templateBuilder.toString())
    }

}