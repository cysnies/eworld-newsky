package com.sk89q.worldedit;

public class Countable implements Comparable {
   private Object id;
   private int amount;

   public Countable(Object id, int amount) {
      this.id = id;
      this.amount = amount;
   }

   public Object getID() {
      return this.id;
   }

   public void setID(Object id) {
      this.id = id;
   }

   public int getAmount() {
      return this.amount;
   }

   public void setAmount(int amount) {
      this.amount = amount;
   }

   public void decrement() {
      --this.amount;
   }

   public void increment() {
      ++this.amount;
   }

   public int compareTo(Countable other) {
      if (this.amount > other.amount) {
         return 1;
      } else {
         return this.amount == other.amount ? 0 : -1;
      }
   }
}
