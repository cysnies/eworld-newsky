package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.StringUtils;
import com.sk89q.jchronic.utils.Token;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class Scalar extends Tag {
   private static final Pattern SCALAR_PATTERN = Pattern.compile("^\\d*$");
   public static Set TIMES = new HashSet();

   static {
      TIMES.add("am");
      TIMES.add("pm");
      TIMES.add("morning");
      TIMES.add("afternoon");
      TIMES.add("evening");
      TIMES.add("night");
   }

   public Scalar(Integer type) {
      super(type);
   }

   public static List scan(List tokens, Options options) {
      for(int i = 0; i < tokens.size(); ++i) {
         Token token = (Token)tokens.get(i);
         Token postToken = null;
         if (i < tokens.size() - 1) {
            postToken = (Token)tokens.get(i + 1);
         }

         Scalar t = scan(token, postToken, options);
         if (t != null) {
            token.tag(t);
         }

         Scalar var6 = ScalarDay.scan(token, postToken, options);
         if (var6 != null) {
            token.tag(var6);
         }

         Scalar var7 = ScalarMonth.scan(token, postToken, options);
         if (var7 != null) {
            token.tag(var7);
         }

         Scalar var8 = ScalarYear.scan(token, postToken, options);
         if (var8 != null) {
            token.tag(var8);
         }
      }

      return tokens;
   }

   public static Scalar scan(Token token, Token postToken, Options options) {
      if (SCALAR_PATTERN.matcher(token.getWord()).matches()) {
         if (token.getWord() != null && token.getWord().length() > 0 && (postToken == null || !TIMES.contains(postToken.getWord()))) {
            return new Scalar(Integer.valueOf(token.getWord()));
         }
      } else {
         Integer intStrValue = StringUtils.integerValue(token.getWord());
         if (intStrValue != null) {
            return new Scalar(intStrValue);
         }
      }

      return null;
   }

   public String toString() {
      return "scalar";
   }
}
