package com.rpkit.skills.bukkit.command

import com.rpkit.characters.bukkit.character.RPKCharacterProvider
import com.rpkit.players.bukkit.player.RPKPlayerProvider
import com.rpkit.skills.bukkit.RPKSkillsBukkit
import com.rpkit.skills.bukkit.skills.RPKSkillProvider
import com.rpkit.skills.bukkit.skills.canUse
import com.rpkit.skills.bukkit.skills.use
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


class SkillCommand(private val plugin: RPKSkillsBukkit): CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender.hasPermission("rpkit.skills.command.skill")) {
            if (sender is Player) {
                val playerProvider = plugin.core.serviceManager.getServiceProvider(RPKPlayerProvider::class)
                val characterProvider = plugin.core.serviceManager.getServiceProvider(RPKCharacterProvider::class)
                val skillProvider = plugin.core.serviceManager.getServiceProvider(RPKSkillProvider::class)
                val player = playerProvider.getPlayer(sender)
                val character = characterProvider.getActiveCharacter(player)
                if (character != null) {
                    if (args.isNotEmpty()) {
                        val skill = skillProvider.getSkill(args[0])
                        if (skill != null) {
                            if (character.canUse(skill)) {
                                if (character.mana >= skill.manaCost) {
                                    if (skillProvider.getSkillCooldown(character, skill) <= 0) {
                                        character.use(skill)
                                        skillProvider.setSkillCooldown(character, skill, skill.cooldown)
                                        character.mana -= skill.manaCost
                                        characterProvider.updateCharacter(character)
                                        sender.sendMessage(plugin.messages["skill-valid", mapOf(
                                                Pair("skill", skill.name)
                                        )])
                                    } else {
                                        sender.sendMessage(plugin.messages["skill-invalid-on-cooldown", mapOf(
                                                Pair("skill", skill.name),
                                                Pair("cooldown", skillProvider.getSkillCooldown(character, skill).toString())
                                        )])
                                    }
                                } else {
                                    sender.sendMessage(plugin.messages["skill-invalid-not-enough-mana", mapOf(
                                            Pair("skill", skill.name),
                                            Pair("mana-cost", skill.manaCost.toString()),
                                            Pair("mana", character.mana.toString()),
                                            Pair("max-mana", character.maxMana.toString())
                                    )])
                                }
                            } else {
                                sender.sendMessage(plugin.messages["skill-invalid-unmet-prerequisites", mapOf(
                                        Pair("skill", skill.name)
                                )])
                            }
                        } else {
                            sender.sendMessage(plugin.messages["skill-invalid-skill"])
                        }
                    } else {
                        sender.sendMessage(plugin.messages["skill-list-title"])
                        skillProvider.skills
                                .filter { skill -> character.canUse(skill) }
                                .forEach { skill ->
                                    sender.sendMessage(plugin.messages["skill-list-item", mapOf(
                                            Pair("skill", skill.name)
                                    )])
                                }
                    }
                } else {
                    sender.sendMessage(plugin.messages["no-character"])
                }
            } else {
                sender.sendMessage(plugin.messages["not-from-console"])
            }
        } else {
            sender.sendMessage(plugin.messages["no-permission-skill"])
        }
        return true
    }

}