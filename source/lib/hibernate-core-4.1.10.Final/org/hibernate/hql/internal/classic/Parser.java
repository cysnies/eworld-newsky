package org.hibernate.hql.internal.classic;

import org.hibernate.QueryException;

public interface Parser {
   void token(String var1, QueryTranslatorImpl var2) throws QueryException;

   void start(QueryTranslatorImpl var1) throws QueryException;

   void end(QueryTranslatorImpl var1) throws QueryException;
}
