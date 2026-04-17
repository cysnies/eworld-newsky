package net.milkbowl.vault.economy.plugins;

import cosine.boseconomy.BOSEconomy;
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

public class Economy_BOSE7 implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "BOSEconomy";
   private Plugin plugin = null;
   private BOSEconomy economy = null;

   public Economy_BOSE7(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin bose = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
         if (bose != null && bose.isEnabled() && bose.getDescription().getVersion().startsWith("0.7")) {
            this.economy = (BOSEconomy)bose;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "BOSEconomy"));
         }
      }

   }

   public String getName() {
      return "BOSEconomy";
   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public double getBalance(String playerName) {
      double balance = this.economy.getPlayerMoneyDouble(playerName);
      return balance;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.economy.getPlayerMoneyDouble(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else if (!this.has(playerName, amount)) {
         return new EconomyResponse((double)0.0F, this.economy.getPlayerMoneyDouble(playerName), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
      } else {
         double balance = this.economy.getPlayerMoneyDouble(playerName);
         if (this.economy.setPlayerMoney(playerName, balance - amount, false)) {
            balance = this.economy.getPlayerMoneyDouble(playerName);
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, "");
         } else {
            return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Error withdrawing funds");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.economy.getPlayerMoneyDouble(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
      } else {
         double balance = this.economy.getPlayerMoneyDouble(playerName);
         if (this.economy.setPlayerMoney(playerName, balance + amount, false)) {
            balance = this.economy.getPlayerMoneyDouble(playerName);
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, "");
         } else {
            return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Error depositing funds");
         }
      }
   }

   public String currencyNamePlural() {
      return this.economy.getMoneyNamePlural();
   }

   public String currencyNameSingular() {
      return this.economy.getMoneyName();
   }

   public String format(double amount) {
      return this.economy.getMoneyFormatted(amount);
   }

   public EconomyResponse createBank(String name, String player) {
      boolean success = this.economy.addBankOwner(name, player, false);
      return success ? new EconomyResponse((double)0.0F, this.economy.getBankMoneyDouble(name), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to create that bank account.");
   }

   public EconomyResponse deleteBank(String name) {
      boolean success = this.economy.removeBank(name);
      return success ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to remove that bank account.");
   }

   public EconomyResponse bankHas(String name, double amount) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         double bankMoney = this.economy.getBankMoneyDouble(name);
         return bankMoney < amount ? new EconomyResponse((double)0.0F, bankMoney, EconomyResponse.ResponseType.FAILURE, "The bank does not have enough money!") : new EconomyResponse((double)0.0F, bankMoney, EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      EconomyResponse er = this.bankHas(name, amount);
      if (!er.transactionSuccess()) {
         return er;
      } else {
         this.economy.addBankMoney(name, -amount, true);
         return new EconomyResponse(amount, this.economy.getBankMoneyDouble(name), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse(amount, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         this.economy.addBankMoney(name, amount, true);
         return new EconomyResponse(amount, this.economy.getBankMoneyDouble(name), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         return this.economy.isBankOwner(name, playerName) ? new EconomyResponse((double)0.0F, this.economy.getBankMoneyDouble(name), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That player is not a bank owner!");
      }
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         return this.economy.isBankMember(name, playerName) ? new EconomyResponse((double)0.0F, this.economy.getBankMoneyDouble(name), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That player is not a bank member!");
      }
   }

   public EconomyResponse bankBalance(String name) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         double bankMoney = this.economy.getBankMoneyDouble(name);
         return new EconomyResponse((double)0.0F, bankMoney, EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public List getBanks() {
      return this.economy.getBankList();
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public boolean hasBankSupport() {
      return true;
   }

   public boolean hasAccount(String playerName) {
      return this.economy.playerRegistered(playerName, false);
   }

   public boolean createPlayerAccount(String playerName) {
      return this.economy.playerRegistered(playerName, false) ? false : this.economy.registerPlayer(playerName);
   }

   public int fractionalDigits() {
      return this.economy.getFractionalDigits();
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
      Economy_BOSE7 economy = null;

      public EconomyServerListener(Economy_BOSE7 economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin bose = Economy_BOSE7.this.plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
            if (bose != null && bose.getDescription().getVersion().startsWith("0.7")) {
               this.economy.economy = (BOSEconomy)bose;
               Economy_BOSE7.log.info(String.format("[%s][Economy] %s hooked.", Economy_BOSE7.this.plugin.getDescription().getName(), "BOSEconomy"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("BOSEconomy") && event.getPlugin().getDescription().getVersion().startsWith("0.7")) {
            this.economy.economy = null;
            Economy_BOSE7.log.info(String.format("[%s][Economy] %s unhooked.", Economy_BOSE7.this.plugin.getDescription().getName(), "BOSEconomy"));
         }

      }
   }
}
