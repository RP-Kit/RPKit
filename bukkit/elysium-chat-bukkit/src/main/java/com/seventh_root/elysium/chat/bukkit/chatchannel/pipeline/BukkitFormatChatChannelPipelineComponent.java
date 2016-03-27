package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline;

import com.seventh_root.elysium.api.character.ElysiumCharacter;
import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent;
import com.seventh_root.elysium.api.chat.ChatMessageContext;
import com.seventh_root.elysium.api.chat.ElysiumChatChannel;
import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.exception.ChatChannelMessageFormattingFailureException;
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils;
import org.bukkit.ChatColor;

import static com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.FORMATTER;

public class BukkitFormatChatChannelPipelineComponent extends ChatChannelPipelineComponent {

    private final ElysiumChatBukkit plugin;
    private String formatString;

    public BukkitFormatChatChannelPipelineComponent(ElysiumChatBukkit plugin, String formatString) {
        this.plugin = plugin;
        this.formatString = formatString;
    }

    public void setFormatString(String formatString) {
        this.formatString = formatString;
    }

    @Override
    public Type getType() {
        return FORMATTER;
    }

    @Override
    public String process(String message, ChatMessageContext context) throws ChatChannelMessageFormattingFailureException {
        BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
        ElysiumPlayer sender = context.getSender();
        ElysiumPlayer receiver = context.getReceiver();
        ElysiumCharacter senderCharacter = characterProvider.getActiveCharacter(sender);
        ElysiumCharacter receiverCharacter = characterProvider.getActiveCharacter(receiver);
        ElysiumChatChannel chatChannel = context.getChatChannel();
        String formattedMessage = ChatColor.translateAlternateColorCodes('&', formatString);
        if (formattedMessage.contains("$message")) {
            if (message != null) {
                formattedMessage = formattedMessage.replace("$message", message);
            } else {
                throw new ChatChannelMessageFormattingFailureException(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-message")));
            }
        }
        if (formattedMessage.contains("$sender-player")) {
            if (sender != null) {
                formattedMessage = formattedMessage.replace("$sender-player", sender.getName());
            } else {
                return null;
            }
        }
        if (formattedMessage.contains("$sender-character")) {
            if (senderCharacter != null) {
                formattedMessage = formattedMessage.replace("$sender-character", senderCharacter.getName());
            } else {
                throw new ChatChannelMessageFormattingFailureException(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-character")));
            }
        }
        if (formattedMessage.contains("$receiver-player")) {
            if (receiver != null) {
                formattedMessage = formattedMessage.replace("$receiver-player", receiver.getName());
            } else {
                return null;
            }
        }
        if (formattedMessage.contains("$receiver-character")) {
            if (receiverCharacter != null) {
                formattedMessage = formattedMessage.replace("$receiver-character", receiverCharacter.getName());
            } else {
                return null;
            }
        }
        if (formattedMessage.contains("$channel")) {
            if (chatChannel != null) {
                formattedMessage = formattedMessage.replace("$channel", chatChannel.getName());
            } else {
                throw new ChatChannelMessageFormattingFailureException(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-channel")));
            }
        }
        if (formattedMessage.contains("$color") || formattedMessage.contains("$colour")) {
            if (chatChannel != null) {
                String chatColorString = ChatColorUtils.closestChatColorToColor(chatChannel.getColor()).toString();
                formattedMessage = formattedMessage.replace("$color", chatColorString)
                        .replace("$colour", chatColorString);
            } else {
                throw new ChatChannelMessageFormattingFailureException(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-channel")));
            }
        }
        return formattedMessage;
    }

}
