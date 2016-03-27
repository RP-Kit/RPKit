package com.seventh_root.elysium.characters.bukkit.command.gender;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGender;
import com.seventh_root.elysium.characters.bukkit.gender.BukkitGenderProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

public class GenderAddCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final ConversationFactory conversationFactory;

    public GenderAddCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        conversationFactory = new ConversationFactory(plugin)
                .withModality(true)
                .withFirstPrompt(new GenderPrompt())
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
            if (sender.hasPermission("elysium.characters.command.gender.add")) {
                if (args.length > 0) {
                    BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
                    StringBuilder genderBuilder = new StringBuilder();
                    for (int i = 0; i < args.length - 1; i++) {
                        genderBuilder.append(args[i]).append(' ');
                    }
                    genderBuilder.append(args[args.length - 1]);
                    if (genderProvider.getGender(genderBuilder.toString()) == null) {
                        genderProvider.addGender(new BukkitGender(genderBuilder.toString()));
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-add-valid")));
                    } else {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-add-invalid-gender")));
                    }
                } else {
                    conversationFactory.buildConversation((Conversable) sender).begin();
                }
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.no-permission-gender-add")));
            }
        }
        return true;
    }

    private class GenderPrompt extends ValidatingPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-add-prompt"));
        }

        @Override
        protected boolean isInputValid(ConversationContext context, String input) {
            BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
            return genderProvider.getGender(input) == null;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, String invalidInput) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-add-invalid-gender"));
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, String input) {
            BukkitGenderProvider genderProvider = plugin.getCore().getServiceManager().getServiceProvider(BukkitGenderProvider.class);
            genderProvider.addGender(new BukkitGender(input));
            return new GenderAddedPrompt();
        }

    }

    private class GenderAddedPrompt extends MessagePrompt {

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return END_OF_CONVERSATION;
        }

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.gender-add-valid"));
        }

    }

}
