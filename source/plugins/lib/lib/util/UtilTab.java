package lib.util;

import java.util.HashMap;
import lib.tab.Tab;

public class UtilTab {
   public static void register(String mode) {
      Tab.register(mode);
   }

   public static HashMap getMode(String mode) {
      return Tab.getMode(mode);
   }
}
