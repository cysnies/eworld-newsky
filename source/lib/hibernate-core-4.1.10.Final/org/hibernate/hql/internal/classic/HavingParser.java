package org.hibernate.hql.internal.classic;

public class HavingParser extends WhereParser {
   void appendToken(QueryTranslatorImpl q, String token) {
      q.appendHavingToken(token);
   }
}
