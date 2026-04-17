package net.milkbowl.vault.economy.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import me.ashtheking.currency.Currency;
import me.ashtheking.currency.CurrencyList;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_MultiCurrency implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "MultiCurrency";
   private Plugin plugin = null;
   private Currency economy = null;

   public Economy_MultiCurrency(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin multiCurrency = plugin.getServer().getPluginManager().getPlugin("MultiCurrency");
         if (multiCurrency != null && multiCurrency.isEnabled()) {
            this.economy = (Currency)multiCurrency;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "MultiCurrency"));
         }
      }

   }

   public String getName() {
      return "MultiCurrency";
   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public double getBalance(String playerName) {
      double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
      return balance;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      String errorMessage = null;
      if (amount < (double)0.0F) {
         errorMessage = "Cannot withdraw negative funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else if (!CurrencyList.hasEnough(playerName, amount)) {
         errorMessage = "Insufficient funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else if (CurrencyList.subtract(playerName, amount)) {
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
         double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else {
         errorMessage = "Error withdrawing funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      String errorMessage = null;
      if (amount < (double)0.0F) {
         errorMessage = "Cannot deposit negative funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else if (CurrencyList.add(playerName, amount)) {
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
         double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else {
         errorMessage = "Error withdrawing funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = CurrencyList.getValue((String)CurrencyList.maxCurrency(playerName)[0], playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      }
   }

   public String format(double amount) {
      return String.format("%.2f %s", amount, "currency");
   }

   public String currencyNameSingular() {
      return "currency";
   }

   public String currencyNamePlural() {
      return "currency";
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MultiCurrency does not support bank accounts");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return true;
   }

   public boolean createPlayerAccount(String playerName) {
      return false;
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
      Economy_MultiCurrency economy = null;

      public EconomyServerListener(Economy_MultiCurrency economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin mcur = Economy_MultiCurrency.this.plugin.getServer().getPluginManager().getPlugin("MultiCurrency");
            if (mcur != null) {
               this.economy.economy = (Currency)mcur;
               Economy_MultiCurrency.log.info(String.format("[%s][Economy] %s hooked.", Economy_MultiCurrency.this.plugin.getDescription().getName(), "MultiCurrency"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("MultiCurrency")) {
            this.economy.economy = null;
            Economy_MultiCurrency.log.info(String.format("[%s][Economy] %s unhooked.", Economy_MultiCurrency.this.plugin.getDescription().getName(), "MultiCurrency"));
         }

      }
   }
}
