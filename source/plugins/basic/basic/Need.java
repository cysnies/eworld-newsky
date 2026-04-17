package basic;

import lib.config.ReloadConfigEvent;
import lib.util.Util;
import lib.util.UtilConfig;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Need implements Listener {
   private static final String LIB = "lib";
   private String pn = Basic.getPn();
   private int base;
   private int rate;

   public Need(Basic main) {
      this.loadConfig(UtilConfig.getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, main);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         String cmdName = cmd.getName();
         int length = args.length;
         if (cmdName.equalsIgnoreCase("pay")) {
            if (p == null) {
               sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(120)}));
               return;
            }

            if ((length != 1 || !args[0].equalsIgnoreCase("?")) && length == 2) {
               this.pay(p, args[0], Integer.parseInt(args[1]));
               return;
            }

            sender.sendMessage(UtilFormat.format("lib", "cmdHelpHeader", new Object[]{this.get(386)}));
            sender.sendMessage(UtilFormat.format("lib", "cmdHelpItem", new Object[]{this.get(388), this.get(390)}));
         }
      } catch (NumberFormatException var8) {
         sender.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(415)}));
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

   public void pay(Player p, String tar, int amount) {
      tar = Util.getRealName(p, tar);
      if (tar != null) {
         if (p.getName().equals(tar)) {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(410)}));
         } else if (amount <= 0) {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(400)}));
         } else if (UtilEco.get(p.getName()) < (double)amount) {
            p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(405)}));
         } else {
            int tax = 0;
            tax = this.base + this.rate * amount / 10000;
            int get = amount - tax;
            if (get <= 0) {
               p.sendMessage(UtilFormat.format("lib", "fail", new Object[]{this.get(545)}));
            } else {
               UtilEco.add(tar, (double)get);
               UtilEco.del(p.getName(), (double)amount);
               p.sendMessage(UtilFormat.format(this.pn, "pay", new Object[]{tar, amount}));

               try {
                  Bukkit.getPlayerExact(tar).sendMessage(UtilFormat.format(this.pn, "pay2", new Object[]{p.getName(), tax, get}));
               } catch (Exception var7) {
               }

               Util.sendConsoleMessage(UtilFormat.format(this.pn, "pay3", new Object[]{p.getName(), tar, amount, tax}));
            }
         }
      }
   }

   private void loadConfig(YamlConfiguration config) {
      this.base = config.getInt("pay.base");
      this.rate = config.getInt("pay.rate");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
