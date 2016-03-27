package com.seventh_root.elysium.characters.bukkit.command.race;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRace;
import com.seventh_root.elysium.characters.bukkit.race.BukkitRaceProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class RaceAddCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;

    public RaceAddCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new RacePrompt())
                .withEscapeSequence("cancel")
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
        if (sender instanceof Conversable) {
            if (sender.hasPermission("elysium.characters.command.race.add")) {
                if (args.length > 0) {
                    BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
                    StringBuilder raceBuilder = new StringBuilder();
                    for (int i = 0; i < args.length - 1; i++) {
                        raceBuilder.append(args[i]).append(' ');
                    }
                    raceBuilder.append(args[args.length - 1]);
                    if (raceProvider.getRace(raceBuilder.toString()) == null) {
                        raceProvider.addRace(new BukkitRace(raceBuilder.toString()));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-add-valid")));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-add-")));
                    }
                } else {
                    conversationFactory.buildConversation((Conversable) sender).begin();
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-race-add")));
            }
        }
        return true;
    }

    private class RacePrompt extends ValidatingPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-add-prompt"));
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
            return raceProvider.getRace(input) == null;
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            BukkitRaceProvider raceProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitRaceProvider.class);
            raceProvider.addRace(new BukkitRace(input));
            return new RaceAddedPrompt();
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-add-invalid-race"));
        }

    }

    private class RaceAddedPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.race-add-valid"));
        }

    }

}
