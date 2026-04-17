package org.hibernate.hql.internal.classic;

import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.spi.FilterTranslator;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;

public class ClassicQueryTranslatorFactory implements QueryTranslatorFactory {
   public QueryTranslator createQueryTranslator(String queryIdentifier, String queryString, Map filters, SessionFactoryImplementor factory) {
      return new QueryTranslatorImpl(queryIdentifier, queryString, filters, factory);
   }

   public FilterTranslator createFilterTranslator(String queryIdentifier, String queryString, Map filters, SessionFactoryImplementor factory) {
      return new QueryTranslatorImpl(queryIdentifier, queryString, filters, factory);
   }
}
