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
import com.imdeity.lottery.Lottery;
import com.imdeity.lottery.objects.LotteryObject;

public class LotteryCommand implements CommandExecutor {

    private static final List<String> output = new ArrayList<String>();

    static {
        output.add(Deity.utils.chat.formatTitle("Lottery"));
        output.add(Deity.utils.chat.formatCommand("", "/lottery", "", "Checks current lottery info."));
        output.add(Deity.utils.chat.formatCommand("", "/lottery", "buy [n]", "Buys one or [n] of lottery tickets."));
        output.add(Deity.utils.chat.formatCommand("", "/lottery", "winners", "Gets past lottery winners."));
        output.add(Deity.utils.chat.formatCommand("", "/lottery", "claim", "Claim your recent winnings."));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
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
            Deity.chat.sendPlayerMessage(player, "DeityLottery", "&8There is &e" + LotteryObject.getPot() + ".00" + " &8Dei in the current pot.");
            Deity.chat.sendPlayerMessage(player, "DeityLottery", "&8You have &e" + LotteryObject.getNumTickets(player.getName()) + " &8Tickets.");
            Deity.chat.sendPlayerMessage(player, "DeityLottery", "&8Tickets are &e" + Lottery.config.getInt("lottery.ticket_price") + " &8each.");
            Deity.chat.sendPlayerMessage(player, "DeityLottery", "&eUse /lottery ? for a list of commands.");

        } else if (split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("?")) {
            for (String o : output)
                Deity.chat.sendPlayerMessage(player, o);
        } else if (split[0].equalsIgnoreCase("buy") || split[0].equalsIgnoreCase("b")) {
            try {
                buyCommand(player, split);
            } catch (InvalidFundsException ex) {
                Deity.chat.sendPlayerError(player, ex.getError());
            } catch (NegativeMoneyException ex) {
                Deity.chat.sendPlayerError(player, ex.getError());
            } catch (InvalidChannelException ex) {
                ex.printStackTrace();
            }
        } else if (split[0].equalsIgnoreCase("winners") || split[0].equalsIgnoreCase("w")) {
            winnersCommand(player, split);
        } else if (split[0].equalsIgnoreCase("claim") || split[0].equalsIgnoreCase("c")) {
            LotteryObject.claimPrize(player);
        }
    }

    public void buyCommand(Player player, String[] split) throws InvalidFundsException, NegativeMoneyException, InvalidChannelException {
        int numTicket = 1;
        String sql = "";
        if (split.length == 2) numTicket = Integer.parseInt(split[1]);

        sql = "INSERT INTO  " + Deity.data.getDB().tableName("lottery_", "players") + " (`username` )" + "VALUES (" + "'" + player.getName() + "');";
        double money = numTicket * Lottery.config.getInt("lottery.ticket_price");

        if (LotteryObject.isWinner(player.getName())) {
            Deity.chat.sendPlayerError(player, "You have won a lottery in the past 10 days.");
            Deity.chat.sendPlayerError(player, "Don't be so greedy.");
            return;
        }

        if (numTicket < 1) {
            Deity.chat.sendPlayerError(player, "Negative Tickets? Yea...that makes sense.");
            return;
        }

        if ((LotteryObject.getNumTickets(player.getName()) + numTicket) > Lottery.config.getInt("lottery.max_tickets")) {
            Deity.chat.sendPlayerError(player, "You can only have a maximum of " + Lottery.config.getInt("lottery.max_tickets") + " Tickets.");
            return;
        }

        if (Deity.econ.canPay(player.getName(), money)) {
            for (int i = 0; i < numTicket; i++) {
                Deity.data.getDB().Write(sql);
            }
            Deity.econ.send(player.getName(), money, "Lottery Tickets");
            if (numTicket == 1) Deity.chat.sendMessageToAllOnline("DeityLottery", "&e" + player.getName() + " &fjust bought &e1 ticket! &3/lottery buy&f to get one yourself");
            else Deity.chat.sendMessageToAllOnline("DeityLottery", "&e" + player.getName() + " &fjust bought &e" + numTicket + " tickets! &3/lottery buy&f to get one yourself");
        } else {
            throw new InvalidFundsException();
        }
    }

    public void winnersCommand(Player player, String[] split) {
        ArrayList<String> winners = LotteryObject.getWinners();
        Deity.chat.sendPlayerMessageNoTitle(player, "&7----====&8[&5Lottery Winners&8]&7====----");
        for (String s : winners) {
            Deity.chat.sendPlayerMessageNoTitle(player, s);
        }

    }
}