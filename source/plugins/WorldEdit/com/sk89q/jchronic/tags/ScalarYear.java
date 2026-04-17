package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.regex.Pattern;

public class ScalarYear extends Scalar {
   public static final Pattern YEAR_PATTERN = Pattern.compile("^([1-9]\\d)?\\d\\d?$");

   public ScalarYear(Integer type) {
      super(type);
   }

   public String toString() {
      return super.toString() + "-year-" + this.getType();
   }

   public static ScalarYear scan(Token token, Token postToken, Options options) {
      if (YEAR_PATTERN.matcher(token.getWord()).matches()) {
         int scalarValue = Integer.parseInt(token.getWord());
         if (postToken == null || !Scalar.TIMES.contains(postToken.getWord())) {
            if (scalarValue <= 37) {
               scalarValue += 2000;
            } else if (scalarValue <= 137 && scalarValue >= 69) {
               scalarValue += 1900;
            }

            return new ScalarYear(scalarValue);
         }
      }

      return null;
   }
}
