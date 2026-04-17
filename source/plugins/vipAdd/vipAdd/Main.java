package vipAdd;

import java.io.File;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import ticket.Code;

public class Main extends JavaPlugin implements Listener {
   private static String path;

   static {
      path = "e:" + File.separator + "fixvip";
   }

   public void onEnable() {
      Bukkit.getPluginManager().registerEvents(this, this);
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      Player p = e.getPlayer();
      File file = new File(path + File.separator + p.getName() + ".txt");

      try {
         if (file.exists() && Code.upgrade(p) != -1) {
            file.delete();
            p.sendMessage("§a成功转移VIP.");
         }
      } catch (Exception var5) {
      }

   }
}
