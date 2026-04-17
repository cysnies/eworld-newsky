package org.hibernate.hql.internal.ast;

import antlr.RecognitionException;
import org.hibernate.QueryException;

public class QuerySyntaxException extends QueryException {
   public QuerySyntaxException(String message) {
      super(message);
   }

   public QuerySyntaxException(String message, String hql) {
      this(message);
      this.setQueryString(hql);
   }

   public static QuerySyntaxException convert(RecognitionException e) {
      return convert(e, (String)null);
   }

   public static QuerySyntaxException convert(RecognitionException e, String hql) {
      String positionInfo = e.getLine() > 0 && e.getColumn() > 0 ? " near line " + e.getLine() + ", column " + e.getColumn() : "";
      return new QuerySyntaxException(e.getMessage() + positionInfo, hql);
   }
}
