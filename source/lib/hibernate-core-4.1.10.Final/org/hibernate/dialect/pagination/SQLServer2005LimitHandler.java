package org.hibernate.dialect.pagination;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.internal.util.StringHelper;

public class SQLServer2005LimitHandler extends AbstractLimitHandler {
   private static final String SELECT = "select";
   private static final String SELECT_WITH_SPACE = "select ";
   private static final String FROM = "from";
   private static final String DISTINCT = "distinct";
   private static final String ORDER_BY = "order by";
   private static final Pattern ALIAS_PATTERN = Pattern.compile("(?i)\\sas\\s(.)+$");
   private boolean topAdded = false;
   private boolean hasOffset = true;

   public SQLServer2005LimitHandler(String sql, RowSelection selection) {
      super(sql, selection);
   }

   public boolean supportsLimit() {
      return true;
   }

   public boolean useMaxForLimit() {
      return true;
   }

   public boolean supportsLimitOffset() {
      return true;
   }

   public boolean supportsVariableLimit() {
      return true;
   }

   public int convertToFirstRowValue(int zeroBasedFirstResult) {
      return zeroBasedFirstResult + 1;
   }

   public String getProcessedSql() {
      StringBuilder sb = new StringBuilder(this.sql);
      if (sb.charAt(sb.length() - 1) == ';') {
         sb.setLength(sb.length() - 1);
      }

      if (LimitHelper.hasFirstRow(this.selection)) {
         String selectClause = this.fillAliasInSelectClause(sb);
         int orderByIndex = shallowIndexOfWord(sb, "order by", 0);
         if (orderByIndex > 0) {
            this.addTopExpression(sb);
         }

         this.encloseWithOuterQuery(sb);
         sb.insert(0, "WITH query AS (").append(") SELECT ").append(selectClause).append(" FROM query ");
         sb.append("WHERE __hibernate_row_nr__ >= ? AND __hibernate_row_nr__ < ?");
      } else {
         this.hasOffset = false;
         this.addTopExpression(sb);
      }

      return sb.toString();
   }

   public int bindLimitParametersAtStartOfQuery(PreparedStatement statement, int index) throws SQLException {
      if (this.topAdded) {
         statement.setInt(index, this.getMaxOrLimit() - 1);
         return 1;
      } else {
         return 0;
      }
   }

   public int bindLimitParametersAtEndOfQuery(PreparedStatement statement, int index) throws SQLException {
      return this.hasOffset ? super.bindLimitParametersAtEndOfQuery(statement, index) : 0;
   }

   protected String fillAliasInSelectClause(StringBuilder sb) {
      List<String> aliases = new LinkedList();
      int startPos = shallowIndexOf(sb, "select ", 0);
      int endPos = shallowIndexOfWord(sb, "from", startPos);
      int nextComa = startPos;
      int prevComa = startPos;
      int unique = 0;
      boolean selectsMultipleColumns = false;

      while(nextComa != -1) {
         prevComa = nextComa;
         nextComa = shallowIndexOf(sb, ",", nextComa);
         if (nextComa > endPos) {
            break;
         }

         if (nextComa != -1) {
            String expression = sb.substring(prevComa, nextComa);
            if (this.selectsMultipleColumns(expression)) {
               selectsMultipleColumns = true;
            } else {
               String alias = this.getAlias(expression);
               if (alias == null) {
                  alias = StringHelper.generateAlias("page", unique);
                  sb.insert(nextComa, " as " + alias);
                  ++unique;
                  nextComa += (" as " + alias).length();
               }

               aliases.add(alias);
            }

            ++nextComa;
         }
      }

      endPos = shallowIndexOfWord(sb, "from", startPos);
      String expression = sb.substring(prevComa, endPos);
      if (this.selectsMultipleColumns(expression)) {
         selectsMultipleColumns = true;
      } else {
         String alias = this.getAlias(expression);
         if (alias == null) {
            alias = StringHelper.generateAlias("page", unique);
            sb.insert(endPos - 1, " as " + alias);
         }

         aliases.add(alias);
      }

      return selectsMultipleColumns ? "*" : StringHelper.join(", ", aliases.iterator());
   }

   private boolean selectsMultipleColumns(String expression) {
      String lastExpr = expression.trim().replaceFirst("(?i)(.)*\\s", "");
      return "*".equals(lastExpr) || lastExpr.endsWith(".*");
   }

   private String getAlias(String expression) {
      Matcher matcher = ALIAS_PATTERN.matcher(expression);
      return matcher.find() ? matcher.group(0).replaceFirst("(?i)(.)*\\sas\\s", "").trim() : null;
   }

   protected void encloseWithOuterQuery(StringBuilder sql) {
      sql.insert(0, "SELECT inner_query.*, ROW_NUMBER() OVER (ORDER BY CURRENT_TIMESTAMP) as __hibernate_row_nr__ FROM ( ");
      sql.append(" ) inner_query ");
   }

   protected void addTopExpression(StringBuilder sql) {
      int distinctStartPos = shallowIndexOfWord(sql, "distinct", 0);
      if (distinctStartPos > 0) {
         sql.insert(distinctStartPos + "distinct".length(), " TOP(?)");
      } else {
         int selectStartPos = shallowIndexOf(sql, "select ", 0);
         sql.insert(selectStartPos + "select".length(), " TOP(?)");
      }

      this.topAdded = true;
   }

   private static int shallowIndexOfWord(StringBuilder sb, String search, int fromIndex) {
      int index = shallowIndexOf(sb, ' ' + search + ' ', fromIndex);
      return index != -1 ? index + 1 : -1;
   }

   private static int shallowIndexOf(StringBuilder sb, String search, int fromIndex) {
      String lowercase = sb.toString().toLowerCase();
      int len = lowercase.length();
      int searchlen = search.length();
      int pos = -1;
      int depth = 0;
      int cur = fromIndex;

      do {
         pos = lowercase.indexOf(search, cur);
         if (pos != -1) {
            for(int iter = cur; iter < pos; ++iter) {
               char c = sb.charAt(iter);
               if (c == '(') {
                  ++depth;
               } else if (c == ')') {
                  --depth;
               }
            }

            cur = pos + searchlen;
         }
      } while(cur < len && depth != 0 && pos != -1);

      return depth == 0 ? pos : -1;
   }
}
