package com.goncalomb.bukkit;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
   private Utils() {
   }

   public static String[] split(String str, SplitType type) {
      String[] result = str.trim().split(type._regex);
      return result.length == 1 && result[0].isEmpty() ? new String[0] : result;
   }

   public static int parseInt(String str, int defaultValue) {
      try {
         return Integer.parseInt(str);
      } catch (NumberFormatException var3) {
         return defaultValue;
      }
   }

   public static int parseInt(String str, int max, int min, int defaultValue) {
      int value = parseInt(str, defaultValue);
      return value <= max && value >= min ? value : defaultValue;
   }

   public static int parseTimeDuration(String str) {
      Matcher matcher = Pattern.compile("^(?:(\\d{1,4})d)?(?:(\\d{1,2})h)?(?:(\\d{1,2})m)?(?:(\\d{1,2})s)?$", 2).matcher(str);
      if (matcher.find()) {
         int d = parseInt(matcher.group(1), 0);
         int h = parseInt(matcher.group(2), 0);
         int m = parseInt(matcher.group(3), 0);
         int s = parseInt(matcher.group(4), 0);
         if (h < 24 && m < 60 && s < 60) {
            return d * 86400 + h * 3600 + m * 60 + s;
         }
      }

      return -1;
   }

   public static enum SplitType {
      WHITE_SPACES("\\s+"),
      COMMAS("\\s*,\\s*");

      String _regex;

      private SplitType(String regex) {
         this._regex = regex;
      }
   }
}
