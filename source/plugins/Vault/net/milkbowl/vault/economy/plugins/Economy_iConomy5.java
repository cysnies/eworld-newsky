package net.milkbowl.vault.economy.plugins;

import com.iConomy.iConomy;
import com.iConomy.system.Holdings;
import com.iConomy.util.Constants;
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

public class Economy_iConomy5 implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "iConomy 5";
   private Plugin plugin = null;
   protected iConomy economy = null;

   public Economy_iConomy5(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin ec = plugin.getServer().getPluginManager().getPlugin("iConomy");
         if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("com.iConomy.iConomy")) {
            this.economy = (iConomy)ec;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "iConomy 5"));
         }
      }

   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public String getName() {
      return "iConomy 5";
   }

   private double getAccountBalance(String playerName) {
      return iConomy.getAccount(playerName).getHoldings().balance();
   }

   public double getBalance(String playerName) {
      return this.getAccountBalance(playerName);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         Holdings holdings = iConomy.getAccount(playerName).getHoldings();
         if (holdings.hasEnough(amount)) {
            holdings.subtract(amount);
            return new EconomyResponse(amount, this.getAccountBalance(playerName), EconomyResponse.ResponseType.SUCCESS, (String)null);
         } else {
            return new EconomyResponse((double)0.0F, this.getAccountBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         Holdings holdings = iConomy.getAccount(playerName).getHoldings();
         holdings.add(amount);
         return new EconomyResponse(amount, holdings.balance(), EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public String format(double amount) {
      return iConomy.format(amount);
   }

   public String currencyNameSingular() {
      try {
         return (String)Constants.Major.get(0);
      } catch (Exception var2) {
         return "";
      }
   }

   public String currencyNamePlural() {
      try {
         return (String)Constants.Major.get(1);
      } catch (Exception var2) {
         return "";
      }
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single account banks!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy5 does not support single bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return iConomy.hasAccount(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      if (this.hasAccount(playerName)) {
         return false;
      } else {
         iConomy.getAccount(playerName);
         return true;
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
      Economy_iConomy5 economy = null;

      public EconomyServerListener(Economy_iConomy5 economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin ec = Economy_iConomy5.this.plugin.getServer().getPluginManager().getPlugin("iConomy");
            if (ec != null && ec.getClass().getName().equals("com.iConomy.iConomy")) {
               this.economy.economy = (iConomy)ec;
               Economy_iConomy5.log.info(String.format("[%s][Economy] %s hooked.", Economy_iConomy5.this.plugin.getDescription().getName(), "iConomy 5"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("iConomy")) {
            this.economy.economy = null;
            Economy_iConomy5.log.info(String.format("[%s][Economy] %s unhooked.", Economy_iConomy5.this.plugin.getDescription().getName(), "iConomy 5"));
         }

      }
   }
}
