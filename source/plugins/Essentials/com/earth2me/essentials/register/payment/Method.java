package com.earth2me.essentials.register.payment;

import org.bukkit.plugin.Plugin;

public interface Method {
   Object getPlugin();

   String getName();

   String getLongName();

   String getVersion();

   int fractionalDigits();

   String format(double var1);

   boolean hasBanks();

   boolean hasBank(String var1);

   boolean hasAccount(String var1);

   boolean hasBankAccount(String var1, String var2);

   boolean createAccount(String var1);

   boolean createAccount(String var1, Double var2);

   MethodAccount getAccount(String var1);

   MethodBankAccount getBankAccount(String var1, String var2);

   boolean isCompatible(Plugin var1);

   void setPlugin(Plugin var1);

   public interface MethodAccount {
      double balance();

      boolean set(double var1);

      boolean add(double var1);

      boolean subtract(double var1);

      boolean multiply(double var1);

      boolean divide(double var1);

      boolean hasEnough(double var1);

      boolean hasOver(double var1);

      boolean hasUnder(double var1);

      boolean isNegative();

      boolean remove();

      String toString();
   }

   public interface MethodBankAccount {
      double balance();

      String getBankName();

      int getBankId();

      boolean set(double var1);

      boolean add(double var1);

      boolean subtract(double var1);

      boolean multiply(double var1);

      boolean divide(double var1);

      boolean hasEnough(double var1);

      boolean hasOver(double var1);

      boolean hasUnder(double var1);

      boolean isNegative();

      boolean remove();

      String toString();
   }
}
