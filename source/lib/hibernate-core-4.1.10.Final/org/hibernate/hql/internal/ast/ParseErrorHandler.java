package org.hibernate.hql.internal.ast;

import org.hibernate.QueryException;

public interface ParseErrorHandler extends ErrorReporter {
   int getErrorCount();

   void throwQueryException() throws QueryException;
}
