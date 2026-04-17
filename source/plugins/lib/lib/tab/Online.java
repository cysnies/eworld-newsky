package lib.tab;

import java.util.HashMap;
import lib.Lib;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilTab;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Online implements Listener {
   private static final String MODE = "online";
   private String pn;
   private int mode = 0;
   private String pre = "";
   private String suf = "";
   private HashMap playerHash;

   public Online(Lib lib) {
      this.pn = lib.getPn();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, lib);
      UtilTab.register("online");
      this.playerHash = UtilTab.getMode("online");
   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void onReloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      this.checkInit(p);
      ((Tab.Mode)this.playerHash.get(p)).add(p.getName(), this.getName(p));

      Player[] var6;
      for(Player tar : var6 = Bukkit.getOnlinePlayers()) {
         if (!p.equals(tar)) {
            ((Tab.Mode)this.playerHash.get(p)).add(tar.getName(), this.getName(tar));
            ((Tab.Mode)this.playerHash.get(tar)).add(p.getName(), this.getName(p));
         }
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      Player p = e.getPlayer();
      this.playerHash.remove(p);

      Player[] var6;
      for(Player tar : var6 = Bukkit.getOnlinePlayers()) {
         if (!p.equals(tar)) {
            Tab.Mode mode = (Tab.Mode)this.playerHash.get(tar);
            mode.remove(mode.getPos(p.getName()));
         }
      }

   }

   private void checkInit(Player p) {
      if (!this.playerHash.containsKey(p)) {
         this.playerHash.put(p, new Tab.Mode());
      }

   }

   private String getName(Player p) {
      switch (this.mode) {
         case 0:
            return this.pre + p.getName() + this.suf;
         case 1:
            return this.pre + p.getDisplayName() + this.suf;
         case 2:
            return this.pre + p.getPlayerListName() + this.suf;
         default:
            return this.pre + p.getName() + this.suf;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.mode = config.getInt("online.mode");
      this.pre = Util.convert(config.getString("online.pre"));
      this.suf = Util.convert(config.getString("online.suf"));
   }
}
