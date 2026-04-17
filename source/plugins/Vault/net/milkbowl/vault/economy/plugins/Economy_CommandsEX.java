package net.milkbowl.vault.economy.plugins;

import com.github.zathrus_writer.commandsex.CommandsEX;
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

public class Economy_CommandsEX implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "CommandsEX Economy";
   private Plugin plugin = null;
   private CommandsEX economy = null;

   public Economy_CommandsEX(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin commandsex = plugin.getServer().getPluginManager().getPlugin("CommandsEX");
         if (commandsex != null && commandsex.isEnabled()) {
            this.economy = (CommandsEX)commandsex;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "CommandsEX Economy"));
         }
      }

   }

   public boolean isEnabled() {
      return this.economy == null ? false : com.github.zathrus_writer.commandsex.api.economy.Economy.isEnabled();
   }

   public String getName() {
      return "CommandsEX Economy";
   }

   public boolean hasBankSupport() {
      return false;
   }

   public int fractionalDigits() {
      return 2;
   }

   public String format(double amount) {
      return com.github.zathrus_writer.commandsex.api.economy.Economy.getCurrencySymbol() + amount;
   }

   public String currencyNamePlural() {
      return com.github.zathrus_writer.commandsex.api.economy.Economy.getCurrencyPlural();
   }

   public String currencyNameSingular() {
      return com.github.zathrus_writer.commandsex.api.economy.Economy.getCurrencySingular();
   }

   public boolean hasAccount(String playerName) {
      return com.github.zathrus_writer.commandsex.api.economy.Economy.hasAccount(playerName);
   }

   public double getBalance(String playerName) {
      return com.github.zathrus_writer.commandsex.api.economy.Economy.getBalance(playerName);
   }

   public boolean has(String playerName, double amount) {
      return com.github.zathrus_writer.commandsex.api.economy.Economy.has(playerName, amount);
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      EconomyResponse.ResponseType rt;
      String message;
      if (com.github.zathrus_writer.commandsex.api.economy.Economy.has(playerName, amount)) {
         com.github.zathrus_writer.commandsex.api.economy.Economy.withdraw(playerName, amount);
         rt = EconomyResponse.ResponseType.SUCCESS;
         message = null;
      } else {
         rt = EconomyResponse.ResponseType.FAILURE;
         message = "Not enough money";
      }

      return new EconomyResponse(amount, com.github.zathrus_writer.commandsex.api.economy.Economy.getBalance(playerName), rt, message);
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      com.github.zathrus_writer.commandsex.api.economy.Economy.deposit(playerName, amount);
      return new EconomyResponse(amount, com.github.zathrus_writer.commandsex.api.economy.Economy.getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, "Successfully deposited");
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "CommandsEX Economy does not support bank accounts");
   }

   public List getBanks() {
      return new ArrayList();
   }

   public boolean createPlayerAccount(String playerName) {
      if (com.github.zathrus_writer.commandsex.api.economy.Economy.hasAccount(playerName)) {
         return false;
      } else {
         com.github.zathrus_writer.commandsex.api.economy.Economy.createAccount(playerName);
         return true;
      }
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
      Economy_CommandsEX economy = null;

      public EconomyServerListener(Economy_CommandsEX economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin cex = Economy_CommandsEX.this.plugin.getServer().getPluginManager().getPlugin("CommandsEX");
            if (cex != null) {
               this.economy.economy = (CommandsEX)cex;
               Economy_CommandsEX.log.info(String.format("[%s][Economy] %s hooked.", Economy_CommandsEX.this.plugin.getDescription().getName(), "CommandsEX Economy"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("CommandsEX")) {
            this.economy.economy = null;
            Economy_CommandsEX.log.info(String.format("[%s][Economy] %s unhooked.", Economy_CommandsEX.this.plugin.getDescription().getName(), "CommandsEX Economy"));
         }

      }
   }
}
