package com.imdeity.lottery.objects;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;
import com.imdeity.deityapi.exception.InvalidChannelException;
import com.imdeity.deityapi.exception.NegativeMoneyException;
import com.imdeity.lottery.util.ChatTools;
import com.imdeity.lottery.util.Settings;

public class LotteryObject {

    public static int getPot() {
        String sql = "SELECT COUNT(*) FROM " + Settings.getMySQLPlayersTable()
                + " ORDER BY `id` DESC LIMIT 1";

        try {
            return ((Integer.parseInt(Deity.data.getDB().Read(sql).get(1)
                    .get(0))) * (Settings.getTicketPrice()))
                    + (Settings.getExtraPot());
        } catch (Exception ex) {
            return 0;
        }
    }

    /**
     * @return
     */
    public static void drawWinner() {
        String sql = "";
        int winnings = 0;
        String winner = null;

        try {
            winnings = getPot();
            sql = "SELECT `username` FROM " + Settings.getMySQLPlayersTable()
                    + " ORDER BY RAND() LIMIT 1";
            if (Deity.data.getDB().Read(sql) != null) {
                winner = Deity.data.getDB().Read(sql).get(1).get(0);
            } else {
                System.out.println("[Lottery] Lottery Players table is empty.");
                if (Settings.isInDebug())
                    System.out.println("[Lottery Debug] "
                            + Deity.data.getDB().Read(sql));
                return;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        sql = "INSERT INTO  " + Settings.getMySQLWinnersTable() + " ("
                + "`username`, `winnings`, `time`)" + "VALUES (" + "'" + winner
                + "', '" + winnings + "', NOW());";
        if (winner != null) {
            Deity.data.getDB().Write(sql);

            double money = winnings;
            clear();
            try {
            	Deity.chat.sendPlayerMessage(Deity.server.getOnlinePlayer(winner), "Congrats! You have won the lottery! Use /lottery claim to claim your winnings");
                Deity.chat.sendMailToPlayer("[Lottery]", winner,
                        "You just won the daily lottery worth "
                                + winnings + " Dei! Claim your winnings with <teal>/lottery claim");
                Deity.chat
                		.broadcastHerochatMessage(Settings.getChannelName(), 
                				"<gold>[Lottery] Drawing the <yellow>ImDeity <gold>Daily Lottery...");
                Deity.chat
                        .broadcastHerochatMessage(Settings.getChannelName(),
                                "<gold>[Lottery] <green>" + winner + " <gold>just won <green>" + money
                                        + " <gold>dei in the daily Lottery! <teal>/lottery");
            } catch (InvalidChannelException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[Lottery] Player field was null");
        }
    }

    public static void claimPrize(Player player) {
        String sql = "SELECT `winnings` FROM "
                + Settings.getMySQLWinnersTable() + " "
                + " WHERE `username` = '" + player.getName() + "' AND "
                + "`has_claimed` = '0'";
        HashMap<Integer, ArrayList<String>> query = Deity.data.getDB().Read(sql);
        if (query == null) {
            Deity.chat.sendPlayerError(player, "Lottery", "You have no winnings that can be claimed at this time.");
        } else {
            double winnings = (double) Integer.parseInt(query.get(1).get(0));
            try {
                if (Deity.econ.receive(player.getName(), winnings)) {
                    Deity.chat.sendPlayerMessage(player, "[*] ", "<green>Thanks for playing in the lottery, "+winnings+" Dei has been added to your account!");
                    
                    sql = "UPDATE "+Settings.getMySQLWinnersTable()+ " SET `has_claimed` = 1 WHERE `username` = '"+player.getName()+"'";
                    Deity.data.getDB().Write(sql);
                }
            } catch (NegativeMoneyException e) {e.printStackTrace();}
        }
    }

    public static ArrayList<String> getWinners() {
        ArrayList<String> out = new ArrayList<String>();

        String sql = "";
        sql = "SELECT * FROM " + Settings.getMySQLWinnersTable()
                + " ORDER BY `id` DESC LIMIT 10";

        HashMap<Integer, ArrayList<String>> winners = Deity.data.getDB().Read(
                sql);
        for (int i = 1; i <= winners.size(); i++) {
            out.add(ChatTools.formatUserList(winners.get(i).get(1), winners
                    .get(i).get(2)));
        }
        return out;
    }

    public static boolean isWinner(String name) {

        String sql = "";
        sql = "SELECT * FROM " + Settings.getMySQLWinnersTable()
                + " ORDER BY `id` DESC LIMIT 10";

        HashMap<Integer, ArrayList<String>> winners = Deity.data.getDB().Read(
                sql);
        if (winners != null) {
            for (int i = 1; i <= winners.size(); i++) {
                if (winners.get(i).get(1).equalsIgnoreCase(name)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getNumTickets(String name) {
        String sql = "SELECT * FROM " + Settings.getMySQLPlayersTable()
                + " WHERE `username` = '" + name + "';";
        try {
            return Deity.data.getDB().Read(sql).size();
        } catch (Exception ex) {
            return 0;
        }
    }

    public static void clear() {
        String sql = "";

        sql = "TRUNCATE " + Settings.getMySQLPlayersTable() + ";";
        Deity.data.getDB().Write(sql);
    }
}
