package net.milkbowl.vault.economy.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.ic3d.eco.ECO;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_3co implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "3co";
   private Plugin plugin = null;
   private ECO economy = null;

   public Economy_3co(Plugin plugin) {
      this.plugin = plugin;
      log.log(Level.SEVERE, "3co is outdated and WILL BREAK in CB-R5+ - It is highly recommended to update to a new economy plugin and use Vaults conversion!");
      log.log(Level.WARNING, "3co is an integer only economy, you may notice inconsistencies with accounts if you do not setup your other econ using plugins accordingly!");
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin econ = plugin.getServer().getPluginManager().getPlugin("3co");
         if (econ != null && econ.isEnabled()) {
            this.economy = (ECO)econ;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "3co"));
         }
      }

   }

   public String getName() {
      return "3co";
   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public double getBalance(String playerName) {
      double balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
      return balance;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      String errorMessage = null;
      if (amount < (double)0.0F) {
         errorMessage = "Cannot withdraw negative funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else {
         amount = Math.ceil(amount);
         double balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
         if (balance - amount < (double)0.0F) {
            errorMessage = "Insufficient funds";
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
            amount = (double)0.0F;
            balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
            return new EconomyResponse(amount, balance, type, errorMessage);
         } else {
            this.economy.setMoney(this.plugin.getServer().getPlayer(playerName), (int)(balance - amount));
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
            balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
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
         double balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else {
         amount = Math.ceil(amount);
         double balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
         this.economy.setMoney(this.plugin.getServer().getPlayer(playerName), (int)(balance + amount));
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
         balance = (double)this.economy.getMoney(this.plugin.getServer().getPlayer(playerName));
         return new EconomyResponse(amount, balance, type, errorMessage);
      }
   }

   public String currencyNamePlural() {
      return this.economy.getPluralCurrency();
   }

   public String currencyNameSingular() {
      return this.economy.getSingularCurrency();
   }

   public String format(double amount) {
      amount = Math.ceil(amount);
      return amount == (double)1.0F ? String.format("%d %s", (int)amount, this.currencyNameSingular()) : String.format("%d %s", (int)amount, this.currencyNamePlural());
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "3co does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return this.economy.hasAccount(this.plugin.getServer().getPlayer(playerName));
   }

   public boolean createPlayerAccount(String playerName) {
      Player p = Bukkit.getPlayer(playerName);
      if (p == null) {
         return false;
      } else {
         this.economy.createAccount(p, 0);
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
      Economy_3co economy = null;

      public EconomyServerListener(Economy_3co economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin eco = Economy_3co.this.plugin.getServer().getPluginManager().getPlugin("3co");
            if (eco != null) {
               this.economy.economy = (ECO)eco;
               Economy_3co.log.info(String.format("[%s][Economy] %s hooked.", Economy_3co.this.plugin.getDescription().getName(), "3co"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("3co")) {
            this.economy.economy = null;
            Economy_3co.log.info(String.format("[%s][Economy] %s unhooked.", Economy_3co.this.plugin.getDescription().getName(), "3co"));
         }

      }
   }
}
