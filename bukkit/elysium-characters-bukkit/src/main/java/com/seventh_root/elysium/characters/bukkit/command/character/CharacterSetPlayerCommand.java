package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class CharacterSetPlayerCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;

    public CharacterSetPlayerCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new PlayerPrompt(plugin))
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
            if (sender.hasPermission("elysium.characters.command.character.set.player")) {
                Player bukkitPlayer = (Player) sender;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    if (args.length > 0) {
                        @SuppressWarnings("deprecation") OfflinePlayer newBukkitPlayer = plugin.getServer().getPlayer(args[0]);
                        if (newBukkitPlayer != null) {
                            BukkitPlayer newPlayer = playerProvider.getPlayer(newBukkitPlayer);
                            character.setPlayer(newPlayer);
                            characterProvider.updateCharacter(character);
                            characterProvider.setActiveCharacter(player, null);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-player-valid")));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-player-invalid-player")));
                        }
                    } else {
                        conversationFactory.buildConversation(bukkitPlayer).begin();
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-character")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-set-player")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class PlayerPrompt extends PlayerNamePrompt {

        public PlayerPrompt(Plugin plugin) {
            super(plugin);
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Player input) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                Player bukkitPlayer = (Player) conversable;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    character.setPlayer(playerProvider.getPlayer(input));
                    characterProvider.updateCharacter(character);
                    characterProvider.setActiveCharacter(player, null);
                }
            }
            return new PlayerSetPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-player-invalid-player"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-player-prompt"));
        }

    }

    private class PlayerSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-player-valid"));
        }

    }
}
