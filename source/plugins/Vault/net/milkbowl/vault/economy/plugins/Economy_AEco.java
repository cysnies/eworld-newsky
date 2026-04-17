package net.milkbowl.vault.economy.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.neocraft.AEco.AEco;

public class Economy_AEco implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "AEco";
   private Plugin plugin = null;
   private org.neocraft.AEco.part.Economy.Economy economy = null;
   private Method createWallet = null;

   public Economy_AEco(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      log.log(Level.WARNING, "AEco is an integer only economy, you may notice inconsistencies with accounts if you do not setup your other econ using plugins accordingly!");
      if (this.economy == null) {
         Plugin econ = plugin.getServer().getPluginManager().getPlugin("AEco");
         if (econ != null && econ.isEnabled()) {
            this.economy = AEco.ECONOMY;

            try {
               this.createWallet = this.economy.getClass().getMethod("createWallet", String.class);
               this.createWallet.setAccessible(true);
            } catch (SecurityException var4) {
            } catch (NoSuchMethodException var5) {
            }

            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "AEco"));
         }
      }

   }

   public String getName() {
      return "AEco";
   }

   public boolean isEnabled() {
      return this.economy != null;
   }

   public double getBalance(String playerName) {
      return (double)this.economy.cash(playerName);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         amount = Math.ceil(amount);
         int balance = this.economy.cash(playerName);
         if ((double)balance - amount < (double)0.0F) {
            return new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
         } else {
            this.economy.remove(playerName, (int)((double)balance - amount));
            balance = this.economy.cash(playerName);
            return new EconomyResponse(amount, (double)balance, EconomyResponse.ResponseType.SUCCESS, "");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
      } else {
         amount = Math.ceil(amount);
         this.economy.add(playerName, (int)amount);
         return new EconomyResponse(amount, this.getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, "");
      }
   }

   public String currencyNamePlural() {
      return "";
   }

   public String currencyNameSingular() {
      return "";
   }

   public String format(double amount) {
      amount = Math.ceil(amount);
      return amount == (double)1.0F ? String.format("%d %s", (int)amount, this.currencyNameSingular()) : String.format("%d %s", (int)amount, this.currencyNamePlural());
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) >= amount;
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "AEco does not support bank accounts!");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean hasBankSupport() {
      return false;
   }

   public boolean hasAccount(String playerName) {
      return true;
   }

   public boolean createPlayerAccount(String playerName) {
      try {
         return (Boolean)this.createWallet.invoke(this.economy.getClass(), playerName);
      } catch (IllegalArgumentException var3) {
      } catch (IllegalAccessException var4) {
      } catch (InvocationTargetException var5) {
      }

      return false;
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
      Economy_AEco economy = null;

      public EconomyServerListener(Economy_AEco economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin eco = Economy_AEco.this.plugin.getServer().getPluginManager().getPlugin("AEco");
            if (eco != null) {
               this.economy.economy = AEco.ECONOMY;

               try {
                  Economy_AEco.this.createWallet = this.economy.getClass().getMethod("createWallet", String.class);
                  Economy_AEco.this.createWallet.setAccessible(true);
               } catch (SecurityException var4) {
               } catch (NoSuchMethodException var5) {
               }

               Economy_AEco.log.info(String.format("[%s][Economy] %s hooked.", Economy_AEco.this.plugin.getDescription().getName(), "AEco"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("AEco")) {
            this.economy.economy = null;
            Economy_AEco.log.info(String.format("[%s][Economy] %s unhooked.", Economy_AEco.this.plugin.getDescription().getName(), "AEco"));
         }

      }
   }
}
