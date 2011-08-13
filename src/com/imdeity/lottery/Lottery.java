package com.imdeity.lottery;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
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
    private static int taskId = -1;
    
    @Override
    public void onEnable() {
        
        loadSettings();
        checkPlugins();
        verifyDatabase();
        getCommand("lottery").setExecutor(new LotteryCommand(this));
        getCommand("lotteryadmin").setExecutor(new LotteryAdminCommand(this)); 
        taskId = getServer().getScheduler().scheduleAsyncRepeatingTask(this, new printAnnounce(), 20*60*60, 20*60*60 );
       out(this.getDescription().getVersion() + " Enabled.");
    
    }
    
    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTask(taskId);
        out(this.getDescription().getVersion() + " Disabled.");
    }
    
    private void loadSettings() {
        settings = new Settings(this);
        settings.loadSettings("config.yml", "/config.yml");
    }
    
    private void checkPlugins() {
        List<String> using = new ArrayList<String>();
        Plugin test;

        test = getServer().getPluginManager().getPlugin("Permissions");
        if (test == null)
            setSetting("USING_PERMISSIONS", false);
        else {
            permissions = ((Permissions)test).getHandler();
            if (Settings.isUsingPermissions())
                using.add("Permissions");
        }
        
        test = getServer().getPluginManager().getPlugin("iConomy");
        if (test == null)
            setSetting("USING_ICONOMY", false);
        else {
            iconomy = (iConomy)test;
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
        log.info("[" + pdfFile.getName()+ "] " + message);
    }
    
    public void sendGlobalMessage(String message) {
        for(Player p :this.getServer().getOnlinePlayers())
            p.sendMessage(ChatTools.Gray + "[Lottery] " + message);
    }
    
    public void sendNonFormattedGlobalMessage(String message) {
        for(Player p :this.getServer().getOnlinePlayers())
            p.sendMessage(message);
    }
    
    public void sendPlayerMessage(Player p, String message) {
            p.sendMessage(ChatTools.Gray + "[Lottery] " + message);
    }
    
    class printAnnounce implements Runnable
    {
        public void run()
        {
            if (Calendar.getInstance(TimeZone.getTimeZone("GMT-5:00")).get(Calendar.HOUR_OF_DAY) == Settings.getTicketTime()) {
               if (LotteryObject.getPot() != 0) {
                    sendNonFormattedGlobalMessage(ChatTools.formatTitle("Lottery"));
                    sendNonFormattedGlobalMessage(LotteryObject.drawWinner());
                }
            }
        }
     }
}
