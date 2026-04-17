package net.milkbowl.vault.economy.plugins;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.api.NoLoanPermittedException;
import com.earth2me.essentials.api.UserDoesNotExistException;
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

public class Economy_Essentials implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "Essentials Economy";
   private Plugin plugin = null;
   private Essentials ess = null;

   public Economy_Essentials(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.ess == null) {
         Plugin essentials = plugin.getServer().getPluginManager().getPlugin("Essentials");
         if (essentials != null && essentials.isEnabled()) {
            this.ess = (Essentials)essentials;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "Essentials Economy"));
         }
      }

   }

   public boolean isEnabled() {
      return this.ess == null ? false : this.ess.isEnabled();
   }

   public String getName() {
      return "Essentials Economy";
   }

   public double getBalance(String playerName) {
      double balance;
      try {
         balance = com.earth2me.essentials.api.Economy.getMoney(playerName);
      } catch (UserDoesNotExistException var5) {
         this.createPlayerAccount(playerName);
         balance = (double)0.0F;
      }

      return balance;
   }

   public boolean createPlayerAccount(String playerName) {
      return this.hasAccount(playerName) ? false : com.earth2me.essentials.api.Economy.createNPC(playerName);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         String errorMessage = null;

         double balance;
         EconomyResponse.ResponseType type;
         try {
            com.earth2me.essentials.api.Economy.subtract(playerName, amount);
            balance = com.earth2me.essentials.api.Economy.getMoney(playerName);
            type = EconomyResponse.ResponseType.SUCCESS;
         } catch (UserDoesNotExistException var11) {
            if (this.createPlayerAccount(playerName)) {
               return this.withdrawPlayer(playerName, amount);
            }

            amount = (double)0.0F;
            balance = (double)0.0F;
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "User does not exist";
         } catch (NoLoanPermittedException var12) {
            try {
               balance = com.earth2me.essentials.api.Economy.getMoney(playerName);
               amount = (double)0.0F;
               type = EconomyResponse.ResponseType.FAILURE;
               errorMessage = "Loan was not permitted";
            } catch (UserDoesNotExistException var10) {
               amount = (double)0.0F;
               balance = (double)0.0F;
               type = EconomyResponse.ResponseType.FAILURE;
               errorMessage = "User does not exist";
            }
         }

         return new EconomyResponse(amount, balance, type, errorMessage);
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         String errorMessage = null;

         double balance;
         EconomyResponse.ResponseType type;
         try {
            com.earth2me.essentials.api.Economy.add(playerName, amount);
            balance = com.earth2me.essentials.api.Economy.getMoney(playerName);
            type = EconomyResponse.ResponseType.SUCCESS;
         } catch (UserDoesNotExistException var11) {
            if (this.createPlayerAccount(playerName)) {
               return this.depositPlayer(playerName, amount);
            }

            amount = (double)0.0F;
            balance = (double)0.0F;
            type = EconomyResponse.ResponseType.FAILURE;
            errorMessage = "User does not exist";
         } catch (NoLoanPermittedException var12) {
            try {
               balance = com.earth2me.essentials.api.Economy.getMoney(playerName);
               amount = (double)0.0F;
               type = EconomyResponse.ResponseType.FAILURE;
               errorMessage = "Loan was not permitted";
            } catch (UserDoesNotExistException var10) {
               balance = (double)0.0F;
               amount = (double)0.0F;
               type = EconomyResponse.ResponseType.FAILURE;
               errorMessage = "Loan was not permitted";
            }
         }

         return new EconomyResponse(amount, balance, type, errorMessage);
      }
   }

   public String format(double amount) {
      return com.earth2me.essentials.api.Economy.format(amount);
   }

   public String currencyNameSingular() {
      return "";
   }

   public String currencyNamePlural() {
      return "";
   }

   public boolean has(String playerName, double amount) {
      try {
         return com.earth2me.essentials.api.Economy.hasEnough(playerName, amount);
      } catch (UserDoesNotExistException var5) {
         return false;
      }
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Essentials Eco does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return com.earth2me.essentials.api.Economy.playerExists(playerName);
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
      Economy_Essentials economy = null;

      public EconomyServerListener(Economy_Essentials economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.ess == null) {
            Plugin essentials = Economy_Essentials.this.plugin.getServer().getPluginManager().getPlugin("Essentials");
            if (essentials != null) {
               this.economy.ess = (Essentials)essentials;
               Economy_Essentials.log.info(String.format("[%s][Economy] %s hooked.", Economy_Essentials.this.plugin.getDescription().getName(), "Essentials Economy"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.ess != null && event.getPlugin().getDescription().getName().equals("Essentials")) {
            this.economy.ess = null;
            Economy_Essentials.log.info(String.format("[%s][Economy] %s unhooked.", Economy_Essentials.this.plugin.getDescription().getName(), "Essentials Economy"));
         }

      }
   }
}
