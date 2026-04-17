package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SeparatorSlashOrDash extends Separator {
   private static final Pattern SLASH_PATTERN = Pattern.compile("^/$");
   private static final Pattern DASH_PATTERN = Pattern.compile("^-$");

   public SeparatorSlashOrDash(Separator.SeparatorType type) {
      super(type);
   }

   public String toString() {
      return super.toString() + "-slashordash-" + this.getType();
   }

   public static SeparatorSlashOrDash scan(Token token, Options options) {
      Map<Pattern, Separator.SeparatorType> scanner = new HashMap();
      scanner.put(DASH_PATTERN, Separator.SeparatorType.DASH);
      scanner.put(SLASH_PATTERN, Separator.SeparatorType.SLASH);

      for(Pattern scannerItem : scanner.keySet()) {
         if (scannerItem.matcher(token.getWord()).matches()) {
            return new SeparatorSlashOrDash((Separator.SeparatorType)scanner.get(scannerItem));
         }
      }

      return null;
   }
}
