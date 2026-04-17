package net.milkbowl.vault.economy.plugins;

import com.gravypod.Dosh.Dosh;
import com.gravypod.Dosh.MoneyUtils;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.plugin.Plugin;

public class Economy_Dosh implements Economy {
   Plugin plugin;
   Dosh doshPlugin;
   DoshAPIHandler apiHandle;

   public Economy_Dosh(Plugin _plugin) {
      this.plugin = _plugin;
      if (this.plugin.getServer().getPluginManager().isPluginEnabled("Dosh")) {
         this.doshPlugin = (Dosh)this.plugin.getServer().getPluginManager().getPlugin("Dosh");
         this.apiHandle = new DoshAPIHandler();
      }
   }

   public boolean isEnabled() {
      return this.apiHandle != null;
   }

   public String getName() {
      return "Dosh";
   }

   public boolean hasBankSupport() {
      return false;
   }

   public int fractionalDigits() {
      return 0;
   }

   public String format(double amount) {
      return null;
   }

   public String currencyNamePlural() {
      return Dosh.getSettings().moneyName + "s";
   }

   public String currencyNameSingular() {
      return Dosh.getSettings().moneyName;
   }

   public boolean hasAccount(String playerName) {
      return true;
   }

   public double getBalance(String playerName) {
      return Economy_Dosh.DoshAPIHandler.getUserBal(playerName);
   }

   public boolean has(String playerName, double amount) {
      return this.getBalance(playerName) - amount > (double)0.0F;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      return Economy_Dosh.DoshAPIHandler.subtractMoney(playerName, amount) ? new EconomyResponse(amount, this.getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, "Worked!") : new EconomyResponse(amount, this.getBalance(playerName), EconomyResponse.ResponseType.FAILURE, "Didnt work!");
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      Economy_Dosh.DoshAPIHandler.addUserBal(playerName, amount);
      return new EconomyResponse(amount, this.getBalance(playerName), EconomyResponse.ResponseType.SUCCESS, "It worked!");
   }

   public EconomyResponse createBank(String name, String player) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not use banks!");
   }

   public EconomyResponse deleteBank(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not use banks!");
   }

   public EconomyResponse bankBalance(String name) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not use banks!");
   }

   public EconomyResponse bankHas(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not use banks!");
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not use banks!");
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "We do not use banks!");
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      return null;
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      return null;
   }

   public List getBanks() {
      return null;
   }

   public boolean createPlayerAccount(String playerName) {
      return false;
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

   public class DoshAPIHandler extends MoneyUtils {
   }
}
