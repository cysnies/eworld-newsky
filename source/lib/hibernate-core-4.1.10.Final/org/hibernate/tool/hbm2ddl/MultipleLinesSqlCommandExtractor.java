package org.hibernate.tool.hbm2ddl;

import java.io.Reader;
import java.util.List;
import org.hibernate.hql.internal.antlr.SqlStatementLexer;
import org.hibernate.hql.internal.antlr.SqlStatementParser;

public class MultipleLinesSqlCommandExtractor implements ImportSqlCommandExtractor {
   public String[] extractCommands(Reader reader) {
      SqlStatementLexer lexer = new SqlStatementLexer(reader);
      SqlStatementParser parser = new SqlStatementParser(lexer);

      try {
         parser.script();
         parser.throwExceptionIfErrorOccurred();
      } catch (Exception e) {
         throw new ImportScriptException("Error during import script parsing.", e);
      }

      List<String> statementList = parser.getStatementList();
      return (String[])statementList.toArray(new String[statementList.size()]);
   }
}
