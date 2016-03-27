package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.api.character.Gender;
import com.seventh_root.elysium.api.character.Race;
import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRace;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider;
import com.seventh_root.elysium.players.bukkit.BukkitPlayer;
import com.seventh_root.elysium.players.bukkit.BukkitPlayerProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class CharacterNewCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;

    public CharacterNewCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new NamePrompt())
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
            if (sender.hasPermission("elysium.characters.command.character.new")) {
                conversationFactory.buildConversation((Player) sender).begin();
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-new"))));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class NamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-name-prompt"));
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            context.setSessionData("name", input);
            return new NameSetPrompt();
        }

    }

    private class NameSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new GenderPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-name-valid"));
        }

    }

    private class GenderPrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            return plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class).getGender(input) != null;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
            context.setSessionData("gender", genderProvider.getGender(input));
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
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-gender-prompt") + "\n" + genderListBuilder.toString());
        }

    }

    private class GenderSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new AgePrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-gender-valid"));
        }

    }

    private class AgePrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-age-prompt"));
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.intValue() >= plugin.getConfig().getInt("characters.min-age") && input.intValue() <= plugin.getConfig().getInt("characters.max-age");
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-age-invalid-validation"));
        }

        @Override
        protected String getInputNotNumericText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-age-invalid-number"));
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            context.setSessionData("age", input.intValue());
            return new AgeSetPrompt();
        }

    }

    private class AgeSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new RacePrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-age-valid"));
        }

    }

    private class RacePrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            return plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class).getRace(input) != null;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
            context.setSessionData("race", raceProvider.getRace(input));
            return new RaceSetPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-race-invalid-race"));
        }

        @Override
        public String getPromptText(ConversationContext context) {
            BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
            StringBuilder raceListBuilder = new StringBuilder();
            for (BukkitRace race : raceProvider.getRaces()) {
                raceListBuilder.append(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-list-item")
                        .replace("$race", race.getName())))
                        .append("\n");
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-race-prompt")) + "\n" + raceListBuilder.toString();
        }

    }

    private class RaceSetPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new DescriptionPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-race-valid"));
        }

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
            return new CharacterCreatedPrompt();
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-description-valid"));
        }

    }

    private class CharacterCreatedPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                Player bukkitPlayer = (Player) conversable;
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter newCharacter = new BukkitCharacter.Builder(plugin)
                        .player(player)
                        .name((String) context.getSessionData("name"))
                        .gender((Gender) context.getSessionData("gender"))
                        .age((int) context.getSessionData("age"))
                        .race((Race) context.getSessionData("race"))
                        .description((String) context.getSessionData("description"))
                        .build();
                characterProvider.addCharacter(newCharacter);
                characterProvider.setActiveCharacter(player, newCharacter);
            }
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-new-valid"));
        }

    }

}
