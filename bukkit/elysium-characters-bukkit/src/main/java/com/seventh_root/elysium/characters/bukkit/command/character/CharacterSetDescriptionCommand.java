package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class CharacterSetDescriptionCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;

    public CharacterSetDescriptionCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new DescriptionPrompt())
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
            if (sender.hasPermission("elysium.characters.command.character.set.description")) {
                Player bukkitPlayer = (Player) sender;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    if (args.length > 0) {
                        StringBuilder descriptionBuilder = new StringBuilder();
                        for (int i = 0; i < args.length - 1; i++) {
                            descriptionBuilder.append(args[i]).append(" ");
                        }
                        descriptionBuilder.append(args[args.length - 1]);
                        character.setDescription(descriptionBuilder.toString());
                        characterProvider.updateCharacter(character);
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-description-valid")));
                    } else {
                        conversationFactory.buildConversation(bukkitPlayer).begin();
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-character")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-set-description")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class DescriptionPrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-description-prompt"));
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            if (input.equalsIgnoreCase("end")) {
                if (context.getSessionData("description") == null) {
                    context.setSessionData("description", "");
                }
                Conversable conversable = context.getForWhom();
                if (conversable instanceof Player) {
                    Player bukkitPlayer = (Player) conversable;
                    BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                    BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                    BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                    BukkitCharacter character = characterProvider.getActiveCharacter(player);
                    if (character != null) {
                        character.setDescription((String) context.getSessionData("description"));
                        characterProvider.updateCharacter(character);
                    }
                }
                return new DescriptionSetPrompt();
            } else {
                String previousDescription = (String) context.getSessionData("description");
                context.setSessionData("description", (previousDescription == null ? "" : previousDescription + " ") + input);
                return new DescriptionPrompt();
            }
        }

    }

    private class DescriptionSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-description-valid"));
        }

    }
}
