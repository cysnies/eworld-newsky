package com.sk89q.jchronic.tags;

import com.sk89q.jchronic.Options;
import com.sk89q.jchronic.utils.Token;
import java.util.List;

public class Separator extends Tag {
   public Separator(SeparatorType type) {
      super(type);
   }

   public static List scan(List tokens, Options options) {
      for(Token token : tokens) {
         Separator t = SeparatorComma.scan(token, options);
         if (t != null) {
            token.tag(t);
         }

         Separator var5 = SeparatorSlashOrDash.scan(token, options);
         if (var5 != null) {
            token.tag(var5);
         }

         Separator var6 = SeparatorAt.scan(token, options);
         if (var6 != null) {
            token.tag(var6);
         }

         Separator var7 = SeparatorIn.scan(token, options);
         if (var7 != null) {
            token.tag(var7);
         }
      }

      return tokens;
   }

   public String toString() {
      return "separator";
   }

   public static enum SeparatorType {
      COMMA,
      DASH,
      SLASH,
      AT,
      NEWLINE,
      IN;
   }
}
