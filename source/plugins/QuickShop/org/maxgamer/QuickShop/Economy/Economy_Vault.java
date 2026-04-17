package org.maxgamer.QuickShop.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class Economy_Vault implements EconomyCore {
   private net.milkbowl.vault.economy.Economy vault;

   public Economy_Vault() {
      this.setupEconomy();
   }

   private boolean setupEconomy() {
      RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> economyProvider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
      if (economyProvider != null) {
         this.vault = (net.milkbowl.vault.economy.Economy)economyProvider.getProvider();
      }

      return this.vault != null;
   }

   public boolean isValid() {
      return this.vault != null;
   }

   public boolean deposit(String name, double amount) {
      return this.vault.depositPlayer(name, amount).transactionSuccess();
   }

   public boolean withdraw(String name, double amount) {
      return this.vault.withdrawPlayer(name, amount).transactionSuccess();
   }

   public boolean transfer(String from, String to, double amount) {
      if (this.vault.getBalance(from) >= amount) {
         if (this.vault.withdrawPlayer(from, amount).transactionSuccess()) {
            if (!this.vault.depositPlayer(to, amount).transactionSuccess()) {
               this.vault.depositPlayer(from, amount);
               return false;
            } else {
               return true;
            }
         } else {
            return false;
         }
      } else {
         return false;
      }
   }

   public double getBalance(String name) {
      return this.vault.getBalance(name);
   }

   public String format(double balance) {
      try {
         return this.vault.format(balance);
      } catch (NumberFormatException var4) {
         return "$" + balance;
      }
   }
}
