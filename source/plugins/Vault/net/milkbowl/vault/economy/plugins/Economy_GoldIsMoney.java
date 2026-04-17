package net.milkbowl.vault.economy.plugins;

import com.flobi.GoldIsMoney.GoldIsMoney;
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

public class Economy_GoldIsMoney implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "GoldIsMoney";
   private Plugin plugin = null;
   protected GoldIsMoney economy = null;

   public Economy_GoldIsMoney(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin ec = plugin.getServer().getPluginManager().getPlugin("GoldIsMoney");
         if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("com.flobi.GoldIsMoney.GoldIsMoney")) {
            this.economy = (GoldIsMoney)ec;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "GoldIsMoney"));
         }
      }

   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public String getName() {
      return "GoldIsMoney";
   }

   private double getAccountBalance(String playerName) {
      return (double)GoldIsMoney.getBalance(playerName);
   }

   public double getBalance(String playerName) {
      return this.getAccountBalance(playerName);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else if (GoldIsMoney.has(playerName, Math.round(amount))) {
         GoldIsMoney.withdrawPlayer(playerName, (long)amount);
         return new EconomyResponse(amount, this.getAccountBalance(playerName), EconomyResponse.ResponseType.SUCCESS, (String)null);
      } else {
         return new EconomyResponse((double)0.0F, this.getAccountBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         GoldIsMoney.depositPlayer(playerName, Math.round(amount));
         return new EconomyResponse(amount, (double)GoldIsMoney.getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public String format(double amount) {
      return GoldIsMoney.format(Math.round(amount));
   }

   public String currencyNameSingular() {
      return GoldIsMoney.currencyNameSingular();
   }

   public String currencyNamePlural() {
      return GoldIsMoney.currencyNamePlural();
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support single account banks!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support single bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support single bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support single bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support single bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support single bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GoldIsMoney does not support single bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return GoldIsMoney.hasAccount(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      return this.hasAccount(playerName);
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
      Economy_GoldIsMoney economy = null;

      public EconomyServerListener(Economy_GoldIsMoney economy_GoldIsMoney) {
         this.economy = economy_GoldIsMoney;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin ec = Economy_GoldIsMoney.this.plugin.getServer().getPluginManager().getPlugin("GoldIsMoney");
            if (ec != null && ec.getClass().getName().equals("com.flobi.GoldIsMoney.GoldIsMoney")) {
               this.economy.economy = (GoldIsMoney)ec;
               Economy_GoldIsMoney.log.info(String.format("[%s][Economy] %s hooked.", Economy_GoldIsMoney.this.plugin.getDescription().getName(), "GoldIsMoney"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("GoldIsMoney")) {
            this.economy.economy = null;
            Economy_GoldIsMoney.log.info(String.format("[%s][Economy] %s unhooked.", Economy_GoldIsMoney.this.plugin.getDescription().getName(), "GoldIsMoney"));
         }

      }
   }
}
