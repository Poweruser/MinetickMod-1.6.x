package de.minetick.modcommands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import de.minetick.MinetickMod;

public class TPSCommand extends Command {

    private int[] steps = new int[] {18, 16, 14, 10, 0};
    private ChatColor[] colors = new ChatColor[] {ChatColor.GREEN, ChatColor.YELLOW, ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED };
    private StringBuilder[] builders = new StringBuilder[5];

    public TPSCommand(String name) {
        super(name);
        this.usageMessage = "/tps";
        this.description = "Displays the servers tick rate of the last 30 seconds";
        for(int i = 0; i < this.builders.length; i++) {
            this.builders[i] = new StringBuilder();
        }
    }

    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if(!(sender instanceof Player)) { return true; }
        Integer[] array = MinetickMod.getTicksPerSecond();
        if(array.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "TPS statistic: " + ChatColor.RESET + "No data available yet. Try again later");
            return true;
        }
        for(int i = 0; i < this.builders.length; i++) {
            this.builders[i].delete(0, this.builders[i].length());
        }

        int start = array.length - 30;
        for(int i = start; i < array.length; i++) {
            Integer k;
            if(i < 0) {
                k = 0;
            } else {
                k = array[i];
            }
            for(int j = 0; j < this.steps.length; j++) {
                if(k > this.steps[j]) {
                    this.builders[j].append(this.colors[j]);
                    this.builders[j].append('\u2B1B');
                    this.builders[j].append(ChatColor.RESET);
                } else {
                    this.builders[j].append(ChatColor.BLACK);
                    this.builders[j].append('\u2B1C');
                    this.builders[j].append(ChatColor.RESET);
                }
            }
        }
        ChatColor current = ChatColor.RED;
        Integer last = array[array.length - 1];
        for(int i = 0; i < this.steps.length; i++) {
            if(this.steps[i] < last) {
                current = this.colors[i];
                break;
            }
        }
        sender.sendMessage(ChatColor.GOLD + "Current TPS: " + current + last + ChatColor.GOLD + "  TPS statistic (last 30 seconds):");
        for(int i = 0; i < this.builders.length; i++) {
            this.builders[i].append("   >" + this.steps[i]);
            sender.sendMessage(this.builders[i].toString());
        }
        return true;
    }
}
