package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class CharacterSetGenderCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;

    public CharacterSetGenderCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new GenderPrompt())
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
            if (sender.hasPermission("elysium.characters.command.character.set.gender")) {
                Player bukkitPlayer = (Player) sender;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    if (args.length > 0) {
                        BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
                        BukkitGender gender = genderProvider.getGender(args[0]);
                        if (gender != null) {
                            character.setGender(gender);
                            characterProvider.updateCharacter(character);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-gender-valid")));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-gender-invalid-gender")));
                        }
                    } else {
                        conversationFactory.buildConversation(bukkitPlayer).begin();
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-character")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages-no-permission-character-set-gender")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class GenderPrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            return plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class).getGender(input) != null;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                Player bukkitPlayer = (Player) conversable;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    character.setGender(genderProvider.getGender(input));
                    characterProvider.updateCharacter(character);
                }
            }
            return new GenderSetPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-gender-invalid-gender"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
            StringBuilder genderListBuilder = new StringBuilder();
            for (BukkitGender gender : genderProvider.getGenders()) {
                genderListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-list-item")
                        .replace("$gender", gender.getName())))
                        .append("\n");
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-gender-prompt")) + "\n" + genderListBuilder.toString();
        }

    }

    private class GenderSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-gender-valid"));
        }

    }

}
