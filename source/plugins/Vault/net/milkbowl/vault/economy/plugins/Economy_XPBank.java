package net.milkbowl.vault.economy.plugins;

import com.gmail.mirelatrue.xpbank.API;
import com.gmail.mirelatrue.xpbank.Account;
import com.gmail.mirelatrue.xpbank.GroupBank;
import com.gmail.mirelatrue.xpbank.XPBank;
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

public class Economy_XPBank implements Economy {
   private static final Logger log = Logger.getLogger("Minecraft");
   private final String name = "XPBank";
   private Plugin plugin = null;
   private XPBank XPB = null;
   private API api = null;

   public Economy_XPBank(Plugin plugin) {
      this.plugin = plugin;
      Bukkit.getServer().getPluginManager().registerEvents(new EconomyServerListener(this), plugin);
      if (this.XPB == null) {
         Plugin economy = plugin.getServer().getPluginManager().getPlugin("XPBank");
         if (economy != null && economy.isEnabled()) {
            this.XPB = (XPBank)economy;
            this.api = this.XPB.getAPI();
            log.info(String.format("[%s][Economy] %s hooked.", plugin.getDescription().getName(), "XPBank"));
         }
      }

   }

   public boolean isEnabled() {
      return this.XPB != null;
   }

   public String getName() {
      return "XPBank";
   }

   public boolean hasBankSupport() {
      return true;
   }

   public int fractionalDigits() {
      return 0;
   }

   public String format(double amount) {
      return String.format("%d %s", (int)amount, this.api.currencyName((int)amount));
   }

   public String currencyNamePlural() {
      return this.api.getMsg("CurrencyNamePlural");
   }

   public String currencyNameSingular() {
      return this.api.getMsg("currencyName");
   }

   public boolean hasAccount(String playerName) {
      Account account = this.api.getAccount(playerName);
      return account != null;
   }

   public double getBalance(String playerName) {
      Account account = this.api.getAccount(playerName);
      return (double)account.getBalance();
   }

   public boolean has(String playerName, double amount) {
      Account account = this.api.getAccount(playerName);
      return account.getBalance() >= (int)amount;
   }

