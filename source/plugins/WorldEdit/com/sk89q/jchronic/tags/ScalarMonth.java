package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.regex.Pattern;

public class ScalarMonth extends Scalar {
   private static final Pattern MONTH_PATTERN = Pattern.compile("^\\d\\d?$");

   public ScalarMonth(Integer type) {
      super(type);
   }

   public String toString() {
      return super.toString() + "-month-" + this.getType();
   }

   public static ScalarMonth scan(Token token, Token postToken, Options options) {
      if (MONTH_PATTERN.matcher(token.getWord()).matches()) {
         int scalarValue = Integer.parseInt(token.getWord());
         if (scalarValue <= 12 && (postToken == null || !Scalar.TIMES.contains(postToken.getWord()))) {
            return new ScalarMonth(scalarValue);
         }
      }

      return null;
   }
}
