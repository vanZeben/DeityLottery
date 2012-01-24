package com.imdeity.lottery.objects;

import java.sql.SQLDataException;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.entity.Player;

import com.imdeity.deityapi.Deity;
import com.imdeity.deityapi.exception.InvalidChannelException;
import com.imdeity.deityapi.exception.NegativeMoneyException;
import com.imdeity.deityapi.records.DatabaseResults;
import com.imdeity.lottery.Lottery;
import com.imdeity.lottery.util.ChatTools;

public class LotteryObject {

	public static int getPot() {
		String sql = "SELECT COUNT(*) FROM "
				+ Deity.data.getDB().tableName("lottery_", "players")
				+ " ORDER BY `id` DESC LIMIT 1";

		try {
			HashMap<Integer, ArrayList<Object>> query = Deity.data.getDB().Read(sql);
			if (query != null) {
				return (Integer.parseInt(""+query.get(1).get(0))
						* (Lottery.config.getInt("lottery.ticket_price")) + (Lottery.config
						.getInt("lottery.pot")));
			} else {
				return 0;
			}
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
			sql = "SELECT `username` FROM "
					+ Deity.data.getDB().tableName("lottery_", "players")
					+ " ORDER BY RAND() LIMIT 1";
			if (Deity.data.getDB().Read(sql) != null) {
				winner = Deity.data.getDB().Read2(sql).getString(0, "username");
			} else {
				System.out.println("[Lottery] Lottery Players table is empty.");
				if (Lottery.config.getBoolean("lottery.debug_mode"))
					System.out.println("[Lottery Debug] "
							+ Deity.data.getDB().Read(sql));
				return;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		sql = "INSERT INTO  "
				+ Deity.data.getDB().tableName("lottery_", "winners") + " ("
				+ "`username`, `winnings`, `time`)" + "VALUES (" + "'" + winner
				+ "', '" + winnings + "', NOW());";
		if (winner != null) {
			Deity.data.getDB().Write(sql);

			double money = winnings;
			clear();
			try {
				Deity.chat
						.sendPlayerMessage(
								Deity.server.getOnlinePlayer(winner),
								"Congrats! You have won the lottery! Use /lottery claim to claim your winnings");
				Deity.chat
						.sendMailToPlayer(
								"[Lottery]",
								winner,
								"You just won the daily lottery worth "
										+ winnings
										+ " Dei! Claim your winnings with <teal>/lottery claim");
				Deity.chat
						.broadcastHerochatMessage(Lottery.config
								.getString("lottery.channel_name"),
								"<gold>[Lottery] Drawing the <yellow>ImDeity <gold>Daily Lottery...");
				Deity.chat
						.broadcastHerochatMessage(
								Lottery.config
										.getString("lottery.channel_name"),
								"<gold>[Lottery] <green>"
										+ winner
										+ " <gold>just won <green>"
										+ money
										+ " <gold>dei in the daily Lottery! <teal>/lottery");
				System.out.println("[Lottery] "+winner+" won todays lottery");
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
				+ Deity.data.getDB().tableName("lottery_", "winners")
				+ " WHERE `username` = '" + player.getName() + "' AND "
				+ "`has_claimed` = '0';";
		DatabaseResults query = Deity.data.getDB().Read2(sql);
		if (query == null) {
			Deity.chat.sendPlayerError(player, "Lottery",
					"You have no winnings that can be claimed at this time.");
		} else {
			double winnings = 0;
			try {
				winnings = (double) query.getInteger(0, "winnings");
			} catch (SQLDataException e1) {
				e1.printStackTrace();
			}
			try {
				if (Deity.econ.receive(player.getName(), winnings)) {
					Deity.chat.sendPlayerMessage(player, "[*] ",
							"<green>Thanks for playing in the lottery, "
									+ winnings
									+ " Dei has been added to your account!");

					sql = "UPDATE "
							+ Deity.data.getDB().tableName("lottery_",
									"winners")
							+ " SET `has_claimed` = 1 WHERE `username` = '"
							+ player.getName() + "'";
					Deity.data.getDB().Write(sql);
				}
			} catch (NegativeMoneyException e) {
				e.printStackTrace();
			}
		}
	}

	public static ArrayList<String> getWinners() {
		ArrayList<String> out = new ArrayList<String>();

		String sql = "";
		sql = "SELECT `username`, `winnings` FROM "
				+ Deity.data.getDB().tableName("lottery_", "winners")
				+ " ORDER BY `id` DESC LIMIT 10";

		DatabaseResults query = Deity.data.getDB().Read2(sql);
		for (int i = 0; i <= query.rowCount(); i++) {
			try {
				out.add(ChatTools.formatUserList(
						query.getString(i, "username"),
						"" + query.getInteger(i, "winnings")));
			} catch (SQLDataException e) {
				e.printStackTrace();
			}
		}
		return out;
	}

	public static boolean isWinner(String name) {

		String sql = "";
		sql = "SELECT `username` FROM "
				+ Deity.data.getDB().tableName("lottery_", "winners")
				+ " ORDER BY `id` DESC LIMIT 10";

		DatabaseResults query = Deity.data.getDB().Read2(sql);
		if (query != null) {
			for (int i = 1; i <= query.rowCount(); i++) {
				try {
					if (query.getString(i, "username").equalsIgnoreCase(name)) {
						return true;
					}
				} catch (SQLDataException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static int getNumTickets(String name) {
		String sql = "SELECT `id` FROM "
				+ Deity.data.getDB().tableName("lottery_", "players")
				+ " WHERE `username` = '" + name + "';";
		try {
			return Deity.data.getDB().Read2(sql).rowCount();
		} catch (Exception ex) {
			return 0;
		}
	}

	public static void clear() {
		String sql = "";

		sql = "TRUNCATE " + Deity.data.getDB().tableName("lottery_", "players")
				+ ";";
		Deity.data.getDB().Write(sql);
	}
}