   public EconomyResponse withdrawPlayer(String playerName, double amount) {
      Account account = this.api.getAccount(playerName);
      if (account == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("Player doesn't exist."));
      } else {
         int value = (int)amount;
         int balance = account.getBalance();
         if (value < 1) {
            return new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("LessThanZero"));
         } else if (value > balance) {
            return new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, String.format(this.api.getMsg("InsufficientXP"), this.api.currencyName(value)));
         } else {
            account.modifyBalance(-value);
            return new EconomyResponse((double)value, (double)(balance - value), EconomyResponse.ResponseType.SUCCESS, (String)null);
         }
      }
   }

   public EconomyResponse depositPlayer(String playerName, double amount) {
      Account account = this.api.getAccount(playerName);
      if (account == null) {
         this.createPlayerAccount(playerName);
      }

      int value = (int)amount;
      int balance = account.getBalance();
      if (value < 1) {
         return new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("LessThanZero"));
      } else {
         account.addTaxableIncome(value);
         return new EconomyResponse((double)value, (double)(balance + value), EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public EconomyResponse createBank(String name, String player) {
      GroupBank groupBank = this.api.getGroupBank(name);
      if (groupBank != null) {
         return new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.FAILURE, String.format(this.api.getMsg("GroupBankExists"), name));
      } else {
         Account account = this.api.getAccount(player);
         groupBank = this.api.createGroupBank(name, account);
         return new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public EconomyResponse deleteBank(String name) {
      GroupBank groupBank = this.api.getGroupBank(name);
      if (groupBank == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("GroupBankNotExists"));
      } else {
         this.api.deleteGroupBank(groupBank, String.format(this.api.getMsg("Disbanded"), groupBank.getName()));
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.SUCCESS, (String)null);
      }
   }

   public EconomyResponse bankBalance(String name) {
      GroupBank groupBank = this.api.getGroupBank(name);
      return groupBank == null ? new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("GroupBankNotExists")) : new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.SUCCESS, (String)null);
   }

   public EconomyResponse bankHas(String name, double amount) {
      GroupBank groupBank = this.api.getGroupBank(name);
      if (groupBank == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("GroupBankNotExists"));
      } else {
         int value = (int)amount;
         int balance = groupBank.getBalance();
         return balance >= value ? new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.SUCCESS, (String)null) : new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, String.format(this.api.getMsg("InsufficientXP"), this.api.currencyName(value)));
      }
   }

   public EconomyResponse bankWithdraw(String name, double amount) {
      GroupBank groupBank = this.api.getGroupBank(name);
      if (groupBank == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("GroupBankNotExists"));
      } else {
         int value = (int)amount;
         int balance = groupBank.getBalance();
         if (value < 1) {
            return new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("LessThanZero"));
         } else if (value > balance) {
            return new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, String.format(this.api.getMsg("InsufficientXP"), this.api.currencyName(value)));
         } else {
            groupBank.modifyBalance(-value);
            return new EconomyResponse((double)value, (double)(balance - value), EconomyResponse.ResponseType.SUCCESS, (String)null);
         }
      }
   }

   public EconomyResponse bankDeposit(String name, double amount) {
      GroupBank groupBank = this.api.getGroupBank(name);
      if (groupBank == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("GroupBankNotExists"));
      } else {
         int value = (int)amount;
         int balance = groupBank.getBalance();
         if (value < 1) {
            return new EconomyResponse((double)0.0F, (double)balance, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("LessThanZero"));
         } else {
            groupBank.modifyBalance(value);
            return new EconomyResponse((double)value, (double)(balance + value), EconomyResponse.ResponseType.SUCCESS, (String)null);
         }
      }
   }

   public EconomyResponse isBankOwner(String name, String playerName) {
      GroupBank groupBank = this.api.getGroupBank(name);
      if (groupBank == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("GroupBankNotExists"));
      } else {
         Account account = this.api.getAccount(name);
         if (account == null) {
            return new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.FAILURE, this.api.getMsg("PlayerNotExist"));
         } else {
            return groupBank.getOwner().equalsIgnoreCase(name) ? new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.SUCCESS, (String)null) : new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.FAILURE, String.format(this.api.getMsg("PlayerNotOwner"), account.getName(), groupBank.getName()));
         }
      }
   }

   public EconomyResponse isBankMember(String name, String playerName) {
      GroupBank groupBank = this.api.getGroupBank(name);
      if (groupBank == null) {
         return new EconomyResponse((double)0.0F, (double)0.0F, EconomyResponse.ResponseType.FAILURE, this.api.getMsg("GroupBankNotExists"));
      } else {
         Account account = this.api.getAccount(name);
         if (account == null) {
            return new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.FAILURE, this.api.getMsg("PlayerNotExist"));
         } else {
            return groupBank.groupMembers.getMembers().containsKey(playerName) ? new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.SUCCESS, (String)null) : new EconomyResponse((double)0.0F, (double)groupBank.getBalance(), EconomyResponse.ResponseType.FAILURE, String.format(this.api.getMsg("NotAMemberOf"), groupBank.getName(), account.getName()));
         }
      }
   }

   public List getBanks() {
      return this.api.getAllGroupBanks();
   }

   public boolean createPlayerAccount(String playerName) {
      this.api.createAccount(playerName);
      return true;
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
      Economy_XPBank economy = null;

      public EconomyServerListener(Economy_XPBank economy_XPBank) {
         this.economy = economy_XPBank;
      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginEnable(PluginEnableEvent event) {
         if (this.economy.XPB == null) {
            Plugin eco = Economy_XPBank.this.plugin.getServer().getPluginManager().getPlugin("XPBank");
            if (eco != null) {
               this.economy.XPB = (XPBank)eco;
               Economy_XPBank.this.api = Economy_XPBank.this.XPB.getAPI();
               Economy_XPBank.log.info(String.format("[%s][Economy] %s hooked.", Economy_XPBank.this.plugin.getDescription().getName(), "XPBank"));
            }
         }

      }

      @EventHandler(
         priority = EventPriority.MONITOR
      )
      public void onPluginDisable(PluginDisableEvent event) {
         if (this.economy.XPB != null && event.getPlugin().getDescription().getName().equals("XPBank")) {
            this.economy.XPB = null;
            Economy_XPBank.log.info(String.format("[%s][Economy] %s unhooked.", Economy_XPBank.this.plugin.getDescription().getName(), "XPBank"));
         }

      }
   }
}
