package autoCmd;

import java.util.Random;
import lib.config.ReloadConfigEvent;
import lib.hashList.HashList;
import lib.hashList.HashListImpl;
import lib.time.TimeEvent;
import lib.util.UtilConfig;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class AutoCmd implements Listener {
   private Random r = new Random();
   private String pn = Main.getPn();
   private HashList cmdInfoList;

   public AutoCmd(Main main) {
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      for(CmdInfo cmdInfo : this.cmdInfoList) {
         if (TimeEvent.getTime() % (long)cmdInfo.getInterval() == 0L && this.r.nextInt(100) < cmdInfo.getChance()) {
            if (cmdInfo.getSender() == 1) {
               Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmdInfo.getCmd());
            } else {
               Player[] playerList = Bukkit.getOnlinePlayers();
               Player p = playerList[this.r.nextInt(playerList.length)];
               p.chat(cmdInfo.getCmd().replaceAll("<name>", p.getName()));
            }
         }
      }

   }

   private void loadConfig(FileConfiguration config) {
      this.cmdInfoList = new HashListImpl();

      for(int index = 1; config.contains("cmds.cmd" + index); ++index) {
         int interval = config.getInt("cmds.cmd" + index + ".interval");
         int chance = config.getInt("cmds.cmd" + index + ".chance");
         int sender = config.getInt("cmds.cmd" + index + ".sender");
         String cmd = config.getString("cmds.cmd" + index + ".cmd");
         CmdInfo cmdInfo = new CmdInfo(interval, chance, sender, cmd);
         this.cmdInfoList.add(cmdInfo);
      }

   }

   class CmdInfo {
      private int interval;
      private int chance;
      private int sender;
      private String cmd;

      public CmdInfo(int interval, int chance, int sender, String cmd) {
         this.interval = interval;
         this.chance = chance;
         this.sender = sender;
         this.cmd = cmd;
      }

      public int getInterval() {
         return this.interval;
      }

      public int getChance() {
         return this.chance;
      }

      public int getSender() {
         return this.sender;
      }

      public String getCmd() {
         return this.cmd;
      }
   }
}
