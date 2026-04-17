package org.hibernate.loader.custom.sql;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.loader.custom.CustomQuery;
import org.hibernate.persister.collection.SQLLoadableCollection;
import org.hibernate.persister.entity.SQLLoadable;
import org.jboss.logging.Logger;

public class SQLCustomQuery implements CustomQuery {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SQLCustomQuery.class.getName());
   private final String sql;
   private final Set querySpaces = new HashSet();
   private final Map namedParameterBindPoints = new HashMap();
   private final List customQueryReturns = new ArrayList();

   public String getSQL() {
      return this.sql;
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }

   public Map getNamedParameterBindPoints() {
      return this.namedParameterBindPoints;
   }

   public List getCustomQueryReturns() {
      return this.customQueryReturns;
   }

   public SQLCustomQuery(String sqlQuery, NativeSQLQueryReturn[] queryReturns, Collection additionalQuerySpaces, SessionFactoryImplementor factory) throws HibernateException {
      LOG.tracev("Starting processing of sql query [{0}]", sqlQuery);
      SQLQueryReturnProcessor processor = new SQLQueryReturnProcessor(queryReturns, factory);
      SQLQueryReturnProcessor.ResultAliasContext aliasContext = processor.process();
      SQLQueryParser parser = new SQLQueryParser(sqlQuery, new ParserContext(aliasContext), factory);
      this.sql = parser.process();
      this.namedParameterBindPoints.putAll(parser.getNamedParameters());
      this.customQueryReturns.addAll(processor.generateCustomReturns(parser.queryHasAliases()));
      if (additionalQuerySpaces != null) {
         this.querySpaces.addAll(additionalQuerySpaces);
      }

   }

   private static class ParserContext implements SQLQueryParser.ParserContext {
      private final SQLQueryReturnProcessor.ResultAliasContext aliasContext;

      public ParserContext(SQLQueryReturnProcessor.ResultAliasContext aliasContext) {
         this.aliasContext = aliasContext;
      }

      public boolean isEntityAlias(String alias) {
         return this.getEntityPersisterByAlias(alias) != null;
      }

      public SQLLoadable getEntityPersisterByAlias(String alias) {
         return this.aliasContext.getEntityPersister(alias);
      }

      public String getEntitySuffixByAlias(String alias) {
         return this.aliasContext.getEntitySuffix(alias);
      }

      public boolean isCollectionAlias(String alias) {
         return this.getCollectionPersisterByAlias(alias) != null;
      }

      public SQLLoadableCollection getCollectionPersisterByAlias(String alias) {
         return this.aliasContext.getCollectionPersister(alias);
      }

      public String getCollectionSuffixByAlias(String alias) {
         return this.aliasContext.getCollectionSuffix(alias);
      }

      public Map getPropertyResultsMapByAlias(String alias) {
         return this.aliasContext.getPropertyResultsMap(alias);
      }
   }
}
