package com.imdeity.lottery.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;
import com.imdeity.lottery.Lottery;
import com.imdeity.lottery.objects.LotteryObject;

public class LotteryAdminCommand implements CommandExecutor {

    private static final List<String> output = new ArrayList<String>();

    static {
        output.add(Deity.utils.chat.formatTitle("LotteryAdmin"));
        output.add(Deity.utils.chat.formatCommand("", "/lotteryadmin", "addtopot [n]", "Adds [n] to current pot."));
        output.add(Deity.utils.chat.formatCommand("", "/lotteryadmin", "removefrompot [n]", "Removes [n] to current pot."));
        output.add(Deity.utils.chat.formatCommand("", "/lotteryadmin", "draw", "Forces a draw."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Deity.perm.has(player, "lottery.admin")) {
                parseCommand(player, args);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public void parseCommand(Player player, String[] split) {

        if (split.length == 0) {
            for (String o : output)
                Deity.chat.sendPlayerMessage(player, o);
            // } else if (split[0].equalsIgnoreCase("config") ||
            // split[0].equalsIgnoreCase("c")) {
            // config(player, split);
        } else if (split[0].equalsIgnoreCase("addtopot") || split[0].equalsIgnoreCase("a")) {
            addToPot(player, split);
        } else if (split[0].equalsIgnoreCase("removefrompot") || split[0].equalsIgnoreCase("r")) {
            removeFromPot(player, split);
        } else if (split[0].equalsIgnoreCase("draw") || split[0].equalsIgnoreCase("d")) {
            forceDraw(player, split);
        } else {

        }
    }

    public void config(Player player, String[] split) {
        if (split.length == 1) {
            Deity.chat.sendPlayerError(player, "Invalid Syntax, please try again.");
        } else if (split[1].equalsIgnoreCase("")) {

        }
    }

    public void addToPot(Player player, String[] split) {
        if (split.length == 1) {
            Deity.chat.sendPlayerError(player, "Invalid Syntax, please try again.");
        } else {
            int num = 0;
            try {
                num = Integer.parseInt(split[1]);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            Lottery.config.set("lottery.pot", Lottery.config.getInt("lottery.pot") + num);
        }
    }

    public void removeFromPot(Player player, String[] split) {
        if (split.length == 1) {
            Deity.chat.sendPlayerError(player, "Invalid Syntax, please try again.");
        } else {
            int num = 0;
            try {
                num = Integer.parseInt(split[1]);
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
            }
            Lottery.config.set("lottery.pot", Lottery.config.getInt("lottery.pot") - num);
        }
    }

    public void forceDraw(Player player, String[] split) {
        LotteryObject.drawWinner();
    }
}
