package net.milkbowl.vault.economy.plugins;

import com.nijiko.coelho.iConomy.iConomy;
import com.nijiko.coelho.iConomy.system.Account;
import java.util.ArrayList;
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

public class Economy_iConomy4 implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "iConomy 4";
   private Plugin plugin = null;
   protected iConomy economy = null;

   public Economy_iConomy4(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      log.log(Level.SEVERE, "iConomy4 is an extremely outdated plugin and can not be used reliably for economy! You should update to the more recent and maintained iConomy6 for compatibility!");
      if (this.economy == null) {
         Plugin ec = plugin.getServer().getPluginManager().getPlugin("iConomy");
         if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("com.nijiko.coelho.iConomy.iConomy.class")) {
            this.economy = (iConomy)ec;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "iConomy 4"));
         }
      }

   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public String getName() {
      return "iConomy 4";
   }

   public String format(double amount) {
      return iConomy.getBank().format(amount);
   }

   public String currencyNamePlural() {
      return iConomy.getBank().getCurrency() + "s";
   }

   public String currencyNameSingular() {
      return iConomy.getBank().getCurrency();
   }

   public double getBalance(String playerName) {
      return this.getAccountBalance(playerName);
   }

   private double getAccountBalance(String playerName) {
      Account account = iConomy.getBank().getAccount(playerName);
      if (account == null) {
         iConomy.getBank().addAccount(playerName);
         account = iConomy.getBank().getAccount(playerName);
      }

      return account.getBalance();
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      String errorMessage = null;
      if (amount < (double)0.0F) {
         errorMessage = "Cannot withdraw negative funds";
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
         amount = (double)0.0F;
         double balance = this.getAccountBalance(playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else {
         double balance = this.getAccountBalance(playerName);
         if (balance >= amount) {
            Account account = iConomy.getBank().getAccount(playerName);
            if (account == null) {
               return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Could not find account");
            } else {
               account.subtract(amount);
               EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
               balance = this.getAccountBalance(playerName);
               return new EconomyResponse(amount, balance, type, errorMessage);
            }
         } else {
            errorMessage = "Error withdrawing funds";
            EconomyResponse.ResponseType type = EconomyResponse.ResponseType.FAILURE;
            amount = (double)0.0F;
            balance = this.getAccountBalance(playerName);
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
         double balance = this.getAccountBalance(playerName);
         return new EconomyResponse(amount, balance, type, errorMessage);
      } else {
         Account account = iConomy.getBank().getAccount(playerName);
         if (account == null) {
            iConomy.getBank().addAccount(playerName);
            account = iConomy.getBank().getAccount(playerName);
         }

         account.add(amount);
         double balance = this.getAccountBalance(playerName);
         EconomyResponse.ResponseType type = EconomyResponse.ResponseType.SUCCESS;
         return new EconomyResponse(amount, balance, type, errorMessage);
      }
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "iConomy4 does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return iConomy.getBank().hasAccount(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      if (this.hasAccount(playerName)) {
         return false;
      } else {
         iConomy.getBank().addAccount(playerName);
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
      Economy_iConomy4 economy = null;

      public EconomyServerListener(Economy_iConomy4 economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin iConomy = Economy_iConomy4.this.plugin.getServer().getPluginManager().getPlugin("iConomy");
            if (iConomy != null && iConomy.getClass().getName().equals("com.nijiko.coelho.iConomy.iConomy")) {
               this.economy.economy = (iConomy)iConomy;
               Economy_iConomy4.log.info(String.format("[%s][Economy] %s hooked.", Economy_iConomy4.this.plugin.getDescription().getName(), "iConomy 4"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("iConomy")) {
            this.economy.economy = null;
            Economy_iConomy4.log.info(String.format("[%s][Economy] %s unhooked.", Economy_iConomy4.this.plugin.getDescription().getName(), "iConomy 4"));
         }

      }
   }
}
