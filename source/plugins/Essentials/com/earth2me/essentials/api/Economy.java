package com.earth2me.essentials.api;

import com.earth2me.essentials.EssentialsConf;
import com.earth2me.essentials.I18n;
import com.earth2me.essentials.IEssentials;
import com.earth2me.essentials.User;
import com.earth2me.essentials.Util;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Economy {
   private static final Logger logger = Logger.getLogger("Minecraft");
   private static IEssentials ess;
   private static final String noCallBeforeLoad = "Essentials API is called before Essentials is loaded.";

   private Economy() {
   }

   public static void setEss(IEssentials aEss) {
      ess = aEss;
   }

   private static void createNPCFile(String name) {
      File folder = new File(ess.getDataFolder(), "userdata");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      EssentialsConf npcConfig = new EssentialsConf(new File(folder, Util.sanitizeFileName(name) + ".yml"));
      npcConfig.load();
      npcConfig.setProperty("npc", (Object)true);
      npcConfig.setProperty("money", (Object)ess.getSettings().getStartingBalance());
      npcConfig.save();
   }

   private static void deleteNPC(String name) {
      File folder = new File(ess.getDataFolder(), "userdata");
      if (!folder.exists()) {
         folder.mkdirs();
      }

      File config = new File(folder, Util.sanitizeFileName(name) + ".yml");
      EssentialsConf npcConfig = new EssentialsConf(config);
      npcConfig.load();
      if (npcConfig.hasProperty("npc") && npcConfig.getBoolean("npc", false)) {
         if (!config.delete()) {
            logger.log(Level.WARNING, I18n._("deleteFileError", config));
         }

         ess.getUserMap().removeUser(name);
      }

   }

   private static User getUserByName(String name) {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         return ess.getUser(name);
      }
   }

   public static double getMoney(String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return user.getMoney();
      }
   }

   public static void setMoney(String name, double balance) throws UserDoesNotExistException, NoLoanPermittedException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else if (balance < ess.getSettings().getMinMoney()) {
         throw new NoLoanPermittedException();
      } else if (balance < (double)0.0F && !user.isAuthorized("essentials.eco.loan")) {
         throw new NoLoanPermittedException();
      } else {
         user.setMoney(balance);
      }
   }

   public static void add(String name, double amount) throws UserDoesNotExistException, NoLoanPermittedException {
      double result = getMoney(name) + amount;
      setMoney(name, result);
   }

   public static void subtract(String name, double amount) throws UserDoesNotExistException, NoLoanPermittedException {
      double result = getMoney(name) - amount;
      setMoney(name, result);
   }

   public static void divide(String name, double value) throws UserDoesNotExistException, NoLoanPermittedException {
      double result = getMoney(name) / value;
      setMoney(name, result);
   }

   public static void multiply(String name, double value) throws UserDoesNotExistException, NoLoanPermittedException {
      double result = getMoney(name) * value;
      setMoney(name, result);
   }

   public static void resetBalance(String name) throws UserDoesNotExistException, NoLoanPermittedException {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         setMoney(name, (double)ess.getSettings().getStartingBalance());
      }
   }

   public static boolean hasEnough(String name, double amount) throws UserDoesNotExistException {
      return amount <= getMoney(name);
   }

   public static boolean hasMore(String name, double amount) throws UserDoesNotExistException {
      return amount < getMoney(name);
   }

   public static boolean hasLess(String name, double amount) throws UserDoesNotExistException {
      return amount > getMoney(name);
   }

   public static boolean isNegative(String name) throws UserDoesNotExistException {
      return getMoney(name) < (double)0.0F;
   }

   public static String format(double amount) {
      if (ess == null) {
         throw new RuntimeException("Essentials API is called before Essentials is loaded.");
      } else {
         return Util.displayCurrency(amount, ess);
      }
   }

   public static boolean playerExists(String name) {
      return getUserByName(name) != null;
   }

   public static boolean isNPC(String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         return user.isNPC();
      }
   }

   public static boolean createNPC(String name) {
      User user = getUserByName(name);
      if (user == null) {
         createNPCFile(name);
         return true;
      } else {
         return false;
      }
   }

   public static void removeNPC(String name) throws UserDoesNotExistException {
      User user = getUserByName(name);
      if (user == null) {
         throw new UserDoesNotExistException(name);
      } else {
         deleteNPC(name);
      }
   }
}
