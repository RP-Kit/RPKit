package com.seventh_root.elysium.chat.bukkit.command.chatchannel;

import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannel;
import com.seventh_root.elysium.chat.bukkit.chatchannel.BukkitChatChannelProvider;
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class ChatChannelSpeakCommand implements CommandExecutor {

    private final ElysiumChatBukkit plugin;
    private final ConversationFactory conversationFactory;

    public ChatChannelSpeakCommand(ElysiumChatBukkit plugin) {
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
            if (sender.hasPermission("elysium.chat.command.chatchannel.speak")) {
                BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                if (chatChannelProvider.getChatChannels().size() > 0) {
                    Player bukkitPlayer = (Player) sender;
                    BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                    if (args.length > 0) {
                        StringBuilder chatChannelBuilder = new StringBuilder();
                        for (int i = 0; i < args.length - 1; i++) {
                            chatChannelBuilder.append(args[i]).append(' ');
                        }
                        chatChannelBuilder.append(args[args.length - 1]);
                        BukkitChatChannel chatChannel = chatChannelProvider.getChatChannel(chatChannelBuilder.toString());
                        if (chatChannel != null) {
                            if (sender.hasPermission("elysium.chat.command.chatchannel.speak." + chatChannel.getName())) {
                                BukkitChatChannel channel = chatChannelProvider.getChatChannel(chatChannelBuilder.toString());
                                BukkitChatChannel oldChannel = chatChannelProvider.getPlayerChannel(player);
                                if (oldChannel != null) {
                                    oldChannel.removeSpeaker(player);
                                    chatChannelProvider.updateChatChannel(oldChannel);
                                }
                                channel.addSpeaker(player);
                                channel.addListener(player);
                                chatChannelProvider.updateChatChannel(channel);
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-speak-valid").replace("$channel", chatChannel.getName())));
                            } else {
                                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-speak-channel")).replace("$channel", chatChannel.getName()));
                            }
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-speak-invalid-chatchannel")));
                        }
                    } else {
                        conversationFactory.buildConversation(bukkitPlayer).begin();
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-chat-channels-available")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-chatchannel-speak")));
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
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                Player bukkitPlayer = (Player) conversable;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitChatChannel channel = chatChannelProvider.getChatChannel(input);
                BukkitChatChannel oldChannel = chatChannelProvider.getPlayerChannel(player);
                if (oldChannel != null) {
                    oldChannel.removeSpeaker(player);
                    chatChannelProvider.updateChatChannel(oldChannel);
                }
                channel.addSpeaker(player);
                channel.addListener(player);
                chatChannelProvider.updateChatChannel(channel);
                context.setSessionData("channel", channel);
            }
            return new ChatChannelSpeakingPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-speak-invalid-chatchannel"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            BukkitChatChannelProvider chatChannelProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitChatChannelProvider.class);
            StringBuilder channelListBuilder = new StringBuilder();
            for (BukkitChatChannel channel : chatChannelProvider.getChatChannels()) {
                channelListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-list-item")
                        .replace("$color", ChatColorUtils.closestChatColorToColor(channel.getColor()).toString())
                        .replace("$name", channel.getName())))
                        .append("\n");
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.chatchannel-speak-prompt") + "\n" + channelListBuilder.toString());
        }

    }

    private class ChatChannelSpeakingPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&',
                    plugin.getConfig().getString("messages.chatchannel-speak-valid")
                            .replace("$channel", ((BukkitChatChannel) context.getSessionData("channel")).getName())
            );
        }

    }

}
