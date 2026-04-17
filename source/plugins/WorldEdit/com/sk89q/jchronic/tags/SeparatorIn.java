package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SeparatorIn extends Separator {
   private static final Pattern IN_PATTERN = Pattern.compile("^in$");

   public SeparatorIn(Separator.SeparatorType type) {
      super(type);
   }

   public String toString() {
      return super.toString() + "-in";
   }

   public static SeparatorIn scan(Token token, Options options) {
      Map<Pattern, Separator.SeparatorType> scanner = new HashMap();
      scanner.put(IN_PATTERN, Separator.SeparatorType.IN);

      for(Pattern scannerItem : scanner.keySet()) {
         if (scannerItem.matcher(token.getWord()).matches()) {
            return new SeparatorIn((Separator.SeparatorType)scanner.get(scannerItem));
         }
      }

      return null;
   }
}
