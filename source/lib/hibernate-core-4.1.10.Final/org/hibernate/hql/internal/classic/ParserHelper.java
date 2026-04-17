package org.hibernate.hql.internal.classic;

import java.util.StringTokenizer;
import org.hibernate.QueryException;

public final class ParserHelper {
   public static final String HQL_VARIABLE_PREFIX = ":";
   public static final String HQL_SEPARATORS = " \n\r\f\t,()=<>&|+-=/*'^![]#~\\";
   public static final String PATH_SEPARATORS = ".";

   public static boolean isWhitespace(String str) {
      return " \n\r\f\t".indexOf(str) > -1;
   }

   private ParserHelper() {
   }

   public static void parse(Parser p, String text, String seperators, QueryTranslatorImpl q) throws QueryException {
      StringTokenizer tokens = new StringTokenizer(text, seperators, true);
      p.start(q);

      while(tokens.hasMoreElements()) {
         p.token(tokens.nextToken(), q);
      }

      p.end(q);
   }
}
