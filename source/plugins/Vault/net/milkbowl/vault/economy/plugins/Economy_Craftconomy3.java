package net.milkbowl.vault.economy.plugins;

import com.greatmancode.craftconomy3.BukkitLoader;
import com.greatmancode.craftconomy3.Common;
import com.greatmancode.craftconomy3.account.Account;
import com.greatmancode.craftconomy3.database.tables.AccountTable;
import java.util.ArrayList;
import java.util.Iterator;
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

public class Economy_Craftconomy3 implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "Craftconomy3";
   private Plugin plugin = null;
   protected BukkitLoader economy = null;

   public Economy_Craftconomy3(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.economy == null) {
         Plugin ec = plugin.getServer().getPluginManager().getPlugin("Craftconomy3");
         if (ec != null && ec.isEnabled() && ec.getClass().getName().equals("com.greatmancode.craftconomy3.BukkitLoader")) {
            this.economy = (BukkitLoader)ec;
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "Craftconomy3"));
         }
      }

   }

   public boolean isEnabled() {
      return this.economy == null ? false : this.economy.isEnabled();
   }

   public String getName() {
      return "Craftconomy3";
   }

   public String format(double amount) {
      return Common.getInstance().format((String)null, Common.getInstance().getCurrencyManager().getDefaultCurrency(), amount);
   }

   public String currencyNameSingular() {
      return Common.getInstance().getCurrencyManager().getDefaultCurrency().getName();
   }

   public String currencyNamePlural() {
      return Common.getInstance().getCurrencyManager().getDefaultCurrency().getPlural();
   }

   public double getBalance(String playerName) {
      return this.getBalance(playerName, "default");
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      return this.withdrawPlayer(playerName, "default", amount);
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      return this.depositPlayer(playerName, "default", amount);
   }

   public boolean has(String playerName, double amount) {
      return this.has(playerName, "default", amount);
   }

   public EconomyResponse createBank(String name, String player) {
      boolean success = false;
      if (!Common.getInstance().getAccountManager().exist("bank:" + name)) {
         Common.getInstance().getAccountManager().getAccount("bank:" + name).getAccountACL().set(player, true, true, true, true, true);
         success = true;
      }

      return success ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to create that bank account. It already exists!");
   }

   public EconomyResponse deleteBank(String name) {
      boolean success = Common.getInstance().getAccountManager().delete("bank:" + name);
      return success ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Unable to delete that bank account.");
   }

   public EconomyResponse bankHas(String name, double amount) {
      if (Common.getInstance().getAccountManager().exist("bank:" + name)) {
         Account account = Common.getInstance().getAccountManager().getAccount("bank:" + name);
         return account.hasEnough(amount, Common.getInstance().getServerCaller().getDefaultWorld(), Common.getInstance().getCurrencyManager().getDefaultCurrency().getName()) ? new EconomyResponse((double)0.0F, this.bankBalance("bank:" + name).balance, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, this.bankBalance("bank:" + name).balance, EconomyResponse.ResponseType.FAILURE, "The bank does not have enough money!");
      } else {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      }
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         EconomyResponse er = this.bankHas(name, amount);
         if (!er.transactionSuccess()) {
            return er;
         } else {
            return Common.getInstance().getAccountManager().exist("bank:" + name) ? new EconomyResponse((double)0.0F, this.withdrawPlayer("bank:" + name, amount).balance, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
         }
      }
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         return Common.getInstance().getAccountManager().exist("bank:" + name) ? new EconomyResponse((double)0.0F, this.depositPlayer("bank:" + name, amount).balance, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      }
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      if (Common.getInstance().getAccountManager().exist("bank:" + name)) {
         return Common.getInstance().getAccountManager().getAccount("bank:" + name).getAccountACL().isOwner(playerName) ? new EconomyResponse((double)0.0F, this.bankBalance("bank:" + name).balance, EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "This player is not the owner of the bank!");
      } else {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
      }
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      EconomyResponse er = this.isBankOwner(name, playerName);
      if (er.transactionSuccess()) {
         return er;
      } else {
         if (Common.getInstance().getAccountManager().exist("bank:" + name)) {
            Account account = Common.getInstance().getAccountManager().getAccount("bank:" + name);
            if (account.getAccountACL().canDeposit(playerName) && account.getAccountACL().canWithdraw(playerName)) {
               return new EconomyResponse((double)0.0F, this.bankBalance(name).balance, EconomyResponse.ResponseType.SUCCESS, "");
            }
         }

         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "This player is not a member of the bank!");
      }
   }

   public EconomyResponse bankBalance(String name) {
      return Common.getInstance().getAccountManager().exist("bank:" + name) ? new EconomyResponse((double)0.0F, this.getBalance("bank:" + name), EconomyResponse.ResponseType.SUCCESS, "") : new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, "That bank does not exist!");
   }

   public List getBanks() {
      List<AccountTable> accountList = Common.getInstance().getDatabaseManager().getDatabase().select(AccountTable.class).where().contains("name", "bank:").execute().find();
      ArrayList<String> list = new ArrayList();
      Iterator<AccountTable> iterator = accountList.iterator();

      while(iterator.hasNext()) {
         list.add(((AccountTable)iterator.next()).getName().replaceFirst("bank:", ""));
      }

      return list;
   }

   public boolean hasBankSupport() {
      return true;
   }

   public boolean hasAccount(String playerName) {
      return Common.getInstance().getAccountManager().exist(playerName);
   }

   public boolean createPlayerAccount(String playerName) {
      if (Common.getInstance().getAccountManager().exist(playerName)) {
         return false;
      } else {
         Common.getInstance().getAccountManager().getAccount(playerName);
         return true;
      }
   }

   public int fractionalDigits() {
      return -1;
   }

   public boolean hasAccount(String playerName, String worldName) {
      return this.hasAccount(playerName);
   }

   public double getBalance(String playerName, String world) {
      return Common.getInstance().getAccountManager().getAccount(playerName).getBalance(world, Common.getInstance().getCurrencyManager().getDefaultCurrency().getName());
   }

   public boolean has(String playerName, String worldName, double amount) {
      return this.getBalance(playerName, worldName) >= amount;
   }

   public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.getBalance(playerName, worldName), EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
      } else {
         Account account = Common.getInstance().getAccountManager().getAccount(playerName);
         if (account.hasEnough(amount, worldName, Common.getInstance().getCurrencyManager().getDefaultCurrency().getName())) {
            double balance = account.withdraw(amount, worldName, Common.getInstance().getCurrencyManager().getDefaultCurrency().getName());
            return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, "");
         } else {
            return new EconomyResponse((double)0.0F, this.getBalance(playerName, worldName), EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
      if (amount < (double)0.0F) {
         return new EconomyResponse((double)0.0F, this.getBalance(playerName, worldName), EconomyResponse.ResponseType.FAILURE, "Cannot desposit negative funds");
      } else {
         Account account = Common.getInstance().getAccountManager().getAccount(playerName);
         double balance = account.deposit(amount, worldName, Common.getInstance().getCurrencyManager().getDefaultCurrency().getName());
         return new EconomyResponse(amount, balance, EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public boolean createPlayerAccount(String playerName, String worldName) {
      return this.createPlayerAccount(playerName);
   }

   public class EconomyServerListener implements Listener {
      Economy_Craftconomy3 economy = null;

      public EconomyServerListener(Economy_Craftconomy3 economy) {
         this.economy = economy;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.economy == null) {
            Plugin ec = Economy_Craftconomy3.this.plugin.getServer().getPluginManager().getPlugin("Craftconomy3");
            if (ec != null && ec.getClass().getName().equals("com.greatmancode.craftconomy3.BukkitLoader")) {
               this.economy.economy = (BukkitLoader)ec;
               Economy_Craftconomy3.log.info(String.format("[%s][Economy] %s hooked.", Economy_Craftconomy3.this.plugin.getDescription().getName(), "Craftconomy3"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.economy != null && event.getPlugin().getDescription().getName().equals("Craftconomy3")) {
            this.economy.economy = null;
            Economy_Craftconomy3.log.info(String.format("[%s][Economy] %s unhooked.", Economy_Craftconomy3.this.plugin.getDescription().getName(), "Craftconomy3"));
         }

      }
   }
}
