package de.minetick.modcommands;

import net.minecraft.server.Packet51MapChunk;
import net.minecraft.server.Packet56MapChunkBulk;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

public class PacketCompressionCommand extends Command {

    public PacketCompressionCommand(String name) {
        super(name);
        this.usageMessage = "/packetcompression <compressionlevel>";
        this.description = "Sets the compression level for chunk packets (Packet51 and Packet56)";
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
                sender.sendMessage("Current compression level: " + Packet56MapChunkBulk.targetCompressionLevel);
            } else if(args.length == 1) {
                int level = -1;
                try {
                    level = Integer.parseInt(args[0]);
                } catch(NumberFormatException e) {}
                if(level >= 1 && level <= 9) {
                    Packet51MapChunk.changeCompressionLevel(level);
                    Packet56MapChunkBulk.changeCompressionLevel(level);
                    sender.sendMessage("Packet compression was set to level: " + level);
                } else {
                    this.sendHelp(sender);
                }
            }
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("Usage: /packetcompression <compressionlevel>");
        sender.sendMessage("Allowed levels: 1(low, fast) - 9(high, slow)");
        sender.sendMessage("Example: /packetcompression 6  -  Sets the compressionLevel to 6");
        sender.sendMessage("What you set here is not written to the bukkit.yml settings file. You need to do that manually.");
    }
}
