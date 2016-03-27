package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacter;
import com.seventh_root.elysium.characters.bukkit.character.BukkitCharacterProvider;
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

public class CharacterSetRaceCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;

    public CharacterSetRaceCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new RacePrompt())
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
            if (sender.hasPermission("elysium.characters.command.character.set.race")) {
                Player bukkitPlayer = (Player) sender;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    if (args.length > 0) {
                        StringBuilder raceBuilder = new StringBuilder();
                        for (int i = 0; i < args.length - 1; i++) {
                            raceBuilder.append(args[i]).append(" ");
                        }
                        raceBuilder.append(args[args.length - 1]);
                        BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
                        BukkitRace race = raceProvider.getRace(raceBuilder.toString());
                        if (race != null) {
                            character.setRace(race);
                            characterProvider.updateCharacter(character);
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-race-valid")));
                        } else {
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-race-invalid-race")));
                        }
                    } else {
                        conversationFactory.buildConversation(bukkitPlayer).begin();
                    }
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-character")));
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-character-set-race")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.not-from-console")));
        }
        return true;
    }

    private class RacePrompt extends ValidatingPrompt {

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            return plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class).getRace(input) != null;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            Conversable conversable = context.getForWhom();
            if (conversable instanceof Player) {
                Player bukkitPlayer = (Player) conversable;
                BukkitPlayerProvider playerProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitPlayerProvider.class);
                BukkitCharacterProvider characterProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitCharacterProvider.class);
                BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
                BukkitPlayer player = playerProvider.getPlayer(bukkitPlayer);
                BukkitCharacter character = characterProvider.getActiveCharacter(player);
                if (character != null) {
                    character.setRace(raceProvider.getRace(input));
                    characterProvider.updateCharacter(character);
                }
            }
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
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-race-valid"));
        }

    }

}
