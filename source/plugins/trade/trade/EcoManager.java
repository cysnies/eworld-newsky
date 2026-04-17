package trade;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EcoManager {
   private static Economy eco;

   public EcoManager(Main main) {
      this.setupEconomy();
   }

   public int getGold(String name) {
      return (int)eco.getBalance(name);
   }

   public boolean setGold(String name, double amount) {
      if (amount < (double)0.0F) {
         return false;
      } else if (!eco.hasAccount(name)) {
         return false;
      } else {
         double num = eco.getBalance(name);
         double diff = amount - num;
         if (diff == (double)0.0F) {
            return true;
         } else {
            return diff > (double)0.0F ? eco.depositPlayer(name, diff).transactionSuccess() : eco.withdrawPlayer(name, -diff).transactionSuccess();
         }
      }
   }

   public void addGold(String name, int amount) {
      if (amount > 0) {
         this.setGold(name, (double)(this.getGold(name) + amount));
      }

   }

   private void setupEconomy() {
      RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
      eco = (Economy)rsp.getProvider();
   }
}
