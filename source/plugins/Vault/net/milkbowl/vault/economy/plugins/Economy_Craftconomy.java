package net.milkbowl.vault.economy.plugins;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import me.greatman.Craftconomy.Account;
import me.greatman.Craftconomy.AccountHandler;
import me.greatman.Craftconomy.Bank;
import me.greatman.Craftconomy.BankHandler;
import me.greatman.Craftconomy.Craftconomy;
import me.greatman.Craftconomy.CurrencyHandler;
import me.greatman.Craftconomy.utils.Config;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_Craftconomy implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "Craftconomy";
   private Plugin plugin = null;
   protected Craftconomy economy = null;

   public Economy_Craftconomy(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin ec = plugin.getServer().getPluginManager().getPlugin("Craftconomy");
         if (ec != null && ec.getClass().getName().equals("me.greatman.Craftconomy.Craftconomy")) {
            this.economy = (Craftconomy)ec;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "Craftconomy"));
         }
      }

   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public String getName() {
      return "Craftconomy";
   }

   public String format(double amount) {
      return Craftconomy.format(amount, CurrencyHandler.getCurrency(Config.currencyDefault, true));
   }

   public String currencyNameSingular() {
      return CurrencyHandler.getCurrency(Config.currencyDefault, true).getName();
   }

   public String currencyNamePlural() {
      return CurrencyHandler.getCurrency(Config.currencyDefault, true).getNamePlural();
   }

   public double getBalance(String playerName) {
      return AccountHandler.exists(playerName) ? AccountHandler.getAccount(playerName).getDefaultBalance() : (double)0.0F;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         Account account = AccountHandler.getAccount(playerName);
         if (account.hasEnough(amount)) {
            double balance = account.substractMoney(amount);
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, "");
         } else {
            return new EconomyResponse((double)0.0F, account.getDefaultBalance(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         Account account = AccountHandler.getAccount(playerName);
         account.addMoney(amount);
         return new EconomyResponse(amount, account.getDefaultBalance(), EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse createBank(String name, String player) {
      boolean success = BankHandler.create(name, player);
      return success ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to create that bank account.");
   }

   public EconomyResponse deleteBank(String name) {
      boolean success = BankHandler.delete(name);
      return success ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to delete that bank account.");
   }

   public EconomyResponse bankHas(String name, double amount) {
      if (BankHandler.exists(name)) {
         Bank bank = BankHandler.getBank(name);
         return bank.hasEnough(amount) ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, bank.getDefaultBalance(), EconomyResponse.ResponseType.FAILURE, "The bank does not have enough money!");
      } else {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      }
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         EconomyResponse er = this.bankHas(name, amount);
         if (!er.transactionSuccess()) {
            return er;
         } else if (BankHandler.exists(name)) {
            Bank bank = BankHandler.getBank(name);
            double balance = bank.substractMoney(amount);
            return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.SUCCESS, "");
         } else {
            return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
         }
      }
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else if (BankHandler.exists(name)) {
         Bank bank = BankHandler.getBank(name);
         double balance = bank.addMoney(amount);
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.SUCCESS, "");
      } else {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      }
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      if (BankHandler.exists(name)) {
         Bank bank = BankHandler.getBank(name);
         return bank.getOwner().equals(playerName) ? new EconomyResponse((double)0.0F, bank.getDefaultBalance(), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "This player is not the owner of the bank!");
      } else {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      }
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      EconomyResponse er = this.isBankOwner(name, playerName);
      if (er.transactionSuccess()) {
         return er;
      } else {
         if (BankHandler.exists(name)) {
            Bank bank = BankHandler.getBank(name);
            Iterator<String> iterator = bank.getMembers().iterator();

            while(iterator.hasNext()) {
               if (((String)iterator.next()).equals(playerName)) {
                  return new EconomyResponse((double)0.0F, bank.getDefaultBalance(), EconomyResponse.ResponseType.SUCCESS, "");
               }
            }
         }

         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "This player is not a member of the bank!");
      }
   }

   public EconomyResponse bankBalance(String name) {
      return BankHandler.exists(name) ? new EconomyResponse((double)0.0F, BankHandler.getBank(name).getDefaultBalance(), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
   }

   public List getBanks() {
      return BankHandler.listBanks();
   }

   public boolean hasBankSupport() {
      return true;
   }

   public boolean hasAccount(String playerName) {
      return AccountHandler.exists(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      if (AccountHandler.exists(playerName)) {
         return false;
      } else {
         AccountHandler.getAccount(playerName);
         return true;
      }
   }

   public int fractionalDigits() {
      return -1;
   }

   public boolean hasAccount(String playerName, String worldName) {
      return this.hasAccount(playerName);
   }

   public double getBalance(String playerName, String world) {
      return this.getBalance(playerName);
   }

   public boolean has(String playerName, String worldName, double amount) {
      return this.has(playerName, amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
      return this.withdrawPlayer(playerName, amount);
   }

   public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
      return this.depositPlayer(playerName, amount);
   }

   public boolean createPlayerAccount(String playerName, String worldName) {
      return this.createPlayerAccount(playerName);
   }

   public class EconomyServerListener implements Listener {
      Economy_Craftconomy economy = null;

      public EconomyServerListener(Economy_Craftconomy economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin ec = Economy_Craftconomy.this.plugin.getServer().getPluginManager().getPlugin("Craftconomy");
            if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("me.greatman.Craftconomy.Craftconomy")) {
               this.economy.economy = (Craftconomy)ec;
               Economy_Craftconomy.log.info(String.format("[%s][Economy] %s hooked.", Economy_Craftconomy.this.plugin.getDescription().getName(), "Craftconomy"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("Craftconomy")) {
            this.economy.economy = null;
            Economy_Craftconomy.log.info(String.format("[%s][Economy] %s unhooked.", Economy_Craftconomy.this.plugin.getDescription().getName(), "Craftconomy"));
         }

      }
   }
}
