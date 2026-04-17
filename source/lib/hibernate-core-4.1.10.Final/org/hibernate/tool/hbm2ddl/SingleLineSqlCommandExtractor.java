package org.hibernate.tool.hbm2ddl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.internal.util.StringHelper;

public class SingleLineSqlCommandExtractor implements ImportSqlCommandExtractor {
   public String[] extractCommands(Reader reader) {
      BufferedReader bufferedReader = new BufferedReader(reader);
      List<String> statementList = new LinkedList();

      try {
         for(String sql = bufferedReader.readLine(); sql != null; sql = bufferedReader.readLine()) {
            String trimmedSql = sql.trim();
            if (!StringHelper.isEmpty(trimmedSql) && !this.isComment(trimmedSql)) {
               if (trimmedSql.endsWith(";")) {
                  trimmedSql = trimmedSql.substring(0, trimmedSql.length() - 1);
               }

               statementList.add(trimmedSql);
            }
         }

         return (String[])statementList.toArray(new String[statementList.size()]);
      } catch (IOException e) {
         throw new ImportScriptException("Error during import script parsing.", e);
      }
   }

   private boolean isComment(String line) {
      return line.startsWith("--") || line.startsWith("//") || line.startsWith("/*");
   }
}
