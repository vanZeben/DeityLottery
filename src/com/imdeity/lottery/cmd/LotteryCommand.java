package com.imdeity.lottery.cmd;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.iConomy.iConomy;
import com.imdeity.lottery.Lottery;
import com.imdeity.objects.LotteryObject;
import com.imdeity.util.*;

public class LotteryCommand implements CommandExecutor {

    private static final List<String> output = new ArrayList<String>();
    private Lottery plugin = null;
     
    static {
        output.add(ChatTools.formatTitle("Lottery"));
        output.add(ChatTools.formatCommand("", "/lottery", "", "Checks current lottery info."));
        output.add(ChatTools.formatCommand("", "/lottery", "buy [n]", "Buys one or [n] of lottery tickets."));
        output.add(ChatTools.formatCommand("", "/lottery", "winners", "Gets past lottery winners."));
    }
    
    public LotteryCommand(Lottery instance) {
        this.plugin = instance;
        
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            if (Lottery.permissions.has(player, "lottery.general")) {
                parseCommand(player, args);
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    public void parseCommand(Player player, String[] split) {    
        if (split.length == 0){
            List<String> out = new ArrayList<String>();
            out.add(ChatTools.LightGray + "There is " + ChatTools.Yellow + LotteryObject.getPot()+".00 "+ChatTools.LightGray+"Dei in the current pot.");
            out.add(ChatTools.LightGray + "You have "+ ChatTools.Yellow + LotteryObject.getNumTickets(player.getName()) + ChatTools.LightGray + " Tickets.");
            out.add(ChatTools.LightGray + "Use /lottery ? for a list of commands.");
            for (String o : out) 
                plugin.sendPlayerMessage(player, o);
        } else if (split[0].equalsIgnoreCase("help") || split[0].equalsIgnoreCase("?")) {
            for (String o : output) 
                player.sendMessage(o);
        } else if (split[0].equalsIgnoreCase("buy") || split[0].equalsIgnoreCase("b")) {
             buyCommand(player, split);
        } else if (split[0].equalsIgnoreCase("winners") || split[0].equalsIgnoreCase("w")) {
             winnersCommand(player, split);
        } else {
            
        }
    }
    
    public void buyCommand(Player player, String[] split) {
        int numTicket = 1;
        String sql = "";
        if (split.length == 2)
            numTicket = Integer.parseInt(split[1]);
        
        sql = "INSERT INTO  "+Settings.getMySQLPlayersTable()+" (" +
                "`username` )" + "VALUES (" +
                "'"+player.getName()+"');";
        double money = numTicket * Settings.getTicketPrice();
         
        if (LotteryObject.isWinner(player.getName()))  {
            plugin.sendPlayerMessage(player, ChatTools.Red+"You have won a lottery in the past 10 days.");
            plugin.sendPlayerMessage(player, ChatTools.Red+"Don't be so greedy.");
            return;
        }
        
        if (numTicket > Settings.getMaxTickets() || (LotteryObject.getNumTickets(player.getName())+numTicket) >= Settings.getMaxTickets()) {
            plugin.sendPlayerMessage(player, ChatTools.Red+"You can only have a maximum of "+Settings.getMaxTickets()+" Tickets.");
            return;
        }
        
        if (verifyBalance(player, money)) {
            iConomy.getAccount(player.getName()).getHoldings().subtract(money);
            for (int i = 0; i<numTicket; i++) {
                Lottery.database.Write(sql);
            }
            plugin.sendGlobalMessage(ChatTools.White + player.getName() + ChatTools.LightGray + " bought " + ChatTools.Yellow + numTicket + ChatTools.LightGray + " Tickets.");
        } else {
            plugin.sendPlayerMessage(player, ChatTools.Red + "You do not have enough money to do this.");
        }
    }

    public void winnersCommand(Player player, String[] split) {
        ArrayList<String> winners = LotteryObject.getWinners();
        player.sendMessage(ChatTools.formatTitle("Auction Winners"));
        for (String s : winners) {
            plugin.sendPlayerMessage(player, s);
        }

    }
    
    public boolean verifyBalance(Player player, double checkAmount) {
        if (iConomy.getAccount(player.getName()).getHoldings().hasEnough(checkAmount))
            return true;
        return false;
    }
}