package net.milkbowl.vault.economy.plugins;

import com.github.omwah.SDFEconomy.SDFEconomy;
import com.github.omwah.SDFEconomy.SDFEconomyAPI;
import java.util.List;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

public class Economy_SDFEconomy implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private Plugin plugin = null;
   private final String name = "SDFEconomy";
   private SDFEconomyAPI api = null;

   public Economy_SDFEconomy(Plugin _plugin) {
      this.plugin = _plugin;
      this.plugin.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), this.plugin);
      this.load_api();
   }

   public void load_api() {
      SDFEconomy pluginSDF = (SDFEconomy)this.plugin.getServer().getPluginManager().getPlugin("SDFEconomy");
      if (!this.isEnabled() && pluginSDF != null) {
         this.api = pluginSDF.getAPI();
         log.info(String.format("[%s][Economy] %s hooked.", this.plugin.getDescription().getName(), "SDFEconomy"));
      }

   }

   public void unload_api() {
      SDFEconomy pluginSDF = (SDFEconomy)this.plugin.getServer().getPluginManager().getPlugin("SDFEconomy");
      if (this.isEnabled() && pluginSDF != null) {
         this.api = null;
         log.info(String.format("[%s][Economy] %s unhooked.", this.plugin.getDescription().getName(), "SDFEconomy"));
      }

   }

   public boolean isEnabled() {
      return this.api != null;
   }

   public String getName() {
      return "SDFEconomy";
   }

   public boolean hasBankSupport() {
      return this.api.hasBankSupport();
   }

   public int fractionalDigits() {
      return this.api.fractionalDigits();
   }

   public String format(double amount) {
      return this.api.format(amount);
   }

   public String currencyNamePlural() {
      return this.api.currencyNamePlural();
   }

   public String currencyNameSingular() {
      return this.api.currencyNameSingular();
   }

   public boolean hasAccount(String playerName) {
      return this.api.hasAccount(playerName);
   }

   public double getBalance(String playerName) {
      return this.api.getBalance(playerName);
   }

   public boolean has(String playerName, double amount) {
      return this.api.has(playerName, amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      return this.api.withdrawPlayer(playerName, amount);
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      return this.api.depositPlayer(playerName, amount);
   }

   public EconomyResponse createBank(String name, String player) {
      return this.api.createBank(name, player);
   }

   public EconomyResponse deleteBank(String name) {
      return this.api.deleteBank(name);
   }

   public EconomyResponse bankBalance(String name) {
      return this.api.bankBalance(name);
   }

   public EconomyResponse bankHas(String name, double amount) {
      return this.api.bankHas(name, amount);
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return this.api.bankWithdraw(name, amount);
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return this.api.bankDeposit(name, amount);
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return this.api.isBankOwner(name, playerName);
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return this.api.isBankMember(name, playerName);
   }

   public List getBanks() {
      return this.api.getBankNames();
   }

   public boolean createPlayerAccount(String playerName) {
      return this.api.createPlayerAccount(playerName);
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
      Economy_SDFEconomy economy = null;

      public EconomyServerListener(Economy_SDFEconomy economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (event.getPlugin().getDescription().getName().equals("SDFEconomy")) {
            this.economy.load_api();
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (event.getPlugin().getDescription().getName().equals("SDFEconomy")) {
            this.economy.unload_api();
         }

      }
   }
}
