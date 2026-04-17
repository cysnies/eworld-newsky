package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.utils.Token;
import java.util.regex.Matcher;

public class OrdinalDay extends Ordinal {
   public OrdinalDay(Integer type) {
      super(type);
   }

   public String toString() {
      return super.toString() + "-day-" + this.getType();
   }

   public static OrdinalDay scan(Token token) {
      Matcher ordinalMatcher = Ordinal.ORDINAL_PATTERN.matcher(token.getWord());
      if (ordinalMatcher.find()) {
         int ordinalValue = Integer.parseInt(ordinalMatcher.group(1));
         if (ordinalValue <= 31) {
            return new OrdinalDay(ordinalValue);
         }
      }

      return null;
   }
}
