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

public class ChatChannelSetMatchPatternCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;
    private final ConversationFactory conversationFactory;

    public ChatChannelSetMatchPatternCommand(ElysiumChatBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new ChatChannelPrompt())
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
            if (sender.hasPermission("elysium.chat.command.chatchannel.set.matchpattern")) {
                conversationFactory.buildConversation((Conversable) sender).begin();
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-set-matchpattern")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class ChatChannelPrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            return plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class).getChatChannel(input) != null;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-chatchannel-invalid-chatchannel"));
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            context.setSessionData("channel", input);
            return new ChatChannelMatchPatternPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-chatchannel-prompt"));
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
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
            BukkitChatChannel chatChannel = chatChannelProvider.getChatChannel((String) context.getSessionData("channel"));
            chatChannel.setMatchPattern((String) context.getSessionData("match_pattern"));
            chatChannelProvider.updateChatChannel(chatChannel);
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-match-pattern-valid"));
        }

    }

}
