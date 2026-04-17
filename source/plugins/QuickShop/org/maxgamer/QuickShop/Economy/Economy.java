package org.maxgamer.QuickShop.Economy;

public class Economy implements EconomyCore {
   private EconomyCore core;

   public Economy(EconomyCore core) {
      this.core = core;
   }

   public boolean isValid() {
      return this.core.isValid();
   }

   public boolean deposit(String name, double amount) {
      return this.core.deposit(name, amount);
   }

   public boolean withdraw(String name, double amount) {
      return this.core.withdraw(name, amount);
   }

   public boolean transfer(String from, String to, double amount) {
      return this.core.transfer(from, to, amount);
   }

   public double getBalance(String name) {
      return this.core.getBalance(name);
   }

   public String format(double balance) {
      return this.core.format(balance);
   }

   public boolean has(String name, double amount) {
      return this.core.getBalance(name) >= amount;
   }

   public String toString() {
      return this.core.getClass().getName().split("_")[1];
   }
}
