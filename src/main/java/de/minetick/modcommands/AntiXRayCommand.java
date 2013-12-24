package de.minetick.modcommands;

import java.util.List;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.WorldServer;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class AntiXRayCommand extends Command {

    private String begin = ChatColor.GOLD + "[MinetickMod] " + ChatColor.WHITE;
    
    public AntiXRayCommand(String name) {
        super(name);
        this.usageMessage = "/antixray add|remove worldName";
        this.description = "Enables or disables chunk orebfuscation";
    }
    
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if(sender instanceof ConsoleCommandSender || sender instanceof Player) {
            if(sender instanceof Player) {
                Player p = (Player) sender;
                if(!p.isOp()) {
                    p.sendMessage("You are not allowed to use this command!");
                    return true;
                }
            }
            if(args.length == 2) {
                String world = args[1];
                if(!world.isEmpty()) {
                    List<WorldServer> worlds = MinecraftServer.getServer().worlds;
                    WorldServer ws = null;
                    for(int i = 0; i < worlds.size(); i++) {
                        WorldServer w = worlds.get(i);
                        if(world.equalsIgnoreCase(w.getWorld().getName())) {
                            ws = w;
                            break;
                        }
                    }
                    if(ws != null) {
                        if(args[0].equalsIgnoreCase("add")) {
                            if(!ws.antiXRay.isEnabled()) {
                                ws.antiXRay.enable();
                                sender.sendMessage(begin + "AntiXRay is now enabled in world " + ws.getWorld().getName());
                            } else {
                                sender.sendMessage(begin + "AntiXRay was already enabled in world " + ws.getWorld().getName());
                            }
                        } else if(args[0].equalsIgnoreCase("remove")) {
                            if(ws.antiXRay.isEnabled()) {
                                ws.antiXRay.disable();
                                sender.sendMessage(begin + "AntiXRay is now disabled in world " + ws.getWorld().getName());
                            } else {
                                sender.sendMessage(begin + "AntiXRay was already disabled in world " + ws.getWorld().getName());
                            }
                        } else {
                            sender.sendMessage(begin + this.usageMessage);
                        }
                    } else {
                        sender.sendMessage(begin + "World \"" + world + "\" not found.");
                    }
                }
            } else {
                sender.sendMessage(begin + this.usageMessage);
            }
            return true;
        }
        return false;
    }
}
