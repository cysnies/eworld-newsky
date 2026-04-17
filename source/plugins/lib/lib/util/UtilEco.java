package lib.util;

import lib.Eco;
import lib.Lib;

public class UtilEco {
   private static Eco Eco;

   public static void init(Lib lib) {
      Eco = lib.getEco();
   }

   public static double get(String name) {
      return Eco.get(name);
   }

   public static boolean set(String name, double amount) {
      return Eco.set(name, amount);
   }

   public static boolean add(String name, double amount) {
      return Eco.add(name, amount);
   }

   public static boolean del(String name, double amount) {
      return Eco.del(name, amount);
   }
}
