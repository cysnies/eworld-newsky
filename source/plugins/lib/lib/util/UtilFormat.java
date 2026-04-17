package lib.util;

import lib.Format;
import lib.Lib;

public class UtilFormat {
   private static Format Format;

   public static void init(Lib lib) {
      Format = lib.getFormat();
   }

   public static String format(String pn, String type, Object... args) {
      return Format.format(pn, type, args);
   }

   public static String format(String pluginName, int id) {
      return Format.format(pluginName, id);
   }
}
