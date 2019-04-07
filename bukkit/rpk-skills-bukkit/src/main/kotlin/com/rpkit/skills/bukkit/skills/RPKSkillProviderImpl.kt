package com.rpkit.skills.bukkit.skills

import com.rpkit.characters.bukkit.character.RPKCharacter
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.database.table.RPKSkillCooldownTable
import java.io.File
import java.nio.charset.Charset
import javax.script.ScriptContext
import javax.script.ScriptEngineManager


class RPKSkillProviderImpl(private val plugin: RPKSkillsBukkit): RPKSkillProvider {

    override val skills: MutableList<RPKSkill> = mutableListOf()

    fun init() {
        val skillsDirectory = File(plugin.dataFolder, "skills")
        if (!skillsDirectory.exists()) {
            skillsDirectory.mkdirs()
            plugin.saveResource("skills/fireball.js", false)
        }
        val originalClassLoader = Thread.currentThread().contextClassLoader
        Thread.currentThread().contextClassLoader = plugin.javaClass.classLoader
        val engineManager = ScriptEngineManager()
        for (file in skillsDirectory.listFiles()) {
            val engine = engineManager.getEngineByName("nashorn")
            val bindings = engine.createBindings()
            bindings["core"] = plugin.core
            engine.setBindings(bindings, ScriptContext.ENGINE_SCOPE)
            file.reader(Charset.forName("UTF-8")).use { reader ->
                engine.eval(reader)
            }
            val skill = engine.get("skill") as? RPKSkill
            if (skill != null) {
                addSkill(skill)
            } else {
                plugin.logger.warning("Failed to load skill from $file")
            }
        }
        Thread.currentThread().contextClassLoader = originalClassLoader
    }

    override fun getSkill(name: String): RPKSkill? {
        return skills.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    override fun addSkill(skill: RPKSkill) {
        skills.add(skill)
    }

    override fun removeSkill(skill: RPKSkill) {
        skills.remove(skill)
    }

    override fun getSkillCooldown(character: RPKCharacter, skill: RPKSkill): Int {
        val skillCooldownTable = plugin.core.database.getTable(RPKSkillCooldownTable::class)
        val skillCooldown = skillCooldownTable.get(character, skill) ?: return 0
        return Math.max(0, Math.ceil((skillCooldown.cooldownTimestamp - System.currentTimeMillis()) / 1000.0).toInt())
    }

    override fun setSkillCooldown(character: RPKCharacter, skill: RPKSkill, seconds: Int) {
        val skillCooldownTable = plugin.core.database.getTable(RPKSkillCooldownTable::class)
        var skillCooldown = skillCooldownTable.get(character, skill)
        if (skillCooldown == null) {
            skillCooldown = RPKSkillCooldown(
                    character = character,
                    skill = skill,
                    cooldownTimestamp = System.currentTimeMillis() + (seconds * 1000)
            )
            skillCooldownTable.insert(skillCooldown)
        } else {
            skillCooldown.cooldownTimestamp = System.currentTimeMillis() + (seconds * 1000)
            skillCooldownTable.update(skillCooldown)
        }
    }

}