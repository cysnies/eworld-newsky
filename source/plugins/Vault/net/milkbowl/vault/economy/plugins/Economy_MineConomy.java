package net.milkbowl.vault.economy.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import me.mjolnir.mineconomy.MineConomy;
import me.mjolnir.mineconomy.exceptions.AccountNameConflictException;
import me.mjolnir.mineconomy.exceptions.NoAccountException;
import me.mjolnir.mineconomy.internal.MCCom;
import me.mjolnir.mineconomy.internal.util.MCFormat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_MineConomy implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "MineConomy";
   private Plugin plugin = null;
   private MineConomy econ = null;

   public Economy_MineConomy(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.econ == null) {
         Plugin econ = plugin.getServer().getPluginManager().getPlugin("MineConomy");
         if (econ != null && econ.isEnabled()) {
            this.econ = (MineConomy)econ;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "MineConomy"));
         }
      }

   }

   public boolean isEnabled() {
      return this.econ != null;
   }

   public String getName() {
      return "MineConomy";
   }

   public String format(double amount) {
      return MCFormat.format(amount);
   }

   public String currencyNameSingular() {
      return MCCom.getDefaultCurrency();
   }

   public String currencyNamePlural() {
      return MCCom.getDefaultCurrency();
   }

   public double getBalance(String playerName) {
      try {
         return MCCom.getExternalBalance(playerName);
      } catch (NoAccountException var3) {
         MCCom.create(playerName);
         return MCCom.getExternalBalance(playerName);
      }
   }

   public boolean has(String playerName, double amount) {
      try {
         return MCCom.canExternalAfford(playerName, amount);
      } catch (NoAccountException var5) {
         MCCom.create(playerName);
         return MCCom.canExternalAfford(playerName, amount);
      }
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      double balance;
      try {
         balance = MCCom.getExternalBalance(playerName);
      } catch (NoAccountException var8) {
         MCCom.create(playerName);
         balance = MCCom.getExternalBalance(playerName);
      }

      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else if (balance >= amount) {
         double finalBalance = balance - amount;
         MCCom.setExternalBalance(playerName, finalBalance);
         return new EconomyResponse(amount, finalBalance, EconomyResponse.ResponseType.SUCCESS, (String)null);
      } else {
         return new EconomyResponse((double)0.0F, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      double balance;
      try {
         balance = MCCom.getExternalBalance(playerName);
      } catch (NoAccountException var7) {
         MCCom.create(playerName);
         balance = MCCom.getExternalBalance(playerName);
      }

      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
      } else {
         balance += amount;
         MCCom.setExternalBalance(playerName, balance);
         return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "MineConomy does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return MCCom.exists(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      try {
         MCCom.create(playerName);
         return true;
      } catch (AccountNameConflictException var3) {
         return false;
      }
   }

   public int fractionalDigits() {
      return 2;
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
      Economy_MineConomy economy = null;

      public EconomyServerListener(Economy_MineConomy economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.econ == null) {
            Plugin eco = Economy_MineConomy.this.plugin.getServer().getPluginManager().getPlugin("MineConomy");
            if (eco != null) {
               this.economy.econ = (MineConomy)eco;
               Economy_MineConomy.log.info(String.format("[%s][Economy] %s hooked.", Economy_MineConomy.this.plugin.getDescription().getName(), "MineConomy"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.econ != null && event.getPlugin().getDescription().getName().equals("MineConomy")) {
            this.economy.econ = null;
            Economy_MineConomy.log.info(String.format("[%s][Economy] %s unhooked.", Economy_MineConomy.this.plugin.getDescription().getName(), "MineConomy"));
         }

      }
   }
}
