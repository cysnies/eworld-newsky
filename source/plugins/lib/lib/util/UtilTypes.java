package lib.util;

import lib.Lib;
import lib.types.InvalidTypeException;
import lib.types.Types;
import org.bukkit.configuration.file.FileConfiguration;

public class UtilTypes {
   private static Types Types;

   public static void init(Lib lib) {
      Types = lib.getTypes();
   }

   public static void reloadTypes(String plugin, FileConfiguration config) {
      Types.reloadTypes(plugin, config);
   }

   public static boolean checkCmd(String plugin, String type, String cmd) throws InvalidTypeException {
      return Types.checkCmd(plugin, type, cmd);
   }

   public static boolean checkEntity(String plugin, String type, String s) throws InvalidTypeException {
      return Types.checkEntity(plugin, type, s);
   }

   public static boolean checkItem(String plugin, String type, String s) throws InvalidTypeException {
      return Types.checkItem(plugin, type, s);
   }
}
