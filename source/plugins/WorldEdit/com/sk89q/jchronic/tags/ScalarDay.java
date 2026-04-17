package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.regex.Pattern;

public class ScalarDay extends Scalar {
   private static final Pattern DAY_PATTERN = Pattern.compile("^\\d\\d?$");

   public ScalarDay(Integer type) {
      super(type);
   }

   public String toString() {
      return super.toString() + "-day-" + this.getType();
   }

   public static ScalarDay scan(Token token, Token postToken, Options options) {
      if (DAY_PATTERN.matcher(token.getWord()).matches()) {
         int scalarValue = Integer.parseInt(token.getWord());
         if (scalarValue <= 31 && (postToken == null || !Scalar.TIMES.contains(postToken.getWord()))) {
            return new ScalarDay(scalarValue);
         }
      }

      return null;
   }
}
