package org.ibex.nestedvm.util;

public final class Sort {
   private static final CompareFunc comparableCompareFunc = new CompareFunc() {
      public int compare(Object var1, Object var2) {
         return ((Comparable)var1).compareTo(var2);
      }
   };

   private Sort() {
   }

   public static void sort(Comparable[] var0) {
      sort(var0, comparableCompareFunc);
   }

   public static void sort(Object[] var0, CompareFunc var1) {
      sort(var0, var1, 0, var0.length - 1);
   }

   private static void sort(Object[] var0, CompareFunc var1, int var2, int var3) {
      if (var2 < var3) {
         if (var3 - var2 <= 6) {
            for(int var10 = var2 + 1; var10 <= var3; ++var10) {
               Object var9 = var0[var10];

               int var11;
               for(var11 = var10 - 1; var11 >= var2 && var1.compare(var0[var11], var9) > 0; --var11) {
                  var0[var11 + 1] = var0[var11];
               }

               var0[var11 + 1] = var9;
            }

         } else {
            Object var5 = var0[var3];
            int var6 = var2 - 1;
            int var7 = var3;

            do {
               while(var6 < var7) {
                  ++var6;
                  if (var1.compare(var0[var6], var5) >= 0) {
                     break;
                  }
               }

               while(var7 > var6) {
                  --var7;
                  if (var1.compare(var0[var7], var5) <= 0) {
                     break;
                  }
               }

               Object var4 = var0[var6];
               var0[var6] = var0[var7];
               var0[var7] = var4;
            } while(var6 < var7);

            Object var8 = var0[var6];
            var0[var6] = var0[var3];
            var0[var3] = var8;
            sort(var0, var1, var2, var6 - 1);
            sort(var0, var1, var6 + 1, var3);
         }
      }
   }

   public interface Comparable {
      int compareTo(Object var1);
   }

   public interface CompareFunc {
      int compare(Object var1, Object var2);
   }
}
