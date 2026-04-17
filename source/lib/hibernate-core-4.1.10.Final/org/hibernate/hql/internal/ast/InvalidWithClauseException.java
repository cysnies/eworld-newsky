package org.hibernate.hql.internal.ast;

public class InvalidWithClauseException extends QuerySyntaxException {
   public InvalidWithClauseException(String message) {
      super(message);
   }

   public InvalidWithClauseException(String message, String queryString) {
      super(message, queryString);
   }
}
