package lib;

import java.util.HashMap;
import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilFormat;
import lib.util.UtilSpeed;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class InputManager implements Listener {
   private static final String INPUT = "input";
   private String pn;
   private HashMap inputHash;
   private int interval;

   public InputManager(Lib lib) {
      this.pn = lib.getPn();
      this.inputHash = new HashMap();
      this.loadConfig(UtilConfig.getConfig(this.pn));
      lib.getPm().registerEvents(this, lib);
      UtilSpeed.register(this.pn, "input");
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(UtilFormat.format((String)null, "fail", this.get(1400)));
      } else {
         Player p = (Player)sender;
         InputHandler inputHandler = (InputHandler)this.inputHash.get(p);
         if (inputHandler == null) {
            sender.sendMessage(UtilFormat.format((String)null, "fail", this.get(1405)));
         } else {
            String msg = Util.combine(args, " ", 0, args.length);
            if (inputHandler.onInput(msg)) {
               this.inputHash.remove(p);
            } else {
               p.sendMessage(this.get(1410));
            }

         }
      }
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
      this.inputHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerQuit(PlayerQuitEvent e) {
      this.inputHash.remove(e.getPlayer());
   }

   @EventHandler(
      priority = EventPriority.LOWEST
   )
   public void onPlayerInteract(PlayerInteractEvent e) {
      this.checkCancelInput(e.getPlayer());
   }

   public boolean input(Player p, InputHandler inputHandler, String tip) {
      if (!UtilSpeed.check(p, this.pn, "input", this.interval)) {
         return false;
      } else {
         this.checkCancelInput(p);
         this.inputHash.put(p, inputHandler);
         p.sendMessage(tip);
         p.sendMessage(this.get(1415));
         return true;
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.interval = config.getInt("input.interval");
   }

   private void checkCancelInput(Player p) {
      if (this.inputHash.containsKey(p)) {
         this.inputHash.remove(p);
         p.sendMessage(UtilFormat.format((String)null, "fail", this.get(1420)));
      }

   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   public interface InputHandler {
      boolean onInput(String var1);
   }
}
