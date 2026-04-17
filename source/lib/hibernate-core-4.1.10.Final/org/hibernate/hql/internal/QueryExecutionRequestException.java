package org.hibernate.hql.internal;

import org.hibernate.QueryException;

public class QueryExecutionRequestException extends QueryException {
   public QueryExecutionRequestException(String message, String queryString) {
      super(message, queryString);
   }
}
