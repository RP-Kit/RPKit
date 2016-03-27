package com.seventh_root.elysium.api.chat;

import com.seventh_root.elysium.api.player.ElysiumPlayer;
import com.seventh_root.elysium.core.database.TableRow;

import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

public interface ElysiumChatChannel extends TableRow {

    String getName();

    void setName(String name);

    Color getColor();

    void setColor(Color color);

    String getFormatString();

    void setFormatString(String formatString);

    int getRadius();

    void setRadius(int radius);

    int getClearRadius();

    void setClearRadius(int clearRadius);

    Collection<? extends ElysiumPlayer> getSpeakers();

    void addSpeaker(ElysiumPlayer speaker);

    void removeSpeaker(ElysiumPlayer speaker);

    Collection<? extends ElysiumPlayer> getListeners();

    void addListener(ElysiumPlayer listener);

    void removeListener(ElysiumPlayer listener);

    List<? extends ChatChannelPipelineComponent> getPipeline();

    String processMessage(String message, ChatMessageContext context);

    String getMatchPattern();

    void setMatchPattern(String matchPattern);

    boolean isIRCEnabled();

    void setIRCEnabled(boolean ircEnabled);

    String getIRCChannel();

    void setIRCChannel(String ircChannel);

    boolean isIRCWhitelist();

    void setIRCWhitelist(boolean ircWhitelist);

    void log(String message) throws IOException;

    boolean isJoinedByDefault();

    void setJoinedByDefault(boolean joinedByDefault);

}
