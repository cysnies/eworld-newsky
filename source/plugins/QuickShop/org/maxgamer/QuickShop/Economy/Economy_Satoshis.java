package org.maxgamer.QuickShop.Economy;

import me.meta1203.plugins.satoshis.Satoshis;
import me.meta1203.plugins.satoshis.SatoshisEconAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class Economy_Satoshis implements EconomyCore {
   SatoshisEconAPI econ;

   public Economy_Satoshis() {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("Satoshis");
      if (plugin == null) {
         throw new NoClassDefFoundError("Satoshis was not found!");
      } else {
         Bukkit.getLogger().info("Hooking Economy");
         this.econ = Satoshis.econ;
      }
   }

   public boolean isValid() {
      return true;
   }

   public boolean deposit(String name, double amount) {
      this.econ.addFunds(name, amount);
      return true;
   }

   public boolean withdraw(String name, double amount) {
      if (this.econ.getMoney(name) >= amount) {
         this.econ.subFunds(name, amount);
         return true;
      } else {
         return false;
      }
   }

   public boolean transfer(String from, String to, double amount) {
      if (this.econ.getMoney(from) >= amount) {
         this.econ.subFunds(from, amount);
         this.econ.addFunds(to, amount);
         return true;
      } else {
         return false;
      }
   }

   public double getBalance(String name) {
      return this.econ.getMoney(name);
   }

   public String format(double balance) {
      return this.econ.formatValue(balance, false);
   }
}
