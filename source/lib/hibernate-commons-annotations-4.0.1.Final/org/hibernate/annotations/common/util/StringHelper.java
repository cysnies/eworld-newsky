package org.hibernate.annotations.common.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

public final class StringHelper {
   private static final int ALIAS_TRUNCATE_LENGTH = 10;
   public static final String WHITESPACE = " \n\r\f\t";

   private StringHelper() {
   }

   public static int lastIndexOfLetter(String string) {
      for(int i = 0; i < string.length(); ++i) {
         char character = string.charAt(i);
         if (!Character.isLetter(character)) {
            return i - 1;
         }
      }

      return string.length() - 1;
   }

   public static String join(String seperator, String[] strings) {
      int length = strings.length;
      if (length == 0) {
         return "";
      } else {
         StringBuffer buf = (new StringBuffer(length * strings[0].length())).append(strings[0]);

         for(int i = 1; i < length; ++i) {
            buf.append(seperator).append(strings[i]);
         }

         return buf.toString();
      }
   }

   public static String join(String seperator, Iterator objects) {
      StringBuffer buf = new StringBuffer();
      if (objects.hasNext()) {
         buf.append(objects.next());
      }

      while(objects.hasNext()) {
         buf.append(seperator).append(objects.next());
      }

      return buf.toString();
   }

   public static String[] add(String[] x, String sep, String[] y) {
      String[] result = new String[x.length];

      for(int i = 0; i < x.length; ++i) {
         result[i] = x[i] + sep + y[i];
      }

      return result;
   }

   public static String repeat(String string, int times) {
      StringBuffer buf = new StringBuffer(string.length() * times);

      for(int i = 0; i < times; ++i) {
         buf.append(string);
      }

      return buf.toString();
   }

   public static String repeat(char character, int times) {
      char[] buffer = new char[times];
      Arrays.fill(buffer, character);
      return new String(buffer);
   }

   public static String replace(String template, String placeholder, String replacement) {
      return replace(template, placeholder, replacement, false);
   }

   public static String[] replace(String[] templates, String placeholder, String replacement) {
      String[] result = new String[templates.length];

      for(int i = 0; i < templates.length; ++i) {
         result[i] = replace(templates[i], placeholder, replacement);
      }

      return result;
   }

   public static String replace(String template, String placeholder, String replacement, boolean wholeWords) {
      if (template == null) {
         return template;
      } else {
         int loc = template.indexOf(placeholder);
         if (loc < 0) {
            return template;
         } else {
            boolean actuallyReplace = !wholeWords || loc + placeholder.length() == template.length() || !Character.isJavaIdentifierPart(template.charAt(loc + placeholder.length()));
            String actualReplacement = actuallyReplace ? replacement : placeholder;
            return template.substring(0, loc) + actualReplacement + replace(template.substring(loc + placeholder.length()), placeholder, replacement, wholeWords);
         }
      }
   }

   public static String replaceOnce(String template, String placeholder, String replacement) {
      if (template == null) {
         return template;
      } else {
         int loc = template.indexOf(placeholder);
         return loc < 0 ? template : template.substring(0, loc) + replacement + template.substring(loc + placeholder.length());
      }
   }

   public static String[] split(String seperators, String list) {
      return split(seperators, list, false);
   }

   public static String[] split(String seperators, String list, boolean include) {
      StringTokenizer tokens = new StringTokenizer(list, seperators, include);
      String[] result = new String[tokens.countTokens()];

      for(int i = 0; tokens.hasMoreTokens(); result[i++] = tokens.nextToken()) {
      }

      return result;
   }

   public static String unqualify(String qualifiedName) {
      int loc = qualifiedName.lastIndexOf(".");
      return loc < 0 ? qualifiedName : qualifiedName.substring(qualifiedName.lastIndexOf(".") + 1);
   }

   public static String qualifier(String qualifiedName) {
      int loc = qualifiedName.lastIndexOf(".");
      return loc < 0 ? "" : qualifiedName.substring(0, loc);
   }

   public static String collapse(String name) {
      if (name == null) {
         return null;
      } else {
         int breakPoint = name.lastIndexOf(46);
         return breakPoint < 0 ? name : collapseQualifier(name.substring(0, breakPoint), true) + name.substring(breakPoint);
      }
   }

   public static String collapseQualifier(String qualifier, boolean includeDots) {
      StringTokenizer tokenizer = new StringTokenizer(qualifier, ".");

      String collapsed;
      for(collapsed = Character.toString(tokenizer.nextToken().charAt(0)); tokenizer.hasMoreTokens(); collapsed = collapsed + tokenizer.nextToken().charAt(0)) {
         if (includeDots) {
            collapsed = collapsed + '.';
         }
      }

      return collapsed;
   }

   public static String partiallyUnqualify(String name, String qualifierBase) {
      return name != null && name.startsWith(qualifierBase) ? name.substring(qualifierBase.length() + 1) : name;
   }

   public static String collapseQualifierBase(String name, String qualifierBase) {
      return name != null && name.startsWith(qualifierBase) ? collapseQualifier(qualifierBase, true) + name.substring(qualifierBase.length()) : collapse(name);
   }

   public static String[] suffix(String[] columns, String suffix) {
      if (suffix == null) {
         return columns;
      } else {
         String[] qualified = new String[columns.length];

         for(int i = 0; i < columns.length; ++i) {
            qualified[i] = suffix(columns[i], suffix);
         }

         return qualified;
      }
   }

