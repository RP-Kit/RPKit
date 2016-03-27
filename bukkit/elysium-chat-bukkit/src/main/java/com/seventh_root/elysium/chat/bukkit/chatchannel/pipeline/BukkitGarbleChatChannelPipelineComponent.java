package com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline;

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent;
import com.seventh_root.elysium.api.chat.ChatMessageContext;
import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.core.util.MathUtils;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Random;

import static com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent.Type.PRE_PROCESSOR;

public class BukkitGarbleChatChannelPipelineComponent extends ChatChannelPipelineComponent {

    private double clearRange;

    public BukkitGarbleChatChannelPipelineComponent(double clearRange) {
        this.clearRange = clearRange;
    }

    public void setClearRange(double clearRange) {
        this.clearRange = clearRange;
    }

    @Override
    public Type getType() {
        return PRE_PROCESSOR;
    }

    @Override
    public String process(String message, ChatMessageContext context) {
        ElysiumPlayer sender = context.getSender();
        ElysiumPlayer receiver = context.getReceiver();
        if (sender instanceof BukkitPlayer && receiver instanceof BukkitPlayer) {
            OfflinePlayer senderOfflineBukkitPlayer = ((BukkitPlayer) sender).getBukkitPlayer().getPlayer();
            OfflinePlayer receiverOfflineBukkitPlayer = ((BukkitPlayer) receiver).getBukkitPlayer().getPlayer();
            if (senderOfflineBukkitPlayer.isOnline() && receiverOfflineBukkitPlayer.isOnline()) {
                Player senderBukkitPlayer = senderOfflineBukkitPlayer.getPlayer();
                Player receiverBukkitPlayer = receiverOfflineBukkitPlayer.getPlayer();
                if (senderBukkitPlayer.hasLineOfSight(receiverBukkitPlayer)) {
                    double distance = MathUtils.fastSqrt(senderBukkitPlayer.getLocation().distanceSquared(receiverBukkitPlayer.getLocation()));
                    double hearingRange = (double) context.getChatChannel().getRadius();
                    double clarity = 1.0D - ((distance - clearRange) / hearingRange);
                    return garbleMessage(message, clarity);
                } else {
                    return garbleMessage(message, 0D);
                }
            }
        }
        return message;
    }

    private String garbleMessage(String message, double clarity) {
        StringBuilder newMessage = new StringBuilder();
        Random random = new Random();
        int i = 0;
        int drops = 0;
        while (i < message.length()) {
            int c = message.codePointAt(i);
            i += Character.charCount(c);
            if (random.nextDouble() < clarity) {
                newMessage.appendCodePoint(c);
            } else if (random.nextDouble() < 0.1D) {
                newMessage.append(ChatColor.DARK_GRAY);
                newMessage.appendCodePoint(c);
                newMessage.append(ChatColor.WHITE);
            } else {
                newMessage.append(' ');
                drops++;
            }
        }
        if (drops == message.length()) {
            return "~~~";
        }
        return newMessage.toString();
    }

}
