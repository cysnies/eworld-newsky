package net.milkbowl.vault.economy.plugins;

import is.currency.Currency;
import is.currency.syst.AccountContext;
import java.util.List;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_CurrencyCore implements Economy {
   private Currency currency;
   private static final Logger log = Logger.getLogger("Minecraft");
   private final Plugin plugin;
   private final String name = "CurrencyCore";

   public Economy_CurrencyCore(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.currency == null) {
         Plugin currencyPlugin = plugin.getServer().getPluginManager().getPlugin("CurrencyCore");
         if (currencyPlugin != null && currencyPlugin.getClass().getName().equals("is.currency.Currency")) {
            this.currency = (Currency)currencyPlugin;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "CurrencyCore"));
         }
      }

   }

   public boolean isEnabled() {
      return this.currency != null;
   }

   public String getName() {
      return "CurrencyCore";
   }

   public String format(double amount) {
      return this.currency.getFormatHelper().format(amount);
   }

   public String currencyNamePlural() {
      return (String)this.currency.getCurrencyConfig().getCurrencyMajor().get(1);
   }

   public String currencyNameSingular() {
      return (String)this.currency.getCurrencyConfig().getCurrencyMajor().get(0);
   }

   public double getBalance(String playerName) {
      AccountContext account = this.currency.getAccountManager().getAccount(playerName);
      return account == null ? (double)0.0F : account.getBalance();
   }

   public boolean has(String playerName, double amount) {
      AccountContext account = this.currency.getAccountManager().getAccount(playerName);
      return account == null ? false : account.hasBalance(amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         AccountContext account = this.currency.getAccountManager().getAccount(playerName);
         if (account == null) {
            return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That account does not exist");
         } else if (!account.hasBalance(amount)) {
            return new EconomyResponse((double)0.0F, account.getBalance(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
         } else {
            account.subtractBalance(amount);
            return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         AccountContext account = this.currency.getAccountManager().getAccount(playerName);
         if (account == null) {
            return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That account does not exist");
         } else {
            account.addBalance(amount);
            return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
         }
      }
   }

   public EconomyResponse createBank(String name, String player) {
      if (this.currency.getAccountManager().hasAccount(name)) {
         return new EconomyResponse((double)0.0F, this.currency.getAccountManager().getAccount(name).getBalance(), EconomyResponse.ResponseType.FAILURE, "That account already exists.");
      } else {
         this.currency.getAccountManager().createAccount(name);
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse deleteBank(String name) {
      if (this.currency.getAccountManager().hasAccount(name)) {
         this.currency.getAccountManager().deleteAccount(name);
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "");
      } else {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That account does not exist!");
      }
   }

   public EconomyResponse bankBalance(String name) {
      AccountContext account = this.currency.getAccountManager().getAccount(name);
      return account == null ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That account does not exists.") : new EconomyResponse((double)0.0F, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
   }

   public EconomyResponse bankHas(String name, double amount) {
      AccountContext account = this.currency.getAccountManager().getAccount(name);
      if (account == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That account does not exist!");
      } else {
         return !account.hasBalance(amount) ? new EconomyResponse((double)0.0F, account.getBalance(), EconomyResponse.ResponseType.FAILURE, "That account does not have enough!") : new EconomyResponse((double)0.0F, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         AccountContext account = this.currency.getAccountManager().getAccount(name);
         if (account == null) {
            return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That account does not exist!");
         } else if (!account.hasBalance(amount)) {
            return new EconomyResponse((double)0.0F, account.getBalance(), EconomyResponse.ResponseType.FAILURE, "That account does not have enough!");
         } else {
            account.subtractBalance(amount);
            return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
         }
      }
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         AccountContext account = this.currency.getAccountManager().getAccount(name);
         if (account == null) {
            return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That account does not exist!");
         } else {
            account.addBalance(amount);
            return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "");
         }
      }
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Currency does not support Bank members.");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Currency does not support Bank members.");
   }

   public List getBanks() {
      return this.currency.getAccountManager().getAccountList();
   }

   public boolean hasBankSupport() {
      return true;
   }

   public boolean hasAccount(String playerName) {
      return this.currency.getAccountManager().getAccount(playerName) != null;
   }

   public boolean createPlayerAccount(String playerName) {
      if (this.currency.getAccountManager().getAccount(playerName) != null) {
         return false;
      } else {
         this.currency.getAccountManager().createAccount(playerName);
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
      private Economy_CurrencyCore economy = null;

      public EconomyServerListener(Economy_CurrencyCore economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.currency == null) {
            Plugin currencyPlugin = Economy_CurrencyCore.this.plugin.getServer().getPluginManager().getPlugin("CurrencyCore");
            if (currencyPlugin != null && currencyPlugin.getClass().getName().equals("is.currency.Currency")) {
               this.economy.currency = (Currency)currencyPlugin;
               Economy_CurrencyCore.log.info(String.format("[%s][Economy] %s hooked.", Economy_CurrencyCore.this.plugin.getDescription().getName(), this.economy.getName()));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.currency != null && event.getPlugin().getDescription().getName().equals("CurrencyCore")) {
            this.economy.currency = null;
            Economy_CurrencyCore.log.info(String.format("[%s][Economy] %s unhooked.", Economy_CurrencyCore.this.plugin.getDescription().getName(), this.economy.getName()));
         }

      }
   }
}
