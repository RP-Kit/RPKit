package com.seventh_root.elysium.core.bukkit.util;

import org.bukkit.ChatColor;

import java.awt.Color;

public class ChatColorUtils {

    private static final Color BLACK = new Color(0, 0, 0);
    private static final Color DARK_BLUE = new Color(0, 0, 170);
    private static final Color DARK_GREEN = new Color(0, 170, 0);
    private static final Color DARK_AQUA = new Color(0, 170, 170);
    private static final Color DARK_RED = new Color(170, 0, 0);
    private static final Color DARK_PURPLE = new Color(170, 0, 170);
    private static final Color GOLD = new Color(255, 170, 0);
    private static final Color GRAY = new Color(170, 170, 170);
    private static final Color DARK_GRAY = new Color(85, 85, 85);
    private static final Color BLUE = new Color(85, 85, 255);
    private static final Color GREEN = new Color(85, 255, 85);
    private static final Color AQUA = new Color(85, 255, 255);
    private static final Color RED = new Color(255, 85, 85);
    private static final Color LIGHT_PURPLE = new Color(255, 85, 255);
    private static final Color YELLOW = new Color(255, 255, 85);
    private static final Color WHITE = new Color(255, 255, 255);

    private ChatColorUtils() {}

    public static ChatColor closestChatColorToColor(Color color) {
        double minDistSquared = Double.MAX_VALUE;
        ChatColor closest = null;
        for (ChatColor chatColor : ChatColor.values()) {
            Color chatColorColor = colorFromChatColor(chatColor);
            if (chatColorColor != null) {
                double distSquared = Math.pow(color.getRed() - chatColorColor.getRed(), 2) + Math.pow(color.getBlue() - chatColorColor.getBlue(), 2) + Math.pow(color.getGreen() - chatColorColor.getGreen(), 2);
                if (distSquared < minDistSquared) {
                    minDistSquared = distSquared;
                    closest = chatColor;
                }
            }
        }
        return closest;
    }

    public static Color colorFromChatColor(ChatColor chatColor) {
        switch (chatColor) {
            case BLACK:
                return BLACK;
            case DARK_BLUE:
                return DARK_BLUE;
            case DARK_GREEN:
                return DARK_GREEN;
            case DARK_AQUA:
                return DARK_AQUA;
            case DARK_RED:
                return DARK_RED;
            case DARK_PURPLE:
                return DARK_PURPLE;
            case GOLD:
                return GOLD;
            case GRAY:
                return GRAY;
            case DARK_GRAY:
                return DARK_GRAY;
            case BLUE:
                return BLUE;
            case GREEN:
                return GREEN;
            case AQUA:
                return AQUA;
            case RED:
                return RED;
            case LIGHT_PURPLE:
                return LIGHT_PURPLE;
            case YELLOW:
                return YELLOW;
            case WHITE:
                return WHITE;
            case MAGIC:
            case BOLD:
            case STRIKETHROUGH:
            case UNDERLINE:
            case ITALIC:
            case RESET:
            default:
                return null;
        }
    }

}
