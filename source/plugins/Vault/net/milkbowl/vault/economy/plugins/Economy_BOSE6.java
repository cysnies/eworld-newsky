package net.milkbowl.vault.economy.plugins;

import cosine.boseconomy.BOSEconomy;
import java.util.List;
import java.util.logging.Level;
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

public class Economy_BOSE6 implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "BOSEconomy";
   private Plugin plugin = null;
   private BOSEconomy economy = null;

   public Economy_BOSE6(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      log.log(Level.SEVERE, "BOSEconomy6 is an extremely outdated plugin and can not be used reliably for economy! You should update the more recent and maintained BOSEconomy7 for compatibility!");
      if (this.economy == null) {
         Plugin bose = plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
         if (bose != null && bose.isEnabled() && bose.getDescription().getVersion().startsWith("0.6")) {
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
      return (double)this.economy.getPlayerMoney(playerName);
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      String errorMessage = null;
      if (amount < (double)0.0F) {
         errorMessage = "Cannot withdraw negative funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = (double)this.economy.getPlayerMoney(playerName);
         return new EconomyResponse(balance, balance, type, errorMessage);
      } else {
         amount = Math.ceil(amount);
         double balance = (double)this.economy.getPlayerMoney(playerName);
         if (balance - amount < (double)0.0F) {
            errorMessage = "Insufficient funds";
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
            amount = (double)0.0F;
            return new EconomyResponse(balance, balance, type, errorMessage);
         } else if (this.economy.setPlayerMoney(playerName, (int)(balance - amount), false)) {
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
            balance = (double)this.economy.getPlayerMoney(playerName);
            return new EconomyResponse(amount, balance, type, errorMessage);
         } else {
            errorMessage = "Error withdrawing funds";
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
            amount = (double)0.0F;
            balance = (double)this.economy.getPlayerMoney(playerName);
            return new EconomyResponse(amount, balance, type, errorMessage);
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      String errorMessage = null;
      if (amount < (double)0.0F) {
         errorMessage = "Cannot deposit negative funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = (double)this.economy.getPlayerMoney(playerName);
         return new EconomyResponse(balance, balance, type, errorMessage);
      } else {
         amount = Math.ceil(amount);
         double balance = (double)this.economy.getPlayerMoney(playerName);
         if (this.economy.setPlayerMoney(playerName, (int)(balance + amount), false)) {
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
            balance = (double)this.economy.getPlayerMoney(playerName);
            return new EconomyResponse(amount, balance, type, errorMessage);
         } else {
            errorMessage = "Error withdrawing funds";
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
            amount = (double)0.0F;
            balance = (double)this.economy.getPlayerMoney(playerName);
            return new EconomyResponse(balance, balance, type, errorMessage);
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
      return amount == (double)1.0F ? String.format("%.0f %s", amount, this.currencyNameSingular()) : String.format("%.2f %s", amount, this.currencyNamePlural());
   }

   public EconomyResponse createBank(String name, String player) {
      boolean success = this.economy.addBankOwner(name, player, false);
      return success ? new EconomyResponse((double)0.0F, (double)this.economy.getBankMoney(name), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to create that bank account.");
   }

   public EconomyResponse deleteBank(String name) {
      boolean success = this.economy.removeBank(name);
      return success ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to remove that bank account.");
   }

   public EconomyResponse bankHas(String name, double amount) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         double bankMoney = (double)this.economy.getBankMoney(name);
         return bankMoney < amount ? new EconomyResponse((double)0.0F, bankMoney, EconomyResponse.ResponseType.FAILURE, "The bank does not have enough money!") : new EconomyResponse((double)0.0F, bankMoney, EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      EconomyResponse er = this.bankHas(name, amount);
      if (!er.transactionSuccess()) {
         return er;
      } else {
         this.economy.addBankMoney(name, (int)(-amount), true);
         return new EconomyResponse((double)((int)amount), (double)this.economy.getBankMoney(name), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse(amount, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         this.economy.addBankMoney(name, (int)amount, true);
         return new EconomyResponse((double)((int)amount), (double)this.economy.getBankMoney(name), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         return this.economy.isBankOwner(name, playerName) ? new EconomyResponse((double)0.0F, (double)this.economy.getBankMoney(name), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That player is not a bank owner!");
      }
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         return this.economy.isBankMember(name, playerName) ? new EconomyResponse((double)0.0F, (double)this.economy.getBankMoney(name), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That player is not a bank member!");
      }
   }

   public EconomyResponse bankBalance(String name) {
      if (!this.economy.bankExists(name)) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      } else {
         double bankMoney = (double)this.economy.getBankMoney(name);
         return new EconomyResponse((double)0.0F, bankMoney, EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public List getBanks() {
      return this.economy.getBankList();
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
      return 0;
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
      Economy_BOSE6 economy = null;

      public EconomyServerListener(Economy_BOSE6 economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin bose = Economy_BOSE6.this.plugin.getServer().getPluginManager().getPlugin("BOSEconomy");
            if (bose != null && bose.getDescription().getVersion().startsWith("0.6")) {
               this.economy.economy = (BOSEconomy)bose;
               Economy_BOSE6.log.info(String.format("[%s][Economy] %s hooked.", Economy_BOSE6.this.plugin.getDescription().getName(), "BOSEconomy"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("BOSEconomy") && event.getPlugin().getDescription().getVersion().startsWith("0.6")) {
            this.economy.economy = null;
            Economy_BOSE6.log.info(String.format("[%s][Economy] %s unhooked.", Economy_BOSE6.this.plugin.getDescription().getName(), "BOSEconomy"));
         }

      }
   }
}
