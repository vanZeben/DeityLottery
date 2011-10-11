package com.imdeity.lottery.objects;

import java.util.ArrayList;
import java.util.HashMap;

import com.imdeity.lottery.util.ChatTools;
import com.imdeity.lottery.util.Settings;
import com.imdeity.profile.Deity;
import com.imdeity.profile.exception.InvalidChannelException;
import com.imdeity.profile.exception.NegativeMoneyException;

public class LotteryObject {

    public static int getPot() {
        String sql = "SELECT * FROM " + Settings.getMySQLPlayersTable()
                + " ORDER BY `id` DESC LIMIT 1";

        try {
            return ((Integer.parseInt(Deity.server.getDB().Read(sql).get(1).get(0))) * (Settings
                    .getTicketPrice())) + (Settings.getExtraPot());
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
        String winner = "";

        try {
            winnings = getPot();
            sql = "SELECT `username` FROM " + Settings.getMySQLPlayersTable()
                    + " ORDER BY RAND() LIMIT 1";
            if (!Deity.server.getDB().Read(sql).isEmpty()  && !Deity.server.getDB().Read(sql).get(1).isEmpty() && Deity.server.getDB().Read(sql).get(1).get(0) != null) {
                winner = Deity.server.getDB().Read(sql).get(1).get(0); 
            } else {
                System.out.println("[Lottery] Lottery Players table is empty.");
                if (Settings.isInDebug())
                    System.out.println("[Lottery Debug] " + Deity.server.getDB().Read(sql));
                return;
            }
        } catch (Exception ex) { 
            ex.printStackTrace();
        }

        sql = "INSERT INTO  " + Settings.getMySQLWinnersTable() + " ("
                + "`username`, `winnings`, `time`)" + "VALUES (" + "'"
                + winner + "', '" + winnings + "', NOW());";
        if (!winner.isEmpty() && winner != null) {
            Deity.server.getDB().Write(sql);
            
            double money = winnings;
            try {
                Deity.econ.receive(winner, money);
            } catch (NegativeMoneyException e) {
                e.printStackTrace();
            }
            Deity.chat.sendMailToPlayer("ImDeityBot", winner, "Guess what? You just won the daily lottery worth "+winnings+" Dei!");
            clear();
            try {
                Deity.chat.broadcastHerochatMessage("global", "ImDeityBot", "Guess what everybody... "+winner+" just won "+money+" dei in the lottery.");
            } catch (InvalidChannelException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("[Lottery] Player field was null");
        }
    }

    public static ArrayList<String> getWinners() {
        ArrayList<String> out = new ArrayList<String>();

        String sql = "";
        sql = "SELECT * FROM " + Settings.getMySQLWinnersTable()
                + " ORDER BY `id` DESC LIMIT 10";

        HashMap<Integer, ArrayList<String>> winners = Deity.server.getDB().Read(sql);
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

        HashMap<Integer, ArrayList<String>> winners = Deity.server.getDB().Read(sql);
        for (int i = 1; i <= winners.size(); i++) {
            if (winners.get(i).get(1).equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public static int getNumTickets(String name) {
        String sql = "SELECT * FROM " + Settings.getMySQLPlayersTable()
                + " WHERE `username` = '" + name + "';";
        try {
            return Deity.server.getDB().Read(sql).size();
        } catch (Exception ex) {
            return 0;
        }
    }

    public static void clear() {
        String sql = "";

        sql = "TRUNCATE " + Settings.getMySQLPlayersTable() + ";";
        Deity.server.getDB().Write(sql);
    }
}
