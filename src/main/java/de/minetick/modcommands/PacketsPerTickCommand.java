package de.minetick.modcommands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import de.minetick.PlayerChunkManager;

public class PacketsPerTickCommand extends Command {

    public PacketsPerTickCommand(String name) {
        super(name);
        this.usageMessage = "/packetspertick <packetCount>";
        this.description = "Adjusts the amount of chunk packets that shall be created for each player every tick";
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if(sender instanceof ConsoleCommandSender || sender instanceof Player) {
            if(sender instanceof Player) {
                Player p = (Player) sender;
                if(!p.isOp()) {
                    p.sendMessage("You are not allowed to use this command!");
                    return true;
                }
            }
            if(args.length == 0) {
                sender.sendMessage("Currently " + PlayerChunkManager.packetsPerTick + " packets will be created per tick for each player");
            } else if(args.length == 1) {
                int count = -1;
                try {
                    count = Integer.parseInt(args[0]);
                } catch(NumberFormatException e) {
                    this.sendHelp(sender);
                }
                if(count >= 1 && count <= 20) {
                    PlayerChunkManager.packetsPerTick = count;
                    sender.sendMessage("Packet creation rate was set to: " + count + " packets per tick for each player");
                } else {
                    this.sendHelp(sender);
                }
            }
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("Usage: /packetspertick <rate>");
        sender.sendMessage("Allowed levels: 1(slow chunkloading, low cpu load) - 20(fast chunkloading, high cpu load)");
        sender.sendMessage("Example: /packetspertick 1  -  Sets the creation rate to 1 chunk packet per tick for each player");
        sender.sendMessage("What you set here is not written to the bukkit.yml settings file. You need to do that manually.");
    }
}
