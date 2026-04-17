package net.milkbowl.vault.economy.plugins;

import boardinggamer.mcmoney.McMoneyAPI;
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

public class Economy_McMoney implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "McMoney";
   private Plugin plugin = null;
   private McMoneyAPI economy = null;

   public Economy_McMoney(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin econ = plugin.getServer().getPluginManager().getPlugin("McMoney");
         if (econ != null && econ.isEnabled()) {
            this.economy = McMoneyAPI.getInstance();
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "McMoney"));
         }
      }

   }

   public String getName() {
      return "McMoney";
   }

   public boolean isEnabled() {
      return this.economy != null;
   }

   public double getBalance(String playerName) {
      return this.economy.getMoney(playerName);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      double balance = this.economy.getMoney(playerName);
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else if (balance - amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
      } else {
         this.economy.removeMoney(playerName, amount);
         return new EconomyResponse(amount, this.economy.getMoney(playerName), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      double balance = this.economy.getMoney(playerName);
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
      } else {
         this.economy.addMoney(playerName, amount);
         return new EconomyResponse(amount, this.economy.getMoney(playerName), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public String currencyNamePlural() {
      return this.economy.moneyNamePlural();
   }

   public String currencyNameSingular() {
      return this.economy.moneyNameSingle();
   }

   public String format(double amount) {
      amount = Math.ceil(amount);
      return amount == (double)1.0F ? String.format("%d %s", (int)amount, this.currencyNameSingular()) : String.format("%d %s", (int)amount, this.currencyNamePlural());
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "McMoney does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return this.economy.playerExists(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      if (!this.hasAccount(playerName)) {
         this.economy.setMoney(playerName, (double)0.0F);
         return true;
      } else {
         return false;
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
      Economy_McMoney economy = null;

      public EconomyServerListener(Economy_McMoney economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin eco = Economy_McMoney.this.plugin.getServer().getPluginManager().getPlugin("McMoney");
            if (eco != null) {
               this.economy.economy = McMoneyAPI.getInstance();
               Economy_McMoney.log.info(String.format("[%s][Economy] %s hooked.", Economy_McMoney.this.plugin.getDescription().getName(), "McMoney"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("McMoney")) {
            this.economy.economy = null;
            Economy_McMoney.log.info(String.format("[%s][Economy] %s unhooked.", Economy_McMoney.this.plugin.getDescription().getName(), "McMoney"));
         }

      }
   }
}
