package org.hibernate.hql.internal.ast;

import java.util.Map;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.hql.spi.FilterTranslator;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class ASTQueryTranslatorFactory implements QueryTranslatorFactory {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ASTQueryTranslatorFactory.class.getName());

   public ASTQueryTranslatorFactory() {
      LOG.usingAstQueryTranslatorFactory();
   }

   public QueryTranslator createQueryTranslator(String queryIdentifier, String queryString, Map filters, SessionFactoryImplementor factory) {
      return new QueryTranslatorImpl(queryIdentifier, queryString, filters, factory);
   }

   public FilterTranslator createFilterTranslator(String queryIdentifier, String queryString, Map filters, SessionFactoryImplementor factory) {
      return new QueryTranslatorImpl(queryIdentifier, queryString, filters, factory);
   }
}
