package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Grabber extends Tag {
   private static final Pattern THIS_PATTERN = Pattern.compile("this");
   private static final Pattern NEXT_PATTERN = Pattern.compile("next");
   private static final Pattern LAST_PATTERN = Pattern.compile("last");

   public Grabber(Relative type) {
      super(type);
   }

   public static List scan(List tokens, Options options) {
      for(Token token : tokens) {
         Grabber t = scanForAll(token, options);
         if (t != null) {
            token.tag(t);
         }
      }

      return tokens;
   }

   public static Grabber scanForAll(Token token, Options options) {
      Map<Pattern, Relative> scanner = new HashMap();
      scanner.put(LAST_PATTERN, Grabber.Relative.LAST);
      scanner.put(NEXT_PATTERN, Grabber.Relative.NEXT);
      scanner.put(THIS_PATTERN, Grabber.Relative.THIS);

      for(Pattern scannerItem : scanner.keySet()) {
         if (scannerItem.matcher(token.getWord()).matches()) {
            return new Grabber((Relative)scanner.get(scannerItem));
         }
      }

      return null;
   }

   public String toString() {
      return "grabber-" + this.getType();
   }

   public static enum Relative {
      LAST,
      NEXT,
      THIS;
   }
}
