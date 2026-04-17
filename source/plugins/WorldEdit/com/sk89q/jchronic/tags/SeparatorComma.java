package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SeparatorComma extends Separator {
   private static final Pattern COMMA_PATTERN = Pattern.compile("^,$");

   public SeparatorComma(Separator.SeparatorType type) {
      super(type);
   }

   public String toString() {
      return super.toString() + "-comma";
   }

   public static SeparatorComma scan(Token token, Options options) {
      Map<Pattern, Separator.SeparatorType> scanner = new HashMap();
      scanner.put(COMMA_PATTERN, Separator.SeparatorType.COMMA);

      for(Pattern scannerItem : scanner.keySet()) {
         if (scannerItem.matcher(token.getWord()).matches()) {
            return new SeparatorComma((Separator.SeparatorType)scanner.get(scannerItem));
         }
      }

      return null;
   }
}
