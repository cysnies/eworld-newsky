package lib;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Eco {
   private Server server;
   private Economy econ;

   public Eco(Lib lib) {
      this.server = lib.getServer();
      this.setupEconomy();
   }

   private void setupEconomy() {
      RegisteredServiceProvider<Economy> rsp = this.server.getServicesManager().getRegistration(Economy.class);
      this.econ = (Economy)rsp.getProvider();
   }

   public double get(Player p) {
      return this.get(p.getName());
   }

   public double get(String name) {
      return this.econ.hasAccount(name) ? this.econ.getBalance(name) : (double)-1.0F;
   }

   public boolean set(Player p, int amount) {
      return this.set(p.getName(), (double)amount);
   }

   public boolean set(String name, int amount) {
      return this.set(name, (double)amount);
   }

   public boolean set(Player p, double amount) {
      return this.set(p.getName(), amount);
   }

   public boolean set(String name, double amount) {
      if (amount < (double)0.0F) {
         return false;
      } else if (!this.econ.hasAccount(name)) {
         return false;
      } else {
         double num = this.econ.getBalance(name);
         double diff = amount - num;
         if (diff == (double)0.0F) {
            return true;
         } else {
            return diff > (double)0.0F ? this.econ.depositPlayer(name, diff).transactionSuccess() : this.econ.withdrawPlayer(name, -diff).transactionSuccess();
         }
      }
   }

   public boolean add(Player p, int amount) {
      return this.add(p.getName(), (double)amount);
   }

   public boolean add(String name, int amount) {
      return this.add(name, (double)amount);
   }

   public boolean add(Player p, double amount) {
      return this.add(p.getName(), amount);
   }

   public boolean add(String name, double amount) {
      return !(amount < (double)0.0F) && this.econ.hasAccount(name) ? this.econ.depositPlayer(name, amount).transactionSuccess() : false;
   }

   public boolean del(Player p, int amount) {
      return this.del(p.getName(), (double)amount);
   }

   public boolean del(String name, int amount) {
      return this.del(name, (double)amount);
   }

   public boolean del(Player p, double amount) {
      return this.del(p.getName(), amount);
   }

   public boolean del(String name, double amount) {
      return !(amount < (double)0.0F) && this.econ.hasAccount(name) ? this.econ.withdrawPlayer(name, amount).transactionSuccess() : false;
   }
}
