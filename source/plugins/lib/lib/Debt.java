package lib;

import java.util.HashMap;
import java.util.List;
import lib.config.ReloadConfigEvent;
import lib.time.TimeEvent;
import lib.util.Util;
import lib.util.UtilEco;
import lib.util.UtilFormat;
import lib.util.UtilPer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class Debt implements Listener {
   private Lib lib;
   private String pn;
   private HashMap userHash;
   private String per_lib_admin;
   private String infoOtherPer;
   private int interval;
   private int log;

   public Debt(Lib lib) {
      this.lib = lib;
      this.pn = lib.getPn();
      this.loadData();
      this.loadConfig(lib.getCon().getConfig(this.pn));
      Bukkit.getPluginManager().registerEvents(this, lib);
   }

   public void onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      try {
         Player p = null;
         if (!(sender instanceof ConsoleCommandSender)) {
            p = (Player)sender;
         }

         int length = args.length;
         if (length != 1 || !args[0].equalsIgnoreCase("?")) {
            if (length == 1) {
               if (args[0].equalsIgnoreCase("show")) {
                  if (p == null) {
                     sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(127)));
                  } else {
                     this.show(sender, p.getName());
                  }

                  return;
               }
            } else if (length == 2) {
               if (args[0].equalsIgnoreCase("show")) {
                  this.show(sender, args[1]);
                  return;
               }
            } else if (length == 3) {
               if (args[0].equalsIgnoreCase("add")) {
                  if (p != null && !UtilPer.checkPer(p, this.per_lib_admin)) {
                     return;
                  }

                  this.addDebt(args[1], Integer.parseInt(args[2]), (String)null);
                  return;
               }
            } else if (length == 4 && args[0].equalsIgnoreCase("add")) {
               if (p != null && !UtilPer.checkPer(p, this.per_lib_admin)) {
                  return;
               }

               this.addDebt(args[1], Integer.parseInt(args[2]), args[3]);
               return;
            }
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpHeader", this.get(1605)));
         if (p == null || UtilPer.hasPer(p, this.per_lib_admin)) {
            sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(1610), this.get(1615)));
         }

         sender.sendMessage(UtilFormat.format(this.pn, "cmdHelpItem", this.get(1620), this.get(1625)));
      } catch (NumberFormatException var7) {
         sender.sendMessage(UtilFormat.format(this.pn, "fail", this.get(126)));
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
      priority = EventPriority.LOW
   )
   public void onTime(TimeEvent e) {
      if (TimeEvent.getTime() % (long)this.interval == 0L) {
         this.checkAllPay();
      }

   }

   public int getDebt(String name) {
      name = Util.getRealName((CommandSender)null, name);
      if (name == null) {
         return -1;
      } else {
         DebtUser du = this.checkInit(name);
         return du.getDebt();
      }
   }

   public boolean addDebt(String name, int debt, String reason) {
      if (debt <= 0) {
         return false;
      } else {
         name = Util.getRealName((CommandSender)null, name);
         if (name == null) {
            return false;
         } else {
            DebtUser du = this.checkInit(name);
            du.setDebt(du.getDebt() + debt);
            this.lib.getDao().addOrUpdateDebtUser(du);
            if (reason == null || reason.isEmpty()) {
               reason = this.get(1600);
            }

            this.log(name, debt, reason);
            Util.sendConsoleMessage(UtilFormat.format(this.pn, "debtAdd2", name, debt, reason));
            Util.sendMsg(name, UtilFormat.format(this.pn, "debtAdd", debt, reason));
            return true;
         }
      }
   }

   public void show(CommandSender sender, String tar) {
      tar = Util.getRealName(sender, tar);
      if (tar != null) {
         Player p = null;
         if (sender instanceof Player) {
            p = (Player)sender;
         }

         if (p == null || p.getName().equals(tar) || UtilPer.checkPer(p, this.infoOtherPer)) {
            DebtUser du = this.checkInit(tar);
            sender.sendMessage(UtilFormat.format(this.pn, "debtStart", tar, this.log));

            for(String s : du.getLog()) {
               sender.sendMessage(s);
            }

            sender.sendMessage(UtilFormat.format(this.pn, "debtTotal", du.getDebt()));
         }
      }
   }

   private void log(String name, int debt, String reason) {
      DebtUser du = this.checkInit(name);
      List<String> log = du.getLog();
      String s = UtilFormat.format(this.pn, "debtShow", Util.getDate(), debt, reason);
      log.add(s);
      if (log.size() > this.log) {
         int cut = log.size() - this.log;

         for(int i = 0; i < cut; ++i) {
            try {
               log.remove(0);
            } catch (Exception var10) {
            }
         }
      }

      this.lib.getDao().addOrUpdateDebtUser(du);
   }

   private void checkAllPay() {
      Player[] var4;
      for(Player p : var4 = Bukkit.getOnlinePlayers()) {
         this.checkPay(p);
      }

   }

   private void checkPay(Player p) {
      DebtUser du = this.checkInit(p.getName());
      if (du.getDebt() > 0) {
         int has = (int)UtilEco.get(p.getName());
         int pay = Math.min(has, du.getDebt());
         if (pay > 0 && UtilEco.del(p.getName(), (double)pay)) {
            du.setDebt(du.getDebt() - pay);
            this.lib.getDao().addOrUpdateDebtUser(du);
            p.sendMessage(UtilFormat.format(this.pn, "debtPay", pay));
         }
      }

   }

   private DebtUser checkInit(String name) {
      DebtUser du = (DebtUser)this.userHash.get(name);
      if (du == null) {
         du = new DebtUser(name);
         this.userHash.put(name, du);
         this.lib.getDao().addOrUpdateDebtUser(du);
      }

      return du;
   }

   private void loadData() {
      this.userHash = new HashMap();

      for(DebtUser debtUser : this.lib.getDao().getAllDebtUsers()) {
         this.userHash.put(debtUser.getName(), debtUser);
      }

   }

   private void loadConfig(YamlConfiguration config) {
      this.per_lib_admin = config.getString("per_lib_admin");
      this.infoOtherPer = config.getString("debt.per.other");
      this.interval = config.getInt("debt.interval");
      this.log = config.getInt("debt.log");
   }

   private String get(int id) {
      return UtilFormat.format(this.pn, id);
   }
}
