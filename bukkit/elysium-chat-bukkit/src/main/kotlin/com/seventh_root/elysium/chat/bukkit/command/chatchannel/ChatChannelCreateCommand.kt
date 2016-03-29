package com.seventh_root.elysium.chat.bukkit.command.chatchannel

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.conversations.*
import org.bukkit.entity.Player
import java.awt.Color

class ChatChannelCreateCommand(private val plugin: ElysiumChatBukkit) : CommandExecutor {
    private val conversationFactory: ConversationFactory

    init {
        conversationFactory = ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(ChatChannelNamePrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
                .addConversationAbandonedListener { event ->
            if (!event.gracefulExit()) {
                val conversable = event.context.forWhom
                if (conversable is Player) {
                    conversable.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.operation-cancelled")))
                }
            }
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        if (sender is Conversable) {
            if (sender.hasPermission("elysium.chat.command.chatchannel.create")) {
                conversationFactory.buildConversation(sender).begin()
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.no-permission-chatchannel-create")))
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.not-from-console")))
        }
        return true
    }

    private inner class ChatChannelNamePrompt : ValidatingPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-create-prompt"))
        }

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            val chatChannelProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
            return chatChannelProvider.getChatChannel(input) == null
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-name-invalid-name"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("name", input)
            return ChatChannelNameSetPrompt()
        }

    }

