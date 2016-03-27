package com.seventh_root.elysium.chat.bukkit.command.chatchannel;

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class ChatChannelCreateCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;
    private final ConversationFactory conversationFactory;

    public ChatChannelCreateCommand(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new ChatChannelNamePrompt())
                .withEscapeSequence("cancel")
                .thatExcludesNonPlayersWithMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")))
                .addConversationAbandonedListener(event -> {
                    if (!event.gracefulExit()) {
                        Conversable conversable = event.getContext().getForWhom();
                        if (conversable instanceof Player) {
                            Player player = (Player) conversable;
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.operation-cancelled")));
                        }
                    }
                });
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Conversable) {
            if (sender.hasPermission("elysium.chat.command.chatchannel.create")) {
                conversationFactory.buildConversation((Conversable) sender).begin();
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-create")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class ChatChannelNamePrompt extends ValidatingPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-create-prompt"));
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
            return chatChannelProvider.getChatChannel(input) == null;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-name-invalid-name"));
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            context.setSessionData("name", input);
            return new ChatChannelNameSetPrompt();
        }

    }

    private class ChatChannelNameSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ChatChannelColorPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-name-valid"));
        }

    }

    private class ChatChannelColorPrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            try {
                ChatColor.valueOf(input.toUpperCase().replace(' ', '_'));
                return true;
            } catch (IllegalArgumentException exception) {
                return false;
            }
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            context.setSessionData("color", ChatColor.valueOf(input.toUpperCase().replace(' ', '_')));
            return new ChatChannelColorSetPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-color-prompt"));
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-color-invalid-color"));
        }

    }

    private class ChatChannelColorSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ChatChannelFormatStringPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-color-valid"));
        }

    }

    private class ChatChannelFormatStringPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-formatstring-prompt"));
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData("format_string", input);
            return new ChatChannelFormatStringSetPrompt();
        }

    }

    private class ChatChannelFormatStringSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ChatChannelRadiusPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-formatstring-valid"));
        }

    }

    private class ChatChannelRadiusPrompt extends NumericPrompt {

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            context.setSessionData("radius", input.intValue());
            return new ChatChannelRadiusSetPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-radius-prompt"));
        }

    }

    private class ChatChannelRadiusSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if ((int) context.getSessionData("radius") > 0) {
                return new ChatChannelClearRadiusPrompt();
            } else {
                context.setSessionData("clear_radius", 0);
                return new ChatChannelMatchPatternPrompt();
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-radius-valid"));
        }

    }

    private class ChatChannelClearRadiusPrompt extends NumericPrompt {

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            context.setSessionData("clear_radius", input.intValue());
            return new ChatChannelClearRadiusSetPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-clear-radius-prompt"));
        }

    }

    private class ChatChannelClearRadiusSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ChatChannelMatchPatternPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-clear-radius-valid"));
        }

    }

    private class ChatChannelMatchPatternPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-match-pattern-prompt"));
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (!input.equalsIgnoreCase("none"))
                context.setSessionData("match_pattern", input);
            else
                context.setSessionData("match_pattern", "");
            return new ChatChannelMatchPatternSetPrompt();
        }

    }

    private class ChatChannelMatchPatternSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if ((int) context.getSessionData("radius") <= 0) {
                return new ChatChannelIRCEnabledPrompt();
            } else {
                context.setSessionData("irc_enabled", false);
                context.setSessionData("irc_channel", "");
                context.setSessionData("irc_whitelist", false);
                return new ChatChannelJoinedByDefaultPrompt();
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-match-pattern-valid"));
        }

    }

    private class ChatChannelIRCEnabledPrompt extends BooleanPrompt {

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel.set-irc-enabled-invalid-boolean"));
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            context.setSessionData("irc_enabled", input);
            return new ChatChannelIRCEnabledSetPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-enabled-prompt"));
        }

    }

    private class ChatChannelIRCEnabledSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            if ((boolean) context.getSessionData("irc_enabled")) {
                return new ChatChannelIRCChannelPrompt();
            } else {
                context.setSessionData("irc_channel", "");
                context.setSessionData("irc_whitelist", false);
                return new ChatChannelJoinedByDefaultPrompt();
            }
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-enabled-valid"));
        }

    }

    private class ChatChannelIRCChannelPrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            return input.matches("/([#&][^\\x07\\x2C\\s]{0,200})/");
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-channel-invalid-irc-channel"));
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            context.setSessionData("irc_channel", input);
            return new ChatChannelIRCChannelSetPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-channel-prompt"));
        }

    }

    private class ChatChannelIRCChannelSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ChatChannelIRCWhitelistPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-channel-valid"));
        }

    }

    private class ChatChannelIRCWhitelistPrompt extends BooleanPrompt {

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            context.setSessionData("irc_whitelist", input);
            return new ChatChannelIRCWhitelistSetPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-whitelist-invalid-boolean"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-whitelist-prompt"));
        }

    }

    private class ChatChannelIRCWhitelistSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ChatChannelJoinedByDefaultPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-irc-whitelist-valid"));
        }

    }

    private class ChatChannelJoinedByDefaultPrompt extends BooleanPrompt {

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            context.setSessionData("joined_by_default", input);
            return new ChatChannelJoinedByDefaultSetPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-joined-by-default-invalid-boolean"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-joined-by-default-prompt"));
        }

    }

    private class ChatChannelJoinedByDefaultSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new ChatChannelCreatedPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-joined-by-default-valid"));
        }
    }

    private class ChatChannelCreatedPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
            String name = (String) context.getSessionData("name");
            ChatColor color = (ChatColor) context.getSessionData("color");
            String formatString = (String) context.getSessionData("format_string");
            int radius = (int) context.getSessionData("radius");
            int clearRadius = (int) context.getSessionData("clear_radius");
            String matchPattern = (String) context.getSessionData("match_pattern");
            boolean ircEnabled = (boolean) context.getSessionData("irc_enabled");
            String ircChannel = (String) context.getSessionData("irc_channel");
            boolean ircWhitelist = (boolean) context.getSessionData("irc_whitelist");
            boolean joinedByDefault = (boolean) context.getSessionData("joined_by_default");
            BukkitChatChannel chatChannel = new BukkitChatChannel.Builder(plugin)
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
                    .build();
            chatChannelProvider.addChatChannel(chatChannel);
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-create-valid"));
        }

    }

}
