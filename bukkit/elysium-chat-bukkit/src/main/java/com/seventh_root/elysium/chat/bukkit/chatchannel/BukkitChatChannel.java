package com.seventh_root.elysium.chat.bukkit.chatchannel;

import com.seventh_root.elysium.api.chat.ChatChannelPipelineComponent;
import com.seventh_root.elysium.api.chat.ChatMessageContext;
import com.seventh_root.elysium.api.chat.ElysiumChatChannel;
import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.chat.bukkit.ElysiumChatBukkit;
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitFormatChatChannelPipelineComponent;
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitGarbleChatChannelPipelineComponent;
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitIRCChatChannelPipelineComponent;
import com.seventh_root.elysium.chat.bukkit.chatchannel.pipeline.BukkitLogChatChannelPipelineComponent;
import com.seventh_root.elysium.chat.exception.ChatChannelMessageFormattingFailureException;
import com.seventh_root.elysium.core.bukkit.util.ChatColorUtils;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class BukkitChatChannel implements ElysiumChatChannel {

    private final ElysiumChatBukkit plugin;

    private int id;
    private String name;
    private ChatColor color;
    private String formatString;
    private int radius;
    private int clearRadius;
    private final List<ElysiumPlayer> speakers;
    private final List<ElysiumPlayer> listeners;
    private final List<ChatChannelPipelineComponent> pipeline;
    private String matchPattern;
    private boolean ircEnabled;
    private String ircChannel;
    private boolean ircWhitelist;
    private boolean joinedByDefault;

    public static class Builder {

        private final ElysiumChatBukkit plugin;

        private int id;
        private String name;
        private ChatColor color;
        private String formatString;
        private int radius;
        private int clearRadius;
        private final List<ElysiumPlayer> speakers;
        private final List<ElysiumPlayer> listeners;
        private final List<ChatChannelPipelineComponent> pipeline;
        private String matchPattern;
        private boolean ircEnabled;
        private String ircChannel;
        private boolean ircWhitelist;
        private boolean joinedByDefault;

        public Builder(ElysiumChatBukkit plugin) {
            this.plugin = plugin;
            this.id = 0;
            this.name = "";
            this.color = ChatColor.WHITE;
            this.formatString = "&f[$color$channel&f] &7$sender-player&f: $message";
            this.radius = -1;
            this.clearRadius = -1;
            this.speakers = new ArrayList<>();
            this.listeners = new ArrayList<>();
            this.pipeline = new ArrayList<>();
            this.matchPattern = "";
            this.ircEnabled = false;
            this.ircChannel = "";
            this.ircWhitelist = false;
            this.joinedByDefault = true;
        }

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder color(ChatColor color) {
            this.color = color;
            return this;
        }

        public Builder formatString(String formatString) {
            this.formatString = formatString;
            return this;
        }

        public Builder radius(int radius) {
            this.radius = radius;
            return this;
        }

        public Builder clearRadius(int clearRadius) {
            this.clearRadius = clearRadius;
            return this;
        }

        public Builder speaker(ElysiumPlayer speaker) {
            this.speakers.add(speaker);
            return this;
        }

        public Builder speakers(Collection<? extends ElysiumPlayer> speakers) {
            this.speakers.clear();
            this.speakers.addAll(speakers);
            return this;
        }

        public Builder listener(ElysiumPlayer listener) {
            this.listeners.add(listener);
            return this;
        }

        public Builder listeners(Collection<? extends ElysiumPlayer> listeners) {
            this.listeners.clear();
            this.listeners.addAll(listeners);
            return this;
        }

        public Builder pipelineComponent(ChatChannelPipelineComponent component) {
            this.pipeline.add(component);
            return this;
        }

        public Builder pipeline(Collection<? extends ChatChannelPipelineComponent> pipeline) {
            this.pipeline.clear();
            this.pipeline.addAll(pipeline);
            return this;
        }

        public Builder matchPattern(String matchPattern) {
            this.matchPattern = matchPattern;
            return this;
        }

        public Builder ircEnabled(boolean ircEnabled) {
            this.ircEnabled = ircEnabled;
            return this;
        }

        public Builder ircChannel(String ircChannel) {
            this.ircChannel = ircChannel;
            return this;
        }

        public Builder ircWhitelist(boolean ircWhitelist) {
            this.ircWhitelist = ircWhitelist;
            return this;
        }

        public Builder joinedByDefault(boolean joinedByDefault) {
            this.joinedByDefault = joinedByDefault;
            return this;
        }

        public BukkitChatChannel build() {
            if (radius > 0 && clearRadius > 0) {
                pipelineComponent(new BukkitGarbleChatChannelPipelineComponent(clearRadius));
            }
            pipelineComponent(new BukkitFormatChatChannelPipelineComponent(plugin, formatString));
            if (ircEnabled && radius <= 0 && !ircChannel.trim().equals("")) {
                pipelineComponent(new BukkitIRCChatChannelPipelineComponent(ircChannel, ircWhitelist));
            }
            pipelineComponent(new BukkitLogChatChannelPipelineComponent());
            return new BukkitChatChannel(
                    plugin,
                    id,
                    name,
                    color,
                    formatString,
                    radius,
                    clearRadius,
                    speakers,
                    listeners,
                    pipeline,
                    matchPattern,
                    ircEnabled,
                    ircChannel,
                    ircWhitelist,
                    joinedByDefault
            );
        }

    }

    private BukkitChatChannel(ElysiumChatBukkit plugin, int id, String name, ChatColor color, String formatString, int radius, int clearRadius, List<ElysiumPlayer> speakers, List<ElysiumPlayer> listeners, List<ChatChannelPipelineComponent> pipeline, String matchPattern, boolean ircEnabled, String ircChannel, boolean ircWhitelist, boolean joinedByDefault) {
        this.plugin = plugin;
        this.id = id;
        this.name = name;
        this.color = color;
        this.formatString = formatString;
        this.radius = radius;
        this.clearRadius = clearRadius;
        this.speakers = speakers;
        this.listeners = listeners;
        this.pipeline = pipeline;
        this.matchPattern = matchPattern;
        this.ircEnabled = ircEnabled;
        this.ircChannel = ircChannel;
        this.ircWhitelist = ircWhitelist;
        this.joinedByDefault = joinedByDefault;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Color getColor() {
        return ChatColorUtils.colorFromChatColor(color);
    }

    @Override
    public void setColor(Color color) {
        this.color = ChatColorUtils.closestChatColorToColor(color);
    }

    @Override
    public String getFormatString() {
        return formatString;
    }

    @Override
    public void setFormatString(String formatString) {
        this.formatString = formatString;
        getPipeline().stream()
                .filter(pipelineComponent -> pipelineComponent instanceof BukkitFormatChatChannelPipelineComponent)
                .findFirst()
                .ifPresent(pipelineComponent -> {
                    BukkitFormatChatChannelPipelineComponent formatComponent = (BukkitFormatChatChannelPipelineComponent) pipelineComponent;
                    formatComponent.setFormatString(formatString);
                });
    }

    @Override
    public int getRadius() {
        return radius;
    }

    @Override
    public void setRadius(int radius) {
        this.radius = radius;
        if (clearRadius > 0 && radius > 0) {
            getPipeline().stream()
                    .filter(pipelineComponent -> pipelineComponent instanceof BukkitGarbleChatChannelPipelineComponent)
                    .findFirst()
                    .ifPresent(pipelineComponent -> {
                        BukkitGarbleChatChannelPipelineComponent garbleComponent = (BukkitGarbleChatChannelPipelineComponent) pipelineComponent;
                        garbleComponent.setClearRange(clearRadius);
                    });
        } else {
            Iterator<ChatChannelPipelineComponent> pipelineIterator = getPipeline().iterator();
            while (pipelineIterator.hasNext()) {
                ChatChannelPipelineComponent pipelineComponent = pipelineIterator.next();
                if (pipelineComponent instanceof BukkitGarbleChatChannelPipelineComponent) {
                    pipelineIterator.remove();
                }
            }
        }
    }

    @Override
    public int getClearRadius() {
        return clearRadius;
    }

    @Override
    public void setClearRadius(int clearRadius) {
        this.clearRadius = clearRadius;
        if (clearRadius > 0 && radius > 0) {
            getPipeline().stream()
                    .filter(pipelineComponent -> pipelineComponent instanceof BukkitGarbleChatChannelPipelineComponent)
                    .findFirst()
                    .ifPresent(pipelineComponent -> {
                        BukkitGarbleChatChannelPipelineComponent garbleComponent = (BukkitGarbleChatChannelPipelineComponent) pipelineComponent;
                        garbleComponent.setClearRange(clearRadius);
                    });
        } else {
            Iterator<ChatChannelPipelineComponent> pipelineIterator = getPipeline().iterator();
            while (pipelineIterator.hasNext()) {
                ChatChannelPipelineComponent pipelineComponent = pipelineIterator.next();
                if (pipelineComponent instanceof BukkitGarbleChatChannelPipelineComponent) {
                    pipelineIterator.remove();
                }
            }
        }
    }

    @Override
    public Collection<? extends ElysiumPlayer> getSpeakers() {
        return speakers;
    }

    @Override
    public void addSpeaker(ElysiumPlayer speaker) {
        if (!speakers.contains(speaker))
            speakers.add(speaker);
    }

    @Override
    public void removeSpeaker(ElysiumPlayer speaker) {
        while (speakers.contains(speaker)) {
            speakers.remove(speaker);
        }
    }

    @Override
    public Collection<? extends ElysiumPlayer> getListeners() {
        return listeners;
    }

    @Override
    public void addListener(ElysiumPlayer listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    @Override
    public void removeListener(ElysiumPlayer listener) {
        while (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    @Override
    public List<ChatChannelPipelineComponent> getPipeline() {
        return pipeline;
    }

    @Override
    public String processMessage(String message, ChatMessageContext context) {
        for (ChatChannelPipelineComponent pipelineComponent : getPipeline()) {
            if (message == null) break;
            try {
                message = pipelineComponent.process(message, context);
            } catch (ChatChannelMessageFormattingFailureException exception) {
                ElysiumPlayer elysiumPlayer = context.getSender();
                if (elysiumPlayer instanceof BukkitPlayer) {
                    BukkitPlayer player = (BukkitPlayer) elysiumPlayer;
                    OfflinePlayer bukkitPlayer = player.getBukkitPlayer();
                    if (bukkitPlayer.isOnline()) {
                        bukkitPlayer.getPlayer().sendMessage(exception.getMessage());
                    }
                }
                break;
            }
        }
        return message;
    }

    @Override
    public String getMatchPattern() {
        return matchPattern;
    }

    @Override
    public void setMatchPattern(String matchPattern) {
        this.matchPattern = matchPattern;
    }

    @Override
    public boolean isIRCEnabled() {
        return ircEnabled;
    }

    @Override
    public void setIRCEnabled(boolean ircEnabled) {
        this.ircEnabled = ircEnabled;
        if (ircEnabled) {
            getPipeline().add(new BukkitIRCChatChannelPipelineComponent(getIRCChannel(), isIRCWhitelist()));
            getPipeline().sort(ChatChannelPipelineComponent::compareTo);
        } else {
            Iterator<ChatChannelPipelineComponent> pipelineIterator = getPipeline().iterator();
            while (pipelineIterator.hasNext()) {
                ChatChannelPipelineComponent pipelineComponent = pipelineIterator.next();
                if (pipelineComponent instanceof BukkitIRCChatChannelPipelineComponent) {
                    pipelineIterator.remove();
                }
            }
        }
    }

    @Override
    public String getIRCChannel() {
        return ircChannel;
    }

    @Override
    public void setIRCChannel(String ircChannel) {
        this.ircChannel = ircChannel;
        if (ircEnabled) {
            getPipeline().stream()
                    .filter(pipelineComponent -> pipelineComponent instanceof BukkitIRCChatChannelPipelineComponent)
                    .findFirst()
                    .ifPresent(pipelineComponent -> {
                BukkitIRCChatChannelPipelineComponent ircComponent = (BukkitIRCChatChannelPipelineComponent) pipelineComponent;
                ircComponent.setIRCChannel(ircChannel);
            });
        }
    }

    @Override
    public boolean isIRCWhitelist() {
        return ircWhitelist;
    }

    @Override
    public void setIRCWhitelist(boolean ircWhitelist) {
        this.ircWhitelist = ircWhitelist;
        if (ircEnabled) {
            getPipeline().stream()
                    .filter(pipelineComponent -> pipelineComponent instanceof BukkitIRCChatChannelPipelineComponent)
                    .findFirst()
                    .ifPresent(pipelineComponent -> {
                        BukkitIRCChatChannelPipelineComponent ircComponent = (BukkitIRCChatChannelPipelineComponent) pipelineComponent;
                        ircComponent.setWhitelist(ircWhitelist);
                    });
        }
    }

    @Override
    public boolean isJoinedByDefault() {
        return joinedByDefault;
    }

    @Override
    public void setJoinedByDefault(boolean joinedByDefault) {
        this.joinedByDefault = joinedByDefault;
    }

    @Override
    public void log(String message) throws IOException {
        File logDirectory = new File(plugin.getDataFolder(), "logs");
        DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        File datedLogDirectory = new File(logDirectory, logDateFormat.format(new Date()));
        if (!datedLogDirectory.exists()) {
            if (!datedLogDirectory.mkdirs())
                throw new IOException("Could not create log directory. Does the server have permission to write to the directory?");
        }
        File log = new File(datedLogDirectory, getName() + ".log");
        if (!log.exists()) {
            try {
                if (!log.createNewFile())
                    throw new IOException("Failed to create log file. Does the server have permission to write to the directory?");
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(log, true));
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            writer.append("[").append(dateFormat.format(new Date())).append("] ").append(message).append("\n");
            writer.close();
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
