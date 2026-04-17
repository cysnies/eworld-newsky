package fr.neatmonster.nocheatplus.utilities;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class StringUtil {
   public static final DecimalFormat fdec3 = new DecimalFormat();
   public static final DecimalFormat fdec1 = new DecimalFormat();

   public static String join(Collection input, String link) {
      StringBuilder builder = new StringBuilder(Math.max(300, input.size() * 10));
      boolean first = true;

      for(Object obj : input) {
         if (!first) {
            builder.append(link);
         }

         builder.append(obj.toString());
         first = false;
      }

      return builder.toString();
   }

   public static List split(String input, Character... chars) {
      List<String> out = new LinkedList();
      out.add(input);
      List<String> queue = new LinkedList();
      Character[] arr$ = chars;
      int len$ = chars.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         char c = arr$[i$];
         String hex = Integer.toHexString(c);
         switch (hex.length()) {
            case 1:
               hex = "000" + hex;
               break;
            case 2:
               hex = "00" + hex;
               break;
            case 3:
               hex = "0" + hex;
         }

         for(String s : out) {
            String[] split = s.split("\\u" + hex);

            for(String _s : split) {
               queue.add(_s);
            }
         }

         List<String> temp = out;
         out = queue;
         queue = temp;
         temp.clear();
      }

      return out;
   }

   public static boolean isSimilar(String s, String t, float threshold) {
      return (double)1.0F - (double)((float)levenshteinDistance(s, t)) / Math.max((double)1.0F, (double)Math.max(s.length(), t.length())) > (double)threshold;
   }

   public static int levenshteinDistance(CharSequence s, CharSequence t) {
      if (s != null && t != null) {
         int n = s.length();
         int m = t.length();
         if (n == 0) {
            return m;
         } else if (m == 0) {
            return n;
         } else {
            if (n > m) {
               CharSequence tmp = s;
               s = t;
               t = tmp;
               n = m;
               m = tmp.length();
            }

            int[] p = new int[n + 1];
            int[] d = new int[n + 1];

            for(int i = 0; i <= n; p[i] = i++) {
            }

            for(int j = 1; j <= m; ++j) {
               char t_j = t.charAt(j - 1);
               d[0] = j;

               for(int var12 = 1; var12 <= n; ++var12) {
                  int cost = s.charAt(var12 - 1) == t_j ? 0 : 1;
                  d[var12] = Math.min(Math.min(d[var12 - 1] + 1, p[var12] + 1), p[var12 - 1] + cost);
               }

               int[] _d = p;
               p = d;
               d = _d;
            }

            return p[n];
         }
      } else {
         throw new IllegalArgumentException("Strings must not be null");
      }
   }

   static {
      DecimalFormatSymbols sym = fdec3.getDecimalFormatSymbols();
      sym.setDecimalSeparator('.');
      fdec3.setDecimalFormatSymbols(sym);
      fdec3.setMaximumFractionDigits(3);
      fdec3.setMinimumIntegerDigits(1);
      sym = fdec1.getDecimalFormatSymbols();
      sym.setDecimalSeparator('.');
      fdec1.setDecimalFormatSymbols(sym);
      fdec1.setMaximumFractionDigits(1);
      fdec1.setMinimumIntegerDigits(1);
   }
}
