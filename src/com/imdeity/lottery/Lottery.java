package com.imdeity.lottery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.imdeity.lottery.cmd.LotteryAdminCommand;
import com.imdeity.lottery.cmd.LotteryCommand;
import com.imdeity.objects.LotteryObject;
import com.imdeity.util.*;

import com.iConomy.iConomy;
import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

public class Lottery extends JavaPlugin {

    protected final Logger log = Logger.getLogger("Minecraft");

    public static PermissionHandler permissions = null;
    public static iConomy iconomy = null;
    public static MySQL database = null;
    public static Settings settings = null;
    public static Settings debug = null;
    private static int taskId = -1;

    @Override
    public void onEnable() {

        loadSettings();
        checkPlugins();
        verifyDatabase();
        getCommand("lottery").setExecutor(new LotteryCommand(this));
        getCommand("lotteryadmin").setExecutor(new LotteryAdminCommand(this));
        taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this,
                new Announce(), 0, (20 * 60 * 60));
        out(this.getDescription().getVersion() + " Enabled.");

    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTask(taskId);
        out(this.getDescription().getVersion() + " Disabled.");
    }

    private void loadSettings() {
        settings = new Settings(this);
        debug = new Settings(this);
        debug.loadSettings("debug.yml", "/debug.yml");
        settings.loadSettings("config.yml", "/config.yml");
    }

    private void checkPlugins() {
        List<String> using = new ArrayList<String>();
        Plugin test;

        test = getServer().getPluginManager().getPlugin("Permissions");
        if (test == null)
            setSetting("USING_PERMISSIONS", false);
        else {
            permissions = ((Permissions) test).getHandler();
            if (Settings.isUsingPermissions())
                using.add("Permissions");
        }

        test = getServer().getPluginManager().getPlugin("iConomy");
        if (test == null)
            setSetting("USING_ICONOMY", false);
        else {
            iconomy = (iConomy) test;
            if (Settings.isUsingIConomy())
                using.add("iConomy");
        }
        if (using.size() > 0)
            out("Using: " + StringMgmt.join(using, ", ") + ".");
    }

    private void verifyDatabase() {
        database = new MySQL();
    }

    public void setSetting(String root, Object value) {
        Settings.setProperty(root, value);
    }

    public void out(String message) {
        PluginDescriptionFile pdfFile = this.getDescription();
        log.info("[" + pdfFile.getName() + "] " + message);
    }

    public void sendGlobalMessage(String message) {
        for (Player p : this.getServer().getOnlinePlayers())
           ChatTools.formatAndSend(message, "Lottery", p);
    }

    public class Announce implements Runnable {
        public void run() {
            String[] time = database.Read("SELECT NOW()").get(1).get(0)
                    .split(" ");
            double currentTime = Double.parseDouble((time[1].split(":"))[0]);
            boolean timeIsValid = (currentTime == Settings.getTicketTime());
            boolean timeIsGreater = (currentTime > Settings.getTicketTime());

            String[] lastTime = database
                    .Read("SELECT * FROM " + Settings.getMySQLWinnersTable()
                            + " ORDER BY `id` DESC LIMIT 1").get(1).get(3)
                    .split(" ");
            int lastDrawDate = Integer.parseInt((lastTime[0].split("-"))[2]);
            int currentDrawDate = Integer.parseInt((time[0].split("-"))[2]);

            boolean dateIsDrawn = (currentDrawDate == (lastDrawDate));
            boolean dateIsValid = ((currentDrawDate - 1) >= lastDrawDate);

            if (Settings.isInDebug()) {
                String output = "";

                if (LotteryObject.getPot() == 0) {
                    output += "No Money in pot";
                    return;
                }
                if (timeIsValid && dateIsValid) {
                    // defaults
                    sendGlobalMessage(LotteryObject.drawWinner());
                    output += "Lottery Ran on time: " + time[1] + "\n";
                    output += "lastDraw: " + lastDrawDate + "|"
                            + "currentDraw " + currentDrawDate;

                } else if (dateIsValid && timeIsGreater && (!dateIsDrawn))  {
                    // behind more then an hour
                    sendGlobalMessage(LotteryObject.drawWinner());
                    output += "Lottery was behind today: " + time[1] + "\n";
                    output += "lastDraw: " + lastDrawDate + "|"
                            + "currentDraw " + currentDrawDate;
                } else if (dateIsDrawn) {
                    // already drew
                    output += "Lottery already ran today: " + currentTime;
                } else {
                    output = "Checking Lottery: " + time[1];
                }
                try {
                    FileMgmt.stringToFile(output, settings.getRootFolder()
                            + FileMgmt.fileSeparator() + "debug.yml", true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                if (timeIsValid && dateIsValid) {
                    // defaults
                    sendGlobalMessage(LotteryObject.drawWinner());
                } else if (dateIsValid && timeIsGreater && (!dateIsDrawn))  {
                    // behind more then an hour
                    sendGlobalMessage(LotteryObject.drawWinner());
                } else if (dateIsDrawn) {
                    // already drew
                }
            }
        }
    }
}
