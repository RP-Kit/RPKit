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

public class CharacterDeleteCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;
    private final ConversationFactory confirmationConversationFactory;

    public CharacterDeleteCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new CharacterPrompt())
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
        confirmationConversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new ConfirmationPrompt())
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
            if (sender.hasPermission("elysium.characters.command.character.delete")) {
                if (args.length > 0) {
                    StringBuilder characterNameBuilder = new StringBuilder();
                    for (int i = 0; i < args.length - 1; i++) {
                        characterNameBuilder.append(args[i]).append(" ");
                    }
                    characterNameBuilder.append(args[args.length - 1]);
                    Player bukkitPlayer = (Player) sender;
                    BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                    BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                    BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                    boolean charFound = false;
                    // Prioritise exact matches...
                    for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                        if (character.getName().equalsIgnoreCase(characterNameBuilder.toString())) {
                            Conversation conversation = confirmationConversationFactory.buildConversation(bukkitPlayer);
                            conversation.getContext().setSessionData("character_id", character.getId());
                            conversation.begin();
                            charFound = true;
                            break;
                        }
                    }
                    // And fall back to partial matches
                    if (!charFound) {
                        for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                            if (character.getName().toLowerCase().contains(characterNameBuilder.toString().toLowerCase())) {
                                Conversation conversation = confirmationConversationFactory.buildConversation(bukkitPlayer);
                                conversation.getContext().setSessionData("character_id", character.getId());
                                conversation.begin();
                                charFound = true;
                                break;
                            }
                        }
                    }
                    if (charFound) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-delete-valid")));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-delete-invalid-character")));
                    }
                } else {
                    conversationFactory.buildConversation((Player) sender).begin();
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-delete")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class CharacterPrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                Player bukkitPlayer = (Player) conversable;
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                    if (character.getName().equalsIgnoreCase(input)) {
                        return true;
                    }
                }
                for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                    if (character.getName().toLowerCase().contains(input.toLowerCase())) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                Player bukkitPlayer = (Player) conversable;
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                boolean charFound = false;
                // Prioritise exact matches...
                for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                    if (character.getName().equalsIgnoreCase(input)) {
                        context.setSessionData("character_id", character.getId());
                        charFound = true;
                        break;
                    }
                }
                // And fall back to partial matches
                if (!charFound) {
                    for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                        if (character.getName().toLowerCase().contains(input.toLowerCase())) {
                            context.setSessionData("character_id", character.getId());
                            break;
                        }
                    }
                }
            }
            return new CharacterDeletedPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-delete-invalid-character"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
            BukkitPlayer player = playerProvider.getPlayer((Player) context.getForWhom());
            BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
            StringBuilder characterListBuilder = new StringBuilder();
            for (BukkitCharacter character : characterProvider.getCharacters(player)) {
                characterListBuilder
                        .append("\n")
                        .append(
                                ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-list-item")
                                        .replace("$character", character.getName())
                                ));
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-delete-prompt")) + characterListBuilder.toString();
        }

    }

    private class ConfirmationPrompt extends BooleanPrompt {

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, boolean input) {
            if (input) {
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitCharacter character = characterProvider.getCharacter((int) context.getSessionData("character_id"));
                characterProvider.removeCharacter(character);
                return new CharacterDeletedPrompt();
            } else {
                return END_OF_CONVERSATION;
            }
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-delete-confirmation-invalid-boolean"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-delete-confirmation"));
        }

    }

    private class CharacterDeletedPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-delete-valid"));
        }
    }

}
