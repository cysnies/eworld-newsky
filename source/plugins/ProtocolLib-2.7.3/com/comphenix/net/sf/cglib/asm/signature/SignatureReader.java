package com.comphenix.net.sf.cglib.asm.signature;

public class SignatureReader {
   private final String a;

   public SignatureReader(String var1) {
      this.a = var1;
   }

   public void accept(SignatureVisitor var1) {
      String var2 = this.a;
      int var3 = var2.length();
      int var7;
      if (var2.charAt(0) == '<') {
         var7 = 2;

         char var11;
         do {
            int var5 = var2.indexOf(58, var7);
            var1.visitFormalTypeParameter(var2.substring(var7 - 1, var5));
            var7 = var5 + 1;
            var11 = var2.charAt(var7);
            if (var11 == 'L' || var11 == '[' || var11 == 'T') {
               var7 = a(var2, var7, var1.visitClassBound());
            }

            while((var11 = var2.charAt(var7++)) == ':') {
               var7 = a(var2, var7, var1.visitInterfaceBound());
            }
         } while(var11 != '>');
      } else {
         var7 = 0;
      }

      if (var2.charAt(var7) == '(') {
         ++var7;

         while(var2.charAt(var7) != ')') {
            var7 = a(var2, var7, var1.visitParameterType());
         }

         for(int var9 = a(var2, var7 + 1, var1.visitReturnType()); var9 < var3; var9 = a(var2, var9 + 1, var1.visitExceptionType())) {
         }
      } else {
         for(int var10 = a(var2, var7, var1.visitSuperclass()); var10 < var3; var10 = a(var2, var10, var1.visitInterface())) {
         }
      }

   }

   public void acceptType(SignatureVisitor var1) {
      a(this.a, 0, var1);
   }

   private static int a(String var0, int var1, SignatureVisitor var2) {
      char var3;
      switch (var3 = var0.charAt(var1++)) {
         case 'B':
         case 'C':
         case 'D':
         case 'F':
         case 'I':
         case 'J':
         case 'S':
         case 'V':
         case 'Z':
            var2.visitBaseType(var3);
            return var1;
         case 'E':
         case 'G':
         case 'H':
         case 'K':
         case 'L':
         case 'M':
         case 'N':
         case 'O':
         case 'P':
         case 'Q':
         case 'R':
         case 'U':
         case 'W':
         case 'X':
         case 'Y':
         default:
            int var5 = var1;
            boolean var6 = false;
            boolean var7 = false;

            label53:
            while(true) {
               switch (var3 = var0.charAt(var1++)) {
                  case '.':
                  case ';':
                     if (!var6) {
                        String var12 = var0.substring(var5, var1 - 1);
                        if (var7) {
                           var2.visitInnerClassType(var12);
                        } else {
                           var2.visitClassType(var12);
                        }
                     }

                     if (var3 == ';') {
                        var2.visitEnd();
                        return var1;
                     }

                     var5 = var1;
                     var6 = false;
                     var7 = true;
                     break;
                  case '<':
                     String var8 = var0.substring(var5, var1 - 1);
                     if (var7) {
                        var2.visitInnerClassType(var8);
                     } else {
                        var2.visitClassType(var8);
                     }

                     var6 = true;

                     while(true) {
                        switch (var3 = var0.charAt(var1)) {
                           case '*':
                              ++var1;
                              var2.visitTypeArgument();
                              break;
                           case '+':
                           case '-':
                              var1 = a(var0, var1 + 1, var2.visitTypeArgument(var3));
                              break;
                           case '>':
                              continue label53;
                           default:
                              var1 = a(var0, var1, var2.visitTypeArgument('='));
                        }
                     }
               }
            }
         case 'T':
            int var4 = var0.indexOf(59, var1);
            var2.visitTypeVariable(var0.substring(var1, var4));
            return var4 + 1;
         case '[':
            return a(var0, var1, var2.visitArrayType());
      }
   }
}
