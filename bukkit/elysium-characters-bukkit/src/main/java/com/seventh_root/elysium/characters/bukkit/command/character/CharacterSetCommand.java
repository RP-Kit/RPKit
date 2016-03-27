package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CharacterSetCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;

    private final CharacterSetPlayerCommand characterSetPlayerCommand;
    private final CharacterSetNameCommand characterSetNameCommand;
    private final CharacterSetGenderCommand characterSetGenderCommand;
    private final CharacterSetAgeCommand characterSetAgeCommand;
    private final CharacterSetRaceCommand characterSetRaceCommand;
    private final CharacterSetDescriptionCommand characterSetDescriptionCommand;
    private final CharacterSetDeadCommand characterSetDeadCommand;

    public CharacterSetCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        characterSetPlayerCommand = new CharacterSetPlayerCommand(plugin);
        characterSetNameCommand = new CharacterSetNameCommand(plugin);
        characterSetGenderCommand = new CharacterSetGenderCommand(plugin);
        characterSetAgeCommand = new CharacterSetAgeCommand(plugin);
        characterSetRaceCommand = new CharacterSetRaceCommand(plugin);
        characterSetDescriptionCommand = new CharacterSetDescriptionCommand(plugin);
        characterSetDeadCommand = new CharacterSetDeadCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            if (args[0].equalsIgnoreCase("player")) {
                return characterSetPlayerCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("name")) {
                return characterSetNameCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("gender")) {
                return characterSetGenderCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("age")) {
                return characterSetAgeCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("race")) {
                return characterSetRaceCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("description") || args[0].equalsIgnoreCase("desc")) {
                return characterSetDescriptionCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("dead")) {
                return characterSetDeadCommand.onCommand(sender, command, label, newArgs);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-usage")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-set-usage")));
        }
        return true;
    }
}
