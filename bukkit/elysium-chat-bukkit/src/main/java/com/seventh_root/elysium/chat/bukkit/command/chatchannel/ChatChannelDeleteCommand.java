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

public class ChatChannelDeleteCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;
    private final ConversationFactory conversationFactory;

    public ChatChannelDeleteCommand(ElysiumChatBukkit plugin) {
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
            if (sender.hasPermission("elysium.chat.command.chatchannel.delete")) {
                BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
                if (chatChannelProvider.getChatChannels().size() > 0) {
                    if (args.length > 0) {
                        StringBuilder chatChannelBuilder = new StringBuilder();
                        for (int i = 0; i < args.length - 1; i++) {
                            chatChannelBuilder.append(args[i]).append(' ');
                        }
                        chatChannelBuilder.append(args[args.length - 1]);
                        BukkitChatChannel chatChannel = chatChannelProvider.getChatChannel(chatChannelBuilder.toString());
                        if (chatChannel != null) {
                            chatChannelProvider.removeChatChannel(chatChannel);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-delete-valid")));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-delete-invalid-chatchannel")));
                        }
                    } else {
                        conversationFactory.buildConversation((Conversable) sender).begin();
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-chat-channels-available")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-delete")));
            }
        }
        return true;
    }

    private class ChatChannelPrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            return plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class).getChatChannel(input) != null;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
                BukkitChatChannel channel = chatChannelProvider.getChatChannel(input);
                chatChannelProvider.removeChatChannel(channel);
            }
            return new ChatChannelDeletedPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-delete-invalid-chatchannel"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-delete-prompt"));
        }

    }

    private class ChatChannelDeletedPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-delete-valid"));
        }

    }

}
