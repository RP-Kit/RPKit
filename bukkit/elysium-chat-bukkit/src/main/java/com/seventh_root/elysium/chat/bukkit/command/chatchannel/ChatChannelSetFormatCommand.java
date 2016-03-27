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

public class ChatChannelSetFormatCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;
    private final ConversationFactory conversationFactory;

    public ChatChannelSetFormatCommand(ElysiumChatBukkit plugin) {
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
        if (sender instanceof Player) {
            if (sender.hasPermission("elysium.chat.command.chatchannel.set.format")) {
                Player bukkitPlayer = (Player) sender;
                BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
                if (args.length > 0) {
                    BukkitChatChannel chatChannel = chatChannelProvider.getChatChannel(args[0]);
                    StringBuilder formatBuilder = new StringBuilder();
                    for (int i = 1; i < args.length - 1; i++) {
                        formatBuilder.append(args[i]).append(" ");
                    }
                    formatBuilder.append(args[args.length - 1]);
                    chatChannel.setName(formatBuilder.toString());
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-formatstring-valid")));
                } else {
                    conversationFactory.buildConversation(bukkitPlayer).begin();
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-set-format")));
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
            return new ChatChannelFormatStringPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-chatchannel-prompt"));
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
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
            BukkitChatChannel chatChannel = chatChannelProvider.getChatChannel((String) context.getSessionData("channel"));
            chatChannel.setFormatString((String) context.getSessionData("format_string"));
            chatChannelProvider.updateChatChannel(chatChannel);
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-set-formatstring-valid"));
        }

    }

}
