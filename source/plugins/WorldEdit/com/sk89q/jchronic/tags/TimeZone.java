package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class TimeZone extends Tag {
   private static final Pattern TIMEZONE_PATTERN = Pattern.compile("[pmce][ds]t");
   public static final Object TZ = new Object();

   public TimeZone() {
      super((Object)null);
   }

   public static List scan(List tokens, Options options) {
      for(Token token : tokens) {
         TimeZone t = scanForAll(token, options);
         if (t != null) {
            token.tag(t);
         }
      }

      return tokens;
   }

   public static TimeZone scanForAll(Token token, Options options) {
      Map<Pattern, Object> scanner = new HashMap();
      scanner.put(TIMEZONE_PATTERN, (Object)null);

      for(Pattern scannerItem : scanner.keySet()) {
         if (scannerItem.matcher(token.getWord()).matches()) {
            return new TimeZone();
         }
      }

      return null;
   }

   public String toString() {
      return "timezone";
   }
}
