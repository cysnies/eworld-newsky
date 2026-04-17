package org.hibernate.engine.query.spi;

import org.hibernate.QueryException;
import org.hibernate.internal.util.StringHelper;

public class ParameterParser {
   private ParameterParser() {
   }

   public static void parse(String sqlString, Recognizer recognizer) throws QueryException {
      boolean hasMainOutputParameter = startsWithEscapeCallTemplate(sqlString);
      boolean foundMainOutputParam = false;
      int stringLength = sqlString.length();
      boolean inQuote = false;

      for(int indx = 0; indx < stringLength; ++indx) {
         char c = sqlString.charAt(indx);
         if (inQuote) {
            if ('\'' == c) {
               inQuote = false;
            }

            recognizer.other(c);
         } else if ('\'' == c) {
            inQuote = true;
            recognizer.other(c);
         } else if ('\\' == c) {
            ++indx;
            recognizer.other(sqlString.charAt(indx));
         } else if (c == ':') {
            int right = StringHelper.firstIndexOfChar(sqlString, " \n\r\f\t,()=<>&|+-=/*'^![]#~\\", indx + 1);
            int chopLocation = right < 0 ? sqlString.length() : right;
            String param = sqlString.substring(indx + 1, chopLocation);
            if (StringHelper.isEmpty(param)) {
               throw new QueryException("Space is not allowed after parameter prefix ':' [" + sqlString + "]");
            }

            recognizer.namedParameter(param, indx);
            indx = chopLocation - 1;
         } else if (c == '?') {
            if (indx < stringLength - 1 && Character.isDigit(sqlString.charAt(indx + 1))) {
               int right = StringHelper.firstIndexOfChar(sqlString, " \n\r\f\t,()=<>&|+-=/*'^![]#~\\", indx + 1);
               int chopLocation = right < 0 ? sqlString.length() : right;
               String param = sqlString.substring(indx + 1, chopLocation);

               try {
                  Integer.valueOf(param);
               } catch (NumberFormatException var12) {
                  throw new QueryException("JPA-style positional param was not an integral ordinal");
               }

               recognizer.jpaPositionalParameter(param, indx);
               indx = chopLocation - 1;
            } else if (hasMainOutputParameter && !foundMainOutputParam) {
               foundMainOutputParam = true;
               recognizer.outParameter(indx);
            } else {
               recognizer.ordinalParameter(indx);
            }
         } else {
            recognizer.other(c);
         }
      }

   }

   public static boolean startsWithEscapeCallTemplate(String sqlString) {
      if (sqlString.startsWith("{") && sqlString.endsWith("}")) {
         int chopLocation = sqlString.indexOf("call");
         if (chopLocation <= 0) {
            return false;
         } else {
            String checkString = sqlString.substring(1, chopLocation + 4);
            String fixture = "?=call";
            int fixturePosition = 0;
            boolean matches = true;
            int i = 0;

            for(int max = checkString.length(); i < max; ++i) {
               char c = Character.toLowerCase(checkString.charAt(i));
               if (!Character.isWhitespace(c)) {
                  if (c != "?=call".charAt(fixturePosition)) {
                     matches = false;
                     break;
                  }

                  ++fixturePosition;
               }
            }

            return matches;
         }
      } else {
         return false;
      }
   }

   public interface Recognizer {
      void outParameter(int var1);

      void ordinalParameter(int var1);

      void namedParameter(String var1, int var2);

      void jpaPositionalParameter(String var1, int var2);

      void other(char var1);
   }
}
