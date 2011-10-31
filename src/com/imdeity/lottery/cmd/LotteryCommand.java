package com.imdeity.lottery.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;
import com.imdeity.deityapi.exception.InvalidChannelException;
import com.imdeity.deityapi.exception.InvalidFundsException;
import com.imdeity.deityapi.exception.NegativeMoneyException;
import com.imdeity.lottery.objects.LotteryObject;
import com.imdeity.lottery.util.*;

public class LotteryCommand implements CommandExecutor {

    private static final List<String> output = new ArrayList<String>();

    static {
        output.add(ChatTools.formatTitle("Lottery"));
        output.add(ChatTools.formatCommand("", "/lottery", "",
                "Checks current lottery info."));
        output.add(ChatTools.formatCommand("", "/lottery", "buy [n]",
                "Buys one or [n] of lottery tickets."));
        output.add(ChatTools.formatCommand("", "/lottery", "winners",
                "Gets past lottery winners."));
        output.add(ChatTools.formatCommand("", "/lottery", "claim",
                "Claim your recent winnings."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
            String commandLabel, String args[]) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Deity.perm.has(player, "lottery.general")) {
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
            List<String> out = new ArrayList<String>();
            out.add("<option><gray>There is <yellow>" + LotteryObject.getPot()
                    + ".00 " + "<gray>Dei in the current pot.");
            out.add("<option><gray>You have <yellow>"
                    + LotteryObject.getNumTickets(player.getName())
                    + "<gray> Tickets.");
            out.add("<option><yellow>Use /lottery ? for a list of commands.");
            for (String o : out)
                ChatTools.formatAndSend(o, "Lottery", player);
        } else if (split[0].equalsIgnoreCase("help")
                || split[0].equalsIgnoreCase("?")) {
            for (String o : output)
                Deity.chat.sendPlayerMessage(player, o);
        } else if (split[0].equalsIgnoreCase("buy")
                || split[0].equalsIgnoreCase("b")) {
            try {
                buyCommand(player, split);
            } catch(InvalidFundsException ex) {
                Deity.chat.sendPlayerError(player, ex.getError());
            } catch (NegativeMoneyException ex) {
                Deity.chat.sendPlayerError(player, ex.getError());
            } catch (InvalidChannelException ex) {
                ex.printStackTrace();
            }
        } else if (split[0].equalsIgnoreCase("winners")
                || split[0].equalsIgnoreCase("w")) {
            winnersCommand(player, split);
        } else if (split[0].equalsIgnoreCase("claim")
                || split[0].equalsIgnoreCase("c")){
            LotteryObject.claimPrize(player);
        }
    }

    public void buyCommand(Player player, String[] split) throws InvalidFundsException, NegativeMoneyException, InvalidChannelException {
        int numTicket = 1;
        String sql = "";
        if (split.length == 2)
            numTicket = Integer.parseInt(split[1]);

        sql = "INSERT INTO  " + Settings.getMySQLPlayersTable() + " ("
                + "`username` )" + "VALUES (" + "'" + player.getName() + "');";
        double money = numTicket * Settings.getTicketPrice();

        if (LotteryObject.isWinner(player.getName())) {
            Deity.chat.sendPlayerError(player, "You have won a lottery in the past 10 days.");
            Deity.chat.sendPlayerError(player, "Don't be so greedy.");
            return;
        }

        if (numTicket < 1) {
            Deity.chat.sendPlayerError(player, "Negative Tickets? Yea...that makes sense.");
            return;
        }

        if ((LotteryObject.getNumTickets(player.getName()) + numTicket) > Settings
                .getMaxTickets()) {
            Deity.chat.sendPlayerError(player,
                    "You can only have a maximum of "
                            + Settings.getMaxTickets() + " Tickets.");
            return;
        }

        if (Deity.econ.canPay(player.getName(), money)) {
            for (int i = 0; i < numTicket; i++) {
                Deity.data.getDB().Write(sql);
            }
            if (numTicket == 1)
                Deity.chat.broadcastHerochatMessage(Settings.getChannelName(), "<gold>[Lottery] " + player.getName() +" just bought " + numTicket + " Ticket! <teal>/lottery");
            else
                Deity.chat.broadcastHerochatMessage(Settings.getChannelName(), "<gold>[Lottery] " + player.getName() +" just bought " + numTicket + " Tickets! <teal>/lottery");
        } else {
            throw new InvalidFundsException();
        }
    }

    public void winnersCommand(Player player, String[] split) {
        ArrayList<String> winners = LotteryObject.getWinners();
        player.sendMessage(ChatTools.formatTitle("Lottery Winners"));
        for (String s : winners) {
           Deity.chat.sendPlayerMessage(player, s);
        }

    }
}