    private inner class ChatChannelNameSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return ChatChannelColorPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-name-valid"))
        }

    }

    private inner class ChatChannelColorPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            try {
                ChatColor.valueOf(input.toUpperCase().replace(' ', '_'))
                return true
            } catch (exception: IllegalArgumentException) {
                return false
            }

        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("color", ChatColorUtils.colorFromChatColor(ChatColor.valueOf(input.toUpperCase().replace(' ', '_'))))
            return ChatChannelColorSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-color-prompt"))
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-color-invalid-color"))
        }

    }

    private inner class ChatChannelColorSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return ChatChannelFormatStringPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-color-valid"))
        }

    }

    private inner class ChatChannelFormatStringPrompt : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-formatstring-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("format_string", input)
            return ChatChannelFormatStringSetPrompt()
        }

    }

    private inner class ChatChannelFormatStringSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return ChatChannelRadiusPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-formatstring-valid"))
        }

    }

    private inner class ChatChannelRadiusPrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("radius", input.toInt())
            return ChatChannelRadiusSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-radius-prompt"))
        }

    }

    private inner class ChatChannelRadiusSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            if (context.getSessionData("radius") as Int > 0) {
                return ChatChannelClearRadiusPrompt()
            } else {
                context.setSessionData("clear_radius", 0)
                return ChatChannelMatchPatternPrompt()
            }
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-radius-valid"))
        }

    }

    private inner class ChatChannelClearRadiusPrompt : NumericPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Number): Prompt {
            context.setSessionData("clear_radius", input.toInt())
            return ChatChannelClearRadiusSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-clear-radius-prompt"))
        }

    }

    private inner class ChatChannelClearRadiusSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return ChatChannelMatchPatternPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-clear-radius-valid"))
        }

    }

    private inner class ChatChannelMatchPatternPrompt : StringPrompt() {

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-match-pattern-prompt"))
        }

        override fun acceptInput(context: ConversationContext, input: String): Prompt {
            if (!input.equals("none", ignoreCase = true))
                context.setSessionData("match_pattern", input)
            else
                context.setSessionData("match_pattern", "")
            return ChatChannelMatchPatternSetPrompt()
        }

    }

    private inner class ChatChannelMatchPatternSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            if (context.getSessionData("radius") as Int <= 0) {
                return ChatChannelIRCEnabledPrompt()
            } else {
                context.setSessionData("irc_enabled", false)
                context.setSessionData("irc_channel", "")
                context.setSessionData("irc_whitelist", false)
                return ChatChannelJoinedByDefaultPrompt()
            }
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-match-pattern-valid"))
        }

    }

    private inner class ChatChannelIRCEnabledPrompt : BooleanPrompt() {

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel.set-irc-enabled-invalid-boolean"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt {
            context.setSessionData("irc_enabled", input)
            return ChatChannelIRCEnabledSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-enabled-prompt"))
        }

    }

    private inner class ChatChannelIRCEnabledSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            if (context.getSessionData("irc_enabled") as Boolean) {
                return ChatChannelIRCChannelPrompt()
            } else {
                context.setSessionData("irc_channel", "")
                context.setSessionData("irc_whitelist", false)
                return ChatChannelJoinedByDefaultPrompt()
            }
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-enabled-valid"))
        }

    }

    private inner class ChatChannelIRCChannelPrompt : ValidatingPrompt() {

        override fun isInputValid(context: ConversationContext, input: String): Boolean {
            return input.matches("/([#&][^\\x07\\x2C\\s]{0,200})/".toRegex())
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-channel-invalid-irc-channel"))
        }

        override fun acceptValidatedInput(context: ConversationContext, input: String): Prompt {
            context.setSessionData("irc_channel", input)
            return ChatChannelIRCChannelSetPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-channel-prompt"))
        }

    }

    private inner class ChatChannelIRCChannelSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return ChatChannelIRCWhitelistPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-channel-valid"))
        }

    }

    private inner class ChatChannelIRCWhitelistPrompt : BooleanPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt {
            context.setSessionData("irc_whitelist", input)
            return ChatChannelIRCWhitelistSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-whitelist-invalid-boolean"))
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-whitelist-prompt"))
        }

    }

    private inner class ChatChannelIRCWhitelistSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return ChatChannelJoinedByDefaultPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-irc-whitelist-valid"))
        }

    }

    private inner class ChatChannelJoinedByDefaultPrompt : BooleanPrompt() {

        override fun acceptValidatedInput(context: ConversationContext, input: Boolean): Prompt {
            context.setSessionData("joined_by_default", input)
            return ChatChannelJoinedByDefaultSetPrompt()
        }

        override fun getFailedValidationText(context: ConversationContext?, invalidInput: String?): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-joined-by-default-invalid-boolean"))
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-joined-by-default-prompt"))
        }

    }

    private inner class ChatChannelJoinedByDefaultSetPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt {
            return ChatChannelCreatedPrompt()
        }

        override fun getPromptText(context: ConversationContext): String {
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-set-joined-by-default-valid"))
        }
    }

    private inner class ChatChannelCreatedPrompt : MessagePrompt() {

        override fun getNextPrompt(context: ConversationContext): Prompt? {
            return Prompt.END_OF_CONVERSATION
        }

        override fun getPromptText(context: ConversationContext): String {
            val chatChannelProvider = plugin.core!!.serviceManager.getServiceProvider(BukkitChatChannelProvider::class.java)
            val name = context.getSessionData("name") as String
            val color = context.getSessionData("color") as Color
            val formatString = context.getSessionData("format_string") as String
            val radius = context.getSessionData("radius") as Int
            val clearRadius = context.getSessionData("clear_radius") as Int
            val matchPattern = context.getSessionData("match_pattern") as String
            val ircEnabled = context.getSessionData("irc_enabled") as Boolean
            val ircChannel = context.getSessionData("irc_channel") as String
            val ircWhitelist = context.getSessionData("irc_whitelist") as Boolean
            val joinedByDefault = context.getSessionData("joined_by_default") as Boolean
            val chatChannel = BukkitChatChannel.Builder(plugin)
                    .name(name)
                    .color(color)
                    .formatString(formatString)
                    .radius(radius)
                    .clearRadius(clearRadius)
                    .matchPattern(matchPattern)
                    .ircEnabled(ircEnabled)
                    .ircChannel(ircChannel)
                    .ircWhitelist(ircWhitelist)
                    .joinedByDefault(joinedByDefault)
                    .build()
            chatChannelProvider.addChatChannel(chatChannel)
            return ChatColor.translateAlternateColorCodes('&', plugin.config.getString("messages.chatchannel-create-valid"))
        }

    }

}
