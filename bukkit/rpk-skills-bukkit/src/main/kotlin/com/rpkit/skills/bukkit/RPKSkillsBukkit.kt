package com.rpkit.skills.bukkit

import com.rpkit.core.bukkit.plugin.RPKBukkitPlugin
import com.rpkit.core.database.Database
import com.rpkit.skills.bukkit.command.SkillCommand
import com.rpkit.skills.bukkit.database.table.RPKSkillCooldownTable
import com.rpkit.skills.bukkit.skills.RPKSkillProviderImpl
import com.rpkit.skills.bukkit.skills.RPKSkillTypeProviderImpl


class RPKSkillsBukkit: RPKBukkitPlugin() {

    override fun onEnable() {
        saveDefaultConfig()
        serviceProviders = arrayOf(
                RPKSkillTypeProviderImpl(this),
                RPKSkillProviderImpl(this)
        )
    }

    override fun onPostEnable() {
        core.serviceManager.getServiceProvider(RPKSkillProviderImpl::class).init()
    }

    override fun registerCommands() {
        getCommand("skill")?.setExecutor(SkillCommand(this))
    }

    override fun createTables(database: Database) {
        database.addTable(RPKSkillCooldownTable(database, this))
    }

    override fun setDefaultMessages() {
        messages.setDefault("skill-valid", "&aUsed \$skill.")
        messages.setDefault("skill-invalid-on-cooldown", "&c\$skill is on cooldown for \$cooldown seconds.")
        messages.setDefault("skill-invalid-not-enough-mana", "&c\$skill requires \$mana-cost mana, you have \$mana/\$max-mana")
        messages.setDefault("skill-invalid-unmet-prerequisites", "&cYou do not meet the prerequisites for \$skill.")
        messages.setDefault("skill-invalid-skill", "&cThere is no skill by that name.")
        messages.setDefault("skill-list-title", "&fSkills: ")
        messages.setDefault("skill-list-item", "&f- &7\$skill")
        messages.setDefault("no-character", "&cYou need a character to perform that command.")
        messages.setDefault("not-from-console", "&cYou must be a player to perform that command.")
        messages.setDefault("no-permission-skill", "&cYou do not have permission to use skills.")
    }

}