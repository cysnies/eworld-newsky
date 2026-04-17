package lib;

import java.util.HashMap;
import lib.util.UtilFormat;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class RealName implements Listener {
   private Lib lib;
   private String pn;
   private HashMap realNameHash;

   public RealName(Lib lib) {
      this.lib = lib;
      this.pn = lib.getPn();
      lib.getPm().registerEvents(this, lib);
      this.loadData();
   }

   public String getRealName(CommandSender sender, String name) {
      String check = name.toLowerCase();
      String result = (String)this.realNameHash.get(check);
      if (result == null && sender != null) {
         sender.sendMessage(UtilFormat.format(this.pn, "notExsitPlayer", name));
      }

      return result;
   }

   @EventHandler(
      priority = EventPriority.LOWEST,
      ignoreCancelled = true
   )
   public void onPlayerLogin(PlayerLoginEvent e) {
      String realName = this.getRealName((CommandSender)null, e.getPlayer().getName());
      if (realName != null && !realName.equals(e.getPlayer().getName())) {
         e.setResult(Result.KICK_OTHER);
         String msg = UtilFormat.format(this.pn, "kickRealName", e.getPlayer().getName(), realName);
         e.setKickMessage(msg);
      }

   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerJoin(PlayerJoinEvent e) {
      String name = e.getPlayer().getName();
      String check = name.toLowerCase();
      if (!this.realNameHash.containsKey(check)) {
         User user = new User(name);
         this.realNameHash.put(check, name);
         this.lib.getDao().addOrUpdateUser(user);
      }

   }

   private void loadData() {
      this.realNameHash = new HashMap();

      for(User user : this.lib.getDao().getAllUsers()) {
         this.realNameHash.put(user.getName().toLowerCase(), user.getName());
      }

   }
}
