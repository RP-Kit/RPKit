package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import com.rpkit.experience.bukkit.event.experience.RPKBukkitExperienceChangeEvent
import org.nfunk.jep.JEP
import kotlin.math.roundToInt


class RPKExperienceProviderImpl(private val plugin: RPKExperienceBukkit): RPKExperienceProvider {

    override fun getLevel(character: RPKCharacter): Int {
        val experience = getExperience(character)
        var level = 1
        while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= experience) {
            level++
        }
        return level
    }

    override fun setLevel(character: RPKCharacter, level: Int) {
        setExperience(character, getExperienceNeededForLevel(level))
    }

    override fun getExperience(character: RPKCharacter): Int {
        val experienceTable = plugin.core.database.getTable(RPKExperienceTable::class)
        val experienceValue = experienceTable.get(character)
        return experienceValue?.value?:0
    }

    override fun setExperience(character: RPKCharacter, experience: Int) {
        val event = RPKBukkitExperienceChangeEvent(character, getExperience(character), experience)
        plugin.server.pluginManager.callEvent(event)
        if (event.isCancelled) return
        val experienceTable = plugin.core.database.getTable(RPKExperienceTable::class)
        var experienceValue = experienceTable.get(character)
        if (experienceValue == null) {
            experienceValue = RPKExperienceValue(character = character, value = event.experience)
            experienceTable.insert(experienceValue)
        } else {
            experienceValue.value = event.experience
            experienceTable.update(experienceValue)
        }
        var level = 1
        while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= event.experience) {
            level++
        }
        val isMaxLevel = level == plugin.config.getInt("levels.max-level")
        val minecraftProfile = character.minecraftProfile
        if (minecraftProfile != null) {
            val bukkitPlayer = plugin.server.getPlayer(minecraftProfile.minecraftUUID)
            if (bukkitPlayer != null) {
                bukkitPlayer.level = level
                if (isMaxLevel) {
                    bukkitPlayer.exp = 0F
                } else {
                    bukkitPlayer.exp = (event.experience - getExperienceNeededForLevel(level)).toFloat() / (getExperienceNeededForLevel(level + 1) - getExperienceNeededForLevel(level)).toFloat()
                }
            }
        }
    }

    override fun getExperienceNeededForLevel(level: Int): Int {
        val expression = plugin.config.getString("experience.equation")
        val parser = JEP()
        parser.addStandardConstants()
        parser.addStandardFunctions()
        parser.addVariable("level", level.toDouble())
        parser.parseExpression(expression)
        return parser.value.roundToInt()
    }

}