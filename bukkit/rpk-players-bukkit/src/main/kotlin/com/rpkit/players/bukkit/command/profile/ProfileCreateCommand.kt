package com.rpkit.players.bukkit.command.profile

import com.rpkit.players.bukkit.RPKPlayersBukkit
import com.rpkit.players.bukkit.profile.RPKMinecraftProfileProvider
import com.rpkit.players.bukkit.profile.RPKProfileImpl
import com.rpkit.players.bukkit.profile.RPKProfileProvider
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.passay.*


class ProfileCreateCommand(private val plugin: RPKPlayersBukkit): CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) {
            sender.sendMessage(plugin.messages["not-from-console"])
            return true
        }
        if (!sender.hasPermission("rpkit.players.command.profile.create")) {
            sender.sendMessage(plugin.messages["no-permission-profile-create"])
            return true
        }
        val minecraftProfileProvider = plugin.core.serviceManager.getServiceProvider(RPKMinecraftProfileProvider::class)
        val minecraftProfile = minecraftProfileProvider.getMinecraftProfile(sender)
        if (minecraftProfile == null) {
            sender.sendMessage(plugin.messages["no-minecraft-profile"])
            return true
        }
        val profile = minecraftProfile.profile
        if (profile != null) {
            sender.sendMessage(plugin.messages["profile-create-invalid-profile"])
            return true
        }
        if (args.size < 2) {
            sender.sendMessage(plugin.messages["profile-create-usage"])
            return true
        }
        val profileProvider = plugin.core.serviceManager.getServiceProvider(RPKProfileProvider::class)
        val name = args[0]
        val password = args[1]
        val passwordRuleResult = plugin.passwordValidator.validate(PasswordData(name, password))
        if (!passwordRuleResult.isValid) {
            passwordRuleResult.details.forEach { ruleResultDetail ->
                val error = when (ruleResultDetail.errorCode) {
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
                }
                sender.sendMessage(ChatColor.RED.toString() + error)
            }
            return true
        }
        val newProfile = RPKProfileImpl(name, password)
        profileProvider.addProfile(newProfile)
        minecraftProfile.profile = newProfile
        minecraftProfileProvider.updateMinecraftProfile(minecraftProfile)
        sender.sendMessage(plugin.messages["profile-create-valid"])
        return true
    }
}