package org.hibernate.engine.jdbc.internal;

import java.util.StringTokenizer;
import org.hibernate.internal.util.StringHelper;

public class DDLFormatterImpl implements Formatter {
   public String format(String sql) {
      if (StringHelper.isEmpty(sql)) {
         return sql;
      } else if (sql.toLowerCase().startsWith("create table")) {
         return this.formatCreateTable(sql);
      } else if (sql.toLowerCase().startsWith("alter table")) {
         return this.formatAlterTable(sql);
      } else {
         return sql.toLowerCase().startsWith("comment on") ? this.formatCommentOn(sql) : "\n    " + sql;
      }
   }

   private String formatCommentOn(String sql) {
      StringBuilder result = (new StringBuilder(60)).append("\n    ");
      StringTokenizer tokens = new StringTokenizer(sql, " '[]\"", true);
      boolean quoted = false;

      while(tokens.hasMoreTokens()) {
         String token = tokens.nextToken();
         result.append(token);
         if (isQuote(token)) {
            quoted = !quoted;
         } else if (!quoted && "is".equals(token)) {
            result.append("\n       ");
         }
      }

      return result.toString();
   }

   private String formatAlterTable(String sql) {
      StringBuilder result = (new StringBuilder(60)).append("\n    ");
      StringTokenizer tokens = new StringTokenizer(sql, " (,)'[]\"", true);

      String token;
      for(boolean quoted = false; tokens.hasMoreTokens(); result.append(token)) {
         token = tokens.nextToken();
         if (isQuote(token)) {
            quoted = !quoted;
         } else if (!quoted && isBreak(token)) {
            result.append("\n        ");
         }
      }

      return result.toString();
   }

   private String formatCreateTable(String sql) {
      StringBuilder result = (new StringBuilder(60)).append("\n    ");
      StringTokenizer tokens = new StringTokenizer(sql, "(,)'[]\"", true);
      int depth = 0;
      boolean quoted = false;

      while(tokens.hasMoreTokens()) {
         String token = tokens.nextToken();
         if (isQuote(token)) {
            quoted = !quoted;
            result.append(token);
         } else if (quoted) {
            result.append(token);
         } else {
            if (")".equals(token)) {
               --depth;
               if (depth == 0) {
                  result.append("\n    ");
               }
            }

            result.append(token);
            if (",".equals(token) && depth == 1) {
               result.append("\n       ");
            }

            if ("(".equals(token)) {
               ++depth;
               if (depth == 1) {
                  result.append("\n        ");
               }
            }
         }
      }

      return result.toString();
   }

   private static boolean isBreak(String token) {
      return "drop".equals(token) || "add".equals(token) || "references".equals(token) || "foreign".equals(token) || "on".equals(token);
   }

   private static boolean isQuote(String tok) {
      return "\"".equals(tok) || "`".equals(tok) || "]".equals(tok) || "[".equals(tok) || "'".equals(tok);
   }
}
