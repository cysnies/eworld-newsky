package net.milkbowl.vault.economy.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import me.ethan.eWallet.ECO;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_eWallet implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "eWallet";
   private Plugin plugin = null;
   private ECO econ = null;

   public Economy_eWallet(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.econ == null) {
         Plugin econ = plugin.getServer().getPluginManager().getPlugin("eWallet");
         if (econ != null && econ.isEnabled()) {
            this.econ = (ECO)econ;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "eWallet"));
         }
      }

   }

   public boolean isEnabled() {
      return this.econ != null;
   }

   public String getName() {
      return "eWallet";
   }

   public String format(double amount) {
      amount = Math.ceil(amount);
      return amount == (double)1.0F ? String.format("%d %s", (int)amount, this.econ.singularCurrency) : String.format("%d %s", (int)amount, this.econ.pluralCurrency);
   }

   public String currencyNameSingular() {
      return this.econ.singularCurrency;
   }

   public String currencyNamePlural() {
      return this.econ.pluralCurrency;
   }

   public double getBalance(String playerName) {
      Integer i = this.econ.getMoney(playerName);
      return i == null ? (double)0.0F : (double)i;
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= Math.ceil(amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      double balance = this.getBalance(playerName);
      amount = Math.ceil(amount);
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else if (balance >= amount) {
         double finalBalance = balance - amount;
         this.econ.takeMoney(playerName, (int)amount);
         return new EconomyResponse(amount, finalBalance, EconomyResponse.ResponseType.SUCCESS, (String)null);
      } else {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      double balance = this.getBalance(playerName);
      amount = Math.ceil(amount);
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
      } else {
         balance += amount;
         this.econ.giveMoney(playerName, (int)amount);
         return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "eWallet does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return this.econ.hasAccount(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      if (this.hasAccount(playerName)) {
         return false;
      } else {
         this.econ.createAccount(playerName, 0);
         return true;
      }
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
      Economy_eWallet economy = null;

      public EconomyServerListener(Economy_eWallet economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.econ == null) {
            Plugin eco = Economy_eWallet.this.plugin.getServer().getPluginManager().getPlugin("eWallet");
            if (eco != null) {
               this.economy.econ = (ECO)eco;
               Economy_eWallet.log.info(String.format("[%s][Economy] %s hooked.", Economy_eWallet.this.plugin.getDescription().getName(), "eWallet"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.econ != null && event.getPlugin().getDescription().getName().equals("eWallet")) {
            this.economy.econ = null;
            Economy_eWallet.log.info(String.format("[%s][Economy] %s unhooked.", Economy_eWallet.this.plugin.getDescription().getName(), "eWallet"));
         }

      }
   }
}
