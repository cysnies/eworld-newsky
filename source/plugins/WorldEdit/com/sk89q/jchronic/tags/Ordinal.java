package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Ordinal extends Tag {
   public static Pattern ORDINAL_PATTERN = Pattern.compile("^(\\d*)(st|nd|rd|th)$");

   public Ordinal(Integer type) {
      super(type);
   }

   public static List scan(List tokens, Options options) {
      for(Token token : tokens) {
         Ordinal t = scan(token, options);
         if (t != null) {
            token.tag(t);
         }

         Ordinal var5 = OrdinalDay.scan(token);
         if (var5 != null) {
            token.tag(var5);
         }
      }

      return tokens;
   }

   public static Ordinal scan(Token token, Options options) {
      Matcher ordinalMatcher = ORDINAL_PATTERN.matcher(token.getWord());
      return ordinalMatcher.find() ? new Ordinal(Integer.valueOf(ordinalMatcher.group(1))) : null;
   }

   public String toString() {
      return "ordinal";
   }
}
