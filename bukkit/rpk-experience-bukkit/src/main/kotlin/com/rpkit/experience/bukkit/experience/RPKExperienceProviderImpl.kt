package com.rpkit.experience.bukkit.experience

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.experience.bukkit.RPKExperienceBukkit
import com.rpkit.experience.bukkit.database.table.RPKExperienceTable
import javax.script.ScriptContext.ENGINE_SCOPE
import javax.script.ScriptEngineManager


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
        val experienceTable = plugin.core.database.getTable(RPKExperienceTable::class)
        var experienceValue = experienceTable.get(character)
        if (experienceValue == null) {
            experienceValue = RPKExperienceValue(character = character, value = experience)
            experienceTable.insert(experienceValue)
        } else {
            experienceValue.value = experience
            experienceTable.update(experienceValue)
        }
        var level = 1
        while (level + 1 <= plugin.config.getInt("levels.max-level") && getExperienceNeededForLevel(level + 1) <= experience) {
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
                    bukkitPlayer.exp = (experience - getExperienceNeededForLevel(level)).toFloat() / (getExperienceNeededForLevel(level + 1) - getExperienceNeededForLevel(level)).toFloat()
                }
            }
        }
    }

    override fun getExperienceNeededForLevel(level: Int): Int {
        val engineManager = ScriptEngineManager()
        val engine = engineManager.getEngineByName("nashorn")
        val bindings = engine.createBindings()
        bindings["level"] = level
        engine.setBindings(bindings, ENGINE_SCOPE)
        return Math.round((engine.eval(plugin.config.getString("experience.equation")) as Number).toDouble()).toInt()
    }

}