   private static String suffix(String name, String suffix) {
      return suffix == null ? name : name + suffix;
   }

   public static String root(String qualifiedName) {
      int loc = qualifiedName.indexOf(".");
      return loc < 0 ? qualifiedName : qualifiedName.substring(0, loc);
   }

   public static String unroot(String qualifiedName) {
      int loc = qualifiedName.indexOf(".");
      return loc < 0 ? qualifiedName : qualifiedName.substring(loc + 1, qualifiedName.length());
   }

   public static boolean booleanValue(String tfString) {
      String trimmed = tfString.trim().toLowerCase();
      return trimmed.equals("true") || trimmed.equals("t");
   }

   public static String toString(Object[] array) {
      int len = array.length;
      if (len == 0) {
         return "";
      } else {
         StringBuffer buf = new StringBuffer(len * 12);

         for(int i = 0; i < len - 1; ++i) {
            buf.append(array[i]).append(", ");
         }

         return buf.append(array[len - 1]).toString();
      }
   }

   public static String[] multiply(String string, Iterator placeholders, Iterator replacements) {
      String[] result;
      for(result = new String[]{string}; placeholders.hasNext(); result = multiply(result, (String)placeholders.next(), (String[])replacements.next())) {
      }

      return result;
   }

   private static String[] multiply(String[] strings, String placeholder, String[] replacements) {
      String[] results = new String[replacements.length * strings.length];
      int n = 0;

      for(int i = 0; i < replacements.length; ++i) {
         for(int j = 0; j < strings.length; ++j) {
            results[n++] = replaceOnce(strings[j], placeholder, replacements[i]);
         }
      }

      return results;
   }

   public static int countUnquoted(String string, char character) {
      if ('\'' == character) {
         throw new IllegalArgumentException("Unquoted count of quotes is invalid");
      } else if (string == null) {
         return 0;
      } else {
         int count = 0;
         int stringLength = string.length();
         boolean inQuote = false;

         for(int indx = 0; indx < stringLength; ++indx) {
            char c = string.charAt(indx);
            if (inQuote) {
               if ('\'' == c) {
                  inQuote = false;
               }
            } else if ('\'' == c) {
               inQuote = true;
            } else if (c == character) {
               ++count;
            }
         }

         return count;
      }
   }

   public static boolean isNotEmpty(String string) {
      return string != null && string.length() > 0;
   }

   public static boolean isEmpty(String string) {
      return string == null || string.length() == 0;
   }

   public static String qualify(String prefix, String name) {
      if (name != null && prefix != null) {
         return (new StringBuffer(prefix.length() + name.length() + 1)).append(prefix).append('.').append(name).toString();
      } else {
         throw new NullPointerException();
      }
   }

   public static String[] qualify(String prefix, String[] names) {
      if (prefix == null) {
         return names;
      } else {
         int len = names.length;
         String[] qualified = new String[len];

         for(int i = 0; i < len; ++i) {
            qualified[i] = qualify(prefix, names[i]);
         }

         return qualified;
      }
   }

   public static int firstIndexOfChar(String sqlString, String string, int startindex) {
      int matchAt = -1;

      for(int i = 0; i < string.length(); ++i) {
         int curMatch = sqlString.indexOf(string.charAt(i), startindex);
         if (curMatch >= 0) {
            if (matchAt == -1) {
               matchAt = curMatch;
            } else {
               matchAt = Math.min(matchAt, curMatch);
            }
         }
      }

      return matchAt;
   }

   public static String truncate(String string, int length) {
      return string.length() <= length ? string : string.substring(0, length);
   }

   public static String generateAlias(String description) {
      return generateAliasRoot(description) + '_';
   }

   public static String generateAlias(String description, int unique) {
      return generateAliasRoot(description) + Integer.toString(unique) + '_';
   }

   private static String generateAliasRoot(String description) {
      String result = truncate(unqualifyEntityName(description), 10).toLowerCase().replace('/', '_').replace('$', '_');
      result = cleanAlias(result);
      return Character.isDigit(result.charAt(result.length() - 1)) ? result + "x" : result;
   }

   private static String cleanAlias(String alias) {
      char[] chars = alias.toCharArray();
      if (!Character.isLetter(chars[0])) {
         for(int i = 1; i < chars.length; ++i) {
            if (Character.isLetter(chars[i])) {
               return alias.substring(i);
            }
         }
      }

      return alias;
   }

   public static String unqualifyEntityName(String entityName) {
      String result = unqualify(entityName);
      int slashPos = result.indexOf(47);
      if (slashPos > 0) {
         result = result.substring(0, slashPos - 1);
      }

      return result;
   }

   public static String toUpperCase(String str) {
      return str == null ? null : str.toUpperCase();
   }

   public static String toLowerCase(String str) {
      return str == null ? null : str.toLowerCase();
   }

   public static String moveAndToBeginning(String filter) {
      if (filter.trim().length() > 0) {
         filter = filter + " and ";
         if (filter.startsWith(" and ")) {
            filter = filter.substring(4);
         }
      }

      return filter;
   }

   public static boolean isQuoted(String name) {
      return name != null && name.length() != 0 && name.charAt(0) == '`' && name.charAt(name.length() - 1) == '`';
   }

   public static String quote(String name) {
      return name != null && name.length() != 0 && !isQuoted(name) ? (new StringBuffer(name.length() + 2)).append('`').append(name).append('`').toString() : name;
   }

   public static String unquote(String name) {
      return isQuoted(name) ? name.substring(1, name.length() - 1) : name;
   }
}
