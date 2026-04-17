package net.milkbowl.vault.economy.plugins;

import java.util.ArrayList;
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
import org.gestern.gringotts.Account;
import org.gestern.gringotts.AccountHolder;
import org.gestern.gringotts.Configuration;
import org.gestern.gringotts.Gringotts;

public class Economy_Gringotts implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "Gringotts";
   private Plugin plugin = null;
   private Gringotts gringotts = null;

   public Economy_Gringotts(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.gringotts == null) {
         Plugin grngts = plugin.getServer().getPluginManager().getPlugin("Gringotts");
         if (grngts != null && grngts.isEnabled()) {
            this.gringotts = (Gringotts)grngts;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "Gringotts"));
         }
      }

   }

   public boolean isEnabled() {
      return this.gringotts != null && this.gringotts.isEnabled();
   }

   public String getName() {
      return "Gringotts";
   }

   public boolean hasBankSupport() {
      return false;
   }

   public int fractionalDigits() {
      return 2;
   }

   public String format(double amount) {
      return Double.toString(amount);
   }

   public String currencyNamePlural() {
      return Configuration.config.currencyNamePlural;
   }

   public String currencyNameSingular() {
      return Configuration.config.currencyNameSingular;
   }

   public boolean hasAccount(String playerName) {
      AccountHolder owner = this.gringotts.accountHolderFactory.getAccount(playerName);
      if (owner == null) {
         return false;
      } else {
         return this.gringotts.accounting.getAccount(owner) != null;
      }
   }

   public double getBalance(String playerName) {
      AccountHolder owner = this.gringotts.accountHolderFactory.getAccount(playerName);
      if (owner == null) {
         return (double)0.0F;
      } else {
         Account account = this.gringotts.accounting.getAccount(owner);
         return account.balance();
      }
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw a negative amount.");
      } else {
         AccountHolder accountHolder = this.gringotts.accountHolderFactory.getAccount(playerName);
         if (accountHolder == null) {
            return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, playerName + " is not a valid account holder.");
         } else {
            Account account = this.gringotts.accounting.getAccount(accountHolder);
            return account.balance() >= amount && account.remove(amount) ? new EconomyResponse(amount, account.balance(), EconomyResponse.ResponseType.SUCCESS, (String)null) : new EconomyResponse((double)0.0F, account.balance(), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         AccountHolder accountHolder = this.gringotts.accountHolderFactory.getAccount(playerName);
         if (accountHolder == null) {
            return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, playerName + " is not a valid account holder.");
         } else {
            Account account = this.gringotts.accounting.getAccount(accountHolder);
            return account.add(amount) ? new EconomyResponse(amount, account.balance(), EconomyResponse.ResponseType.SUCCESS, (String)null) : new EconomyResponse((double)0.0F, account.balance(), EconomyResponse.ResponseType.FAILURE, "Not enough capacity to store that amount!");
         }
      }
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Gringotts does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean createPlayerAccount(String playerName) {
      return this.hasAccount(playerName);
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
      Economy_Gringotts economy = null;

      public EconomyServerListener(Economy_Gringotts economy_Gringotts) {
         this.economy = economy_Gringotts;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.gringotts == null) {
            Plugin grngts = Economy_Gringotts.this.plugin.getServer().getPluginManager().getPlugin("Gringotts");
            if (grngts != null) {
               this.economy.gringotts = (Gringotts)grngts;
               Economy_Gringotts.log.info(String.format("[%s][Economy] %s hooked.", Economy_Gringotts.this.plugin.getDescription().getName(), "Gringotts"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.gringotts != null && event.getPlugin().getDescription().getName().equals("Gringotts")) {
            this.economy.gringotts = null;
            Economy_Gringotts.log.info(String.format("[%s][Economy] %s unhooked.", Economy_Gringotts.this.plugin.getDescription().getName(), "Gringotts"));
         }

      }
   }
}
