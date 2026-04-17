package com.earth2me.essentials.register.payment.methods;

import com.earth2me.essentials.register.payment.Method;
import cosine.boseconomy.BOSEconomy;
import org.bukkit.plugin.Plugin;

public class BOSE7 implements Method {
   private BOSEconomy BOSEconomy;

   public BOSEconomy getPlugin() {
      return this.BOSEconomy;
   }

   public String getName() {
      return "BOSEconomy";
   }

   public String getLongName() {
      return this.getName();
   }

   public String getVersion() {
      return "0.7.0";
   }

   public int fractionalDigits() {
      return this.BOSEconomy.getFractionalDigits();
   }

   public String format(double amount) {
      String currency = this.BOSEconomy.getMoneyNamePlural();
      if (amount == (double)1.0F) {
         currency = this.BOSEconomy.getMoneyName();
      }

      return amount + " " + currency;
   }

   public boolean hasBanks() {
      return true;
   }

   public boolean hasBank(String bank) {
      return this.BOSEconomy.bankExists(bank);
   }

   public boolean hasAccount(String name) {
      return this.BOSEconomy.playerRegistered(name, false);
   }

   public boolean hasBankAccount(String bank, String name) {
      return this.BOSEconomy.isBankOwner(bank, name) || this.BOSEconomy.isBankMember(bank, name);
   }

   public boolean createAccount(String name) {
      if (this.hasAccount(name)) {
         return false;
      } else {
         this.BOSEconomy.registerPlayer(name);
         return true;
      }
   }

   public boolean createAccount(String name, Double balance) {
      if (this.hasAccount(name)) {
         return false;
      } else {
         this.BOSEconomy.registerPlayer(name);
         this.BOSEconomy.setPlayerMoney(name, balance, false);
         return true;
      }
   }

   public Method.MethodAccount getAccount(String name) {
      return !this.hasAccount(name) ? null : new BOSEAccount(name, this.BOSEconomy);
   }

   public Method.MethodBankAccount getBankAccount(String bank, String name) {
      return !this.hasBankAccount(bank, name) ? null : new BOSEBankAccount(bank, this.BOSEconomy);
   }

   public boolean isCompatible(Plugin plugin) {
      return plugin.getDescription().getName().equalsIgnoreCase("boseconomy") && plugin instanceof BOSEconomy && !plugin.getDescription().getVersion().equals("0.6.2");
   }

   public void setPlugin(Plugin plugin) {
      this.BOSEconomy = (BOSEconomy)plugin;
   }

   public class BOSEAccount implements Method.MethodAccount {
      private String name;
      private BOSEconomy BOSEconomy;

      public BOSEAccount(String name, BOSEconomy bOSEconomy) {
         this.name = name;
         this.BOSEconomy = bOSEconomy;
      }

      public double balance() {
         return this.BOSEconomy.getPlayerMoneyDouble(this.name);
      }

      public boolean set(double amount) {
         return this.BOSEconomy.setPlayerMoney(this.name, amount, false);
      }

      public boolean add(double amount) {
         return this.BOSEconomy.addPlayerMoney(this.name, amount, false);
      }

      public boolean subtract(double amount) {
         double balance = this.balance();
         return this.BOSEconomy.setPlayerMoney(this.name, balance - amount, false);
      }

      public boolean multiply(double amount) {
         double balance = this.balance();
         return this.BOSEconomy.setPlayerMoney(this.name, balance * amount, false);
      }

      public boolean divide(double amount) {
         double balance = this.balance();
         return this.BOSEconomy.setPlayerMoney(this.name, balance / amount, false);
      }

      public boolean hasEnough(double amount) {
         return this.balance() >= amount;
      }

      public boolean hasOver(double amount) {
         return this.balance() > amount;
      }

      public boolean hasUnder(double amount) {
         return this.balance() < amount;
      }

      public boolean isNegative() {
         return this.balance() < (double)0.0F;
      }

      public boolean remove() {
         return false;
      }
   }

   public class BOSEBankAccount implements Method.MethodBankAccount {
      private String bank;
      private BOSEconomy BOSEconomy;

      public BOSEBankAccount(String bank, BOSEconomy bOSEconomy) {
         this.bank = bank;
         this.BOSEconomy = bOSEconomy;
      }

      public String getBankName() {
         return this.bank;
      }

      public int getBankId() {
         return -1;
      }

      public double balance() {
         return this.BOSEconomy.getBankMoneyDouble(this.bank);
      }

      public boolean set(double amount) {
         return this.BOSEconomy.setBankMoney(this.bank, amount, true);
      }

      public boolean add(double amount) {
         double balance = this.balance();
         return this.BOSEconomy.setBankMoney(this.bank, balance + amount, false);
      }

      public boolean subtract(double amount) {
         double balance = this.balance();
         return this.BOSEconomy.setBankMoney(this.bank, balance - amount, false);
      }

      public boolean multiply(double amount) {
         double balance = this.balance();
         return this.BOSEconomy.setBankMoney(this.bank, balance * amount, false);
      }

      public boolean divide(double amount) {
         double balance = this.balance();
         return this.BOSEconomy.setBankMoney(this.bank, balance / amount, false);
      }

      public boolean hasEnough(double amount) {
         return this.balance() >= amount;
      }

      public boolean hasOver(double amount) {
         return this.balance() > amount;
      }

      public boolean hasUnder(double amount) {
         return this.balance() < amount;
      }

      public boolean isNegative() {
         return this.balance() < (double)0.0F;
      }

      public boolean remove() {
         return this.BOSEconomy.removeBank(this.bank);
      }
   }
}
