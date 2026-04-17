package lib;

import lib.config.ReloadConfigEvent;
import lib.util.UtilFormat;
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

public class Copy implements Listener {
   private Server server;
   private String pn;
   private String adminPer;

   public Copy(Lib lib) {
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
         if (p == null) {
            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
            return;
         }

         if (!UtilPer.checkPer(p, this.adminPer)) {
            return;
         }

         if (length == 1) {
            ItemStack is = p.getItemInHand();
            if (is != null && is.getTypeId() != 0) {
               int amount;
               try {
                  amount = Integer.parseInt(args[0]);
                  if (amount <= 0) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(725)));
                     return;
                  }
               } catch (NumberFormatException var10) {
                  sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
                  return;
               }

               ItemStack result = is.clone();
               result.setAmount(amount);
               p.getInventory().addItem(new ItemStack[]{result});
               sender.sendMessage(UtilFormat.format(this.pn, "success", this.get(730)));
               return;
            }

            sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(720)));
            return;
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(700)));
         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(705), this.get(710)));
      } catch (NumberFormatException var11) {
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
}
