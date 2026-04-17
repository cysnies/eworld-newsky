package org.maxgamer.QuickShop.Economy;

public interface EconomyCore {
   boolean isValid();

   boolean deposit(String var1, double var2);

   boolean withdraw(String var1, double var2);

   boolean transfer(String var1, String var2, double var3);

   double getBalance(String var1);

   String format(double var1);
}
