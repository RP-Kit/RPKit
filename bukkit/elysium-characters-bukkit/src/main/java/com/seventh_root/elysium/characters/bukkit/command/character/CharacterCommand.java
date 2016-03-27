package com.seventh_root.elysium.characters.bukkit.command.character;

import com.seventh_root.elysium.characters.bukkit.ElysiumCharactersBukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CharacterCommand implements CommandExecutor {

    private final ElysiumCharactersBukkit plugin;
    private final CharacterSetCommand characterSetCommand;
    private final CharacterCardCommand characterCardCommand;
    private final CharacterSwitchCommand characterSwitchCommand;
    private final CharacterListCommand characterListCommand;
    private final CharacterNewCommand characterNewCommand;
    private final CharacterDeleteCommand characterDeleteCommand;

    public CharacterCommand(ElysiumCharactersBukkit plugin) {
        this.plugin = plugin;
        characterSetCommand = new CharacterSetCommand(plugin);
        characterCardCommand = new CharacterCardCommand(plugin);
        characterSwitchCommand = new CharacterSwitchCommand(plugin);
        characterListCommand = new CharacterListCommand(plugin);
        characterNewCommand = new CharacterNewCommand(plugin);
        characterDeleteCommand = new CharacterDeleteCommand(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0) {
            String[] newArgs = new String[args.length - 1];
            System.arraycopy(args, 1, newArgs, 0, args.length - 1);
            if (args[0].equalsIgnoreCase("set")) {
                return characterSetCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("card") || args[0].equalsIgnoreCase("show") || args[0].equalsIgnoreCase("view")) {
                return characterCardCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("switch")) {
                return characterSwitchCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("list")) {
                return characterListCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("new") || args[0].equalsIgnoreCase("create")) {
                return characterNewCommand.onCommand(sender, command, label, newArgs);
            } else if (args[0].equalsIgnoreCase("delete")) {
                return characterDeleteCommand.onCommand(sender, command, label, newArgs);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-usage")));
            }
        } else {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.character-usage")));
        }
        return true;
    }

}
