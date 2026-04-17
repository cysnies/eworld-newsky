package com.sk89q.util;

import java.util.Collection;
import java.util.Map;

public class StringUtil {
   public static String trimLength(String str, int len) {
      return str.length() > len ? str.substring(0, len) : str;
   }

   public static String joinString(String[] str, String delimiter, int initialIndex) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder(str[initialIndex]);

         for(int i = initialIndex + 1; i < str.length; ++i) {
            buffer.append(delimiter).append(str[i]);
         }

         return buffer.toString();
      }
   }

   public static String joinQuotedString(String[] str, String delimiter, int initialIndex, String quote) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder();
         buffer.append(quote);
         buffer.append(str[initialIndex]);
         buffer.append(quote);

         for(int i = initialIndex + 1; i < str.length; ++i) {
            buffer.append(delimiter).append(quote).append(str[i]).append(quote);
         }

         return buffer.toString();
      }
   }

   public static String joinString(String[] str, String delimiter) {
      return joinString((String[])str, delimiter, 0);
   }

   public static String joinString(Object[] str, String delimiter, int initialIndex) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder(str[initialIndex].toString());

         for(int i = initialIndex + 1; i < str.length; ++i) {
            buffer.append(delimiter).append(str[i].toString());
         }

         return buffer.toString();
      }
   }

   public static String joinString(int[] str, String delimiter, int initialIndex) {
      if (str.length == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder(Integer.toString(str[initialIndex]));

         for(int i = initialIndex + 1; i < str.length; ++i) {
            buffer.append(delimiter).append(Integer.toString(str[i]));
         }

         return buffer.toString();
      }
   }

   public static String joinString(Collection str, String delimiter, int initialIndex) {
      if (str.size() == 0) {
         return "";
      } else {
         StringBuilder buffer = new StringBuilder();
         int i = 0;

         for(Object o : str) {
            if (i >= initialIndex) {
               if (i > 0) {
                  buffer.append(delimiter);
               }

               buffer.append(o.toString());
            }

            ++i;
         }

         return buffer.toString();
      }
   }

   public static int getLevenshteinDistance(String s, String t) {
      if (s != null && t != null) {
         int n = s.length();
         int m = t.length();
         if (n == 0) {
            return m;
         } else if (m == 0) {
            return n;
         } else {
            int[] p = new int[n + 1];
            int[] d = new int[n + 1];

            for(int i = 0; i <= n; p[i] = i++) {
            }

            for(int j = 1; j <= m; ++j) {
               char t_j = t.charAt(j - 1);
               d[0] = j;

               for(int var11 = 1; var11 <= n; ++var11) {
                  int cost = s.charAt(var11 - 1) == t_j ? 0 : 1;
                  d[var11] = Math.min(Math.min(d[var11 - 1] + 1, p[var11] + 1), p[var11 - 1] + cost);
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

   public static Enum lookup(Map lookup, String name, boolean fuzzy) {
      String testName = name.replaceAll("[ _]", "").toLowerCase();
      T type = (T)((Enum)lookup.get(testName));
      if (type != null) {
         return type;
      } else if (!fuzzy) {
         return null;
      } else {
         int minDist = Integer.MAX_VALUE;

         for(Map.Entry entry : lookup.entrySet()) {
            String key = (String)entry.getKey();
            if (key.charAt(0) == testName.charAt(0)) {
               int dist = getLevenshteinDistance(key, testName);
               if (dist >= minDist) {
                  minDist = dist;
                  type = (T)((Enum)entry.getValue());
               }
            }
         }

         if (minDist > 1) {
            return null;
         } else {
            return type;
         }
      }
   }
}
