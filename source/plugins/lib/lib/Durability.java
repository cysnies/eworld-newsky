package lib;

import lib.config.ReloadConfigEvent;
import lib.util.UtilFormat;
import lib.util.UtilItems;
import lib.util.UtilPer;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

public class Durability implements Listener {
   private Server server;
   private String pn;
   private String adminPer;

   public Durability(Lib lib) {
      this.server = lib.getServer();
      this.pn = lib.getPn();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      this.server.getPluginManager().registerEvents(this, lib);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         int length = args.length;
         if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 2 && args[0].equalsIgnoreCase("set")) {
            this.setDurability(sender, Integer.parseInt(args[1]));
            return;
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(500)));
         if (p == null || UtilPer.hasPer(p, this.adminPer)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(505), this.get(510)));
         }
      } catch (NumberFormatException var7) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
      }

   }

   @EventHandler(
      priority = EventPriority.LOW
   )
   public void reloadConfig(ReloadConfigEvent e) {
      if (e.getCallPlugin().equals(this.pn)) {
         this.loadConfig(e.getConfig());
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.adminPer = config.getString("durability.adminPer");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }

   private void setDurability(CommandSender sender, int durability) {
      Player p = null;
      if (sender instanceof Player) {
         p = (Player)sender;
      }

      if (p == null) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
      } else if (UtilPer.checkPer(p, this.adminPer)) {
         ItemStack is = p.getItemInHand();
         if (is != null && is.getTypeId() != 0) {
            if (!UtilItems.hasDurability(is)) {
               p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(520)));
            } else {
               if (durability > is.getType().getMaxDurability()) {
                  is.setDurability(is.getType().getMaxDurability());
               } else {
                  is.setDurability((short)durability);
               }

            }
         } else {
            p.sendMessage(UtilFormat.format(this.pn, "fail", this.get(515)));
         }
      }
   }
}
