package com.imdeity.lottery;

import java.io.File;
import java.io.IOException;
import java.sql.SQLDataException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.imdeity.deityapi.Deity;
import com.imdeity.lottery.cmd.LotteryAdminCommand;
import com.imdeity.lottery.cmd.LotteryCommand;
import com.imdeity.lottery.objects.LotteryObject;

public class Lottery extends JavaPlugin {

    protected final Logger log = Logger.getLogger("Minecraft");
    public static FileConfiguration config = null;
    private static int taskId = -1;

    @Override
    public void onEnable() {
        Lottery.config = this.getConfig();
        try {
            Lottery.config.save(new File("plugins/Lottery/config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        verifyDatabase();
        getCommand("lottery").setExecutor(new LotteryCommand());
        getCommand("lotteryadmin").setExecutor(new LotteryAdminCommand());
        taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new Announce(), 0, (20 * 60 * 60));
        out(this.getDescription().getVersion() + " Enabled.");

    }

    @Override
    public void onDisable() {
        try {
            Lottery.config.save(new File("plugins/Lottery/config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        getServer().getScheduler().cancelTask(taskId);
        out(this.getDescription().getVersion() + " Disabled.");
    }

    private void verifyDatabase() {
        Deity.data.getDB()
                .Write("CREATE TABLE IF NOT EXISTS " + Deity.data.getDB().tableName("lottery_", "players") + " (" + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT," + "`username` varchar(16) NOT NULL," + "PRIMARY KEY (`id`)) ENGINE=MyISAM DEFAULT CHARSET=latin1 "
                        + "COMMENT='Current lottery players log';");

        Deity.data.getDB().Write(
                "CREATE TABLE IF NOT EXISTS " + Deity.data.getDB().tableName("lottery_", "winners") + " (" + "`id` int(10) unsigned NOT NULL AUTO_INCREMENT," + "`username` VARCHAR( 16 ) NOT NULL ," + "`winnings` INT( 10 ) NOT NULL DEFAULT  '0' ," + "`time` TIMESTAMP NOT NULL,"
                        + "`has_claimed` INT( 1 ) NOT NULL DEFAULT '0' ," + "PRIMARY KEY (`id`)) ENGINE=MyISAM DEFAULT CHARSET=latin1 " + "COMMENT='Past Lottery winners.';");
    }

    public void out(String message) {
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[" + pdfFile.getName() + "] " + message);
    }

    public class Announce implements Runnable {
        public void run() {
            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            String[] currTime = dateFormat.format(date).split(" ");
            String[] lastTime = null;
            try {
                lastTime = dateFormat.format(Deity.data.getDB().Read2("SELECT `time` FROM " + Deity.data.getDB().tableName("lottery_", "winners") + " ORDER BY `id` DESC LIMIT 1").getDate(0, "time")).split(" ");
            } catch (SQLDataException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DATE, -1);
                lastTime = dateFormat.format(cal.getTime()).split(" ");
            }
            int lastDate = Integer.parseInt((lastTime[0].split("/"))[2]);
            int currDate = Integer.parseInt((currTime[0].split("/"))[2]);

            int corrHour = Lottery.config.getInt("lottery.TIME");
            int currHour = Integer.parseInt((currTime[1].split(":"))[0]);

            boolean timeIsValid = (currHour == corrHour);
            boolean timeIsGreater = (currHour > corrHour);

            boolean dateIsDrawn = (currDate == (lastDate));
            boolean dateIsValid = ((currDate - 1) >= lastDate);

            if (LotteryObject.getPot() == 0) { return; }
            if (dateIsValid && timeIsValid) {
                // defaults
                LotteryObject.drawWinner();
            } else if (dateIsValid && timeIsGreater && !dateIsDrawn) {
                // behind more then an hour
                LotteryObject.drawWinner();
            } else if (dateIsDrawn) {
                // already drew
            }

        }
    }
}
