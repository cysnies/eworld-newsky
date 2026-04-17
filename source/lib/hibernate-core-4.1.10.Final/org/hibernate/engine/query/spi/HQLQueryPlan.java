package org.hibernate.engine.query.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.QueryException;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.QuerySplitter;
import org.hibernate.hql.spi.FilterTranslator;
import org.hibernate.hql.spi.ParameterTranslations;
import org.hibernate.hql.spi.QueryTranslator;
import org.hibernate.hql.spi.QueryTranslatorFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.collections.EmptyIterator;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.internal.util.collections.JoinedIterator;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class HQLQueryPlan implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, HQLQueryPlan.class.getName());
   private final String sourceQuery;
   private final QueryTranslator[] translators;
   private final String[] sqlStrings;
   private final ParameterMetadata parameterMetadata;
   private final ReturnMetadata returnMetadata;
   private final Set querySpaces;
   private final Set enabledFilterNames;
   private final boolean shallow;

   public HQLQueryPlan(String hql, boolean shallow, Map enabledFilters, SessionFactoryImplementor factory) {
      this(hql, (String)null, shallow, enabledFilters, factory);
   }

   protected HQLQueryPlan(String hql, String collectionRole, boolean shallow, Map enabledFilters, SessionFactoryImplementor factory) {
      this.sourceQuery = hql;
      this.shallow = shallow;
      Set copy = new HashSet();
      copy.addAll(enabledFilters.keySet());
      this.enabledFilterNames = Collections.unmodifiableSet(copy);
      String[] concreteQueryStrings = QuerySplitter.concreteQueries(hql, factory);
      int length = concreteQueryStrings.length;
      this.translators = new QueryTranslator[length];
      List<String> sqlStringList = new ArrayList();
      Set combinedQuerySpaces = new HashSet();
      boolean hasCollectionRole = collectionRole == null;
      Map querySubstitutions = factory.getSettings().getQuerySubstitutions();
      QueryTranslatorFactory queryTranslatorFactory = factory.getSettings().getQueryTranslatorFactory();

      for(int i = 0; i < length; ++i) {
         if (hasCollectionRole) {
            this.translators[i] = queryTranslatorFactory.createQueryTranslator(hql, concreteQueryStrings[i], enabledFilters, factory);
            this.translators[i].compile(querySubstitutions, shallow);
         } else {
            this.translators[i] = queryTranslatorFactory.createFilterTranslator(hql, concreteQueryStrings[i], enabledFilters, factory);
            ((FilterTranslator)this.translators[i]).compile(collectionRole, querySubstitutions, shallow);
         }

         combinedQuerySpaces.addAll(this.translators[i].getQuerySpaces());
         sqlStringList.addAll(this.translators[i].collectSqlStrings());
      }

      this.sqlStrings = ArrayHelper.toStringArray((Collection)sqlStringList);
      this.querySpaces = combinedQuerySpaces;
      if (length == 0) {
         this.parameterMetadata = new ParameterMetadata((OrdinalParameterDescriptor[])null, (Map)null);
         this.returnMetadata = null;
      } else {
         this.parameterMetadata = this.buildParameterMetadata(this.translators[0].getParameterTranslations(), hql);
         if (this.translators[0].isManipulationStatement()) {
            this.returnMetadata = null;
         } else {
            Type[] types = length > 1 ? new Type[this.translators[0].getReturnTypes().length] : this.translators[0].getReturnTypes();
            this.returnMetadata = new ReturnMetadata(this.translators[0].getReturnAliases(), types);
         }
      }

   }

   public String getSourceQuery() {
      return this.sourceQuery;
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }

   public ParameterMetadata getParameterMetadata() {
      return this.parameterMetadata;
   }

   public ReturnMetadata getReturnMetadata() {
      return this.returnMetadata;
   }

   public Set getEnabledFilterNames() {
      return this.enabledFilterNames;
   }

   public String[] getSqlStrings() {
      return this.sqlStrings;
   }

   public Set getUtilizedFilterNames() {
      return null;
   }

   public boolean isShallow() {
      return this.shallow;
   }

   public List performList(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Find: {0}", this.getSourceQuery());
         queryParameters.traceParameters(session.getFactory());
      }

      boolean hasLimit = queryParameters.getRowSelection() != null && queryParameters.getRowSelection().definesLimits();
      boolean needsLimit = hasLimit && this.translators.length > 1;
      QueryParameters queryParametersToUse;
      if (needsLimit) {
         LOG.needsLimit();
         RowSelection selection = new RowSelection();
         selection.setFetchSize(queryParameters.getRowSelection().getFetchSize());
         selection.setTimeout(queryParameters.getRowSelection().getTimeout());
         queryParametersToUse = queryParameters.createCopyUsing(selection);
      } else {
         queryParametersToUse = queryParameters;
      }

      List combinedResults = new ArrayList();
      IdentitySet distinction = new IdentitySet();
      int includedCount = -1;

      for(QueryTranslator translator : this.translators) {
         List tmp = translator.list(session, queryParametersToUse);
         if (needsLimit) {
            int first = queryParameters.getRowSelection().getFirstRow() == null ? 0 : queryParameters.getRowSelection().getFirstRow();
            int max = queryParameters.getRowSelection().getMaxRows() == null ? -1 : queryParameters.getRowSelection().getMaxRows();

            for(Object result : tmp) {
               if (distinction.add(result)) {
                  ++includedCount;
                  if (includedCount >= first) {
                     combinedResults.add(result);
                     if (max >= 0 && includedCount > max) {
                        return combinedResults;
                     }
                  }
               }
            }
         } else {
            combinedResults.addAll(tmp);
         }
      }

      return combinedResults;
   }

   public Iterator performIterate(QueryParameters queryParameters, EventSource session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Iterate: {0}", this.getSourceQuery());
         queryParameters.traceParameters(session.getFactory());
      }

      if (this.translators.length == 0) {
         return EmptyIterator.INSTANCE;
      } else {
         Iterator[] results = null;
         boolean many = this.translators.length > 1;
         if (many) {
            results = new Iterator[this.translators.length];
         }

         Iterator result = null;

         for(int i = 0; i < this.translators.length; ++i) {
            result = this.translators[i].iterate(queryParameters, session);
            if (many) {
               results[i] = result;
            }
         }

         return (Iterator)(many ? new JoinedIterator(results) : result);
      }
   }

   public ScrollableResults performScroll(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Iterate: {0}", this.getSourceQuery());
         queryParameters.traceParameters(session.getFactory());
      }

      if (this.translators.length != 1) {
         throw new QueryException("implicit polymorphism not supported for scroll() queries");
      } else if (queryParameters.getRowSelection().definesLimits() && this.translators[0].containsCollectionFetches()) {
         throw new QueryException("firstResult/maxResults not supported in conjunction with scroll() of a query containing collection fetches");
      } else {
         return this.translators[0].scroll(queryParameters, session);
      }
   }

   public int performExecuteUpdate(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Execute update: {0}", this.getSourceQuery());
         queryParameters.traceParameters(session.getFactory());
      }

      if (this.translators.length != 1) {
         LOG.splitQueries(this.getSourceQuery(), this.translators.length);
      }

      int result = 0;

      for(QueryTranslator translator : this.translators) {
         result += translator.executeUpdate(queryParameters, session);
      }

      return result;
   }

   private ParameterMetadata buildParameterMetadata(ParameterTranslations parameterTranslations, String hql) {
      long start = System.currentTimeMillis();
      ParamLocationRecognizer recognizer = ParamLocationRecognizer.parseLocations(hql);
      long end = System.currentTimeMillis();
      if (LOG.isTraceEnabled()) {
         LOG.tracev("HQL param location recognition took {0} mills ({1})", end - start, hql);
      }

      int ordinalParamCount = parameterTranslations.getOrdinalParameterCount();
      int[] locations = ArrayHelper.toIntArray(recognizer.getOrdinalParameterLocationList());
      if (parameterTranslations.supportsOrdinalParameterMetadata() && locations.length != ordinalParamCount) {
         throw new HibernateException("ordinal parameter mismatch");
      } else {
         ordinalParamCount = locations.length;
         OrdinalParameterDescriptor[] ordinalParamDescriptors = new OrdinalParameterDescriptor[ordinalParamCount];

         for(int i = 1; i <= ordinalParamCount; ++i) {
            ordinalParamDescriptors[i - 1] = new OrdinalParameterDescriptor(i, parameterTranslations.supportsOrdinalParameterMetadata() ? parameterTranslations.getOrdinalParameterExpectedType(i) : null, locations[i - 1]);
         }

         Iterator itr = recognizer.getNamedParameterDescriptionMap().entrySet().iterator();
         Map<String, NamedParameterDescriptor> namedParamDescriptorMap = new HashMap();

         while(itr.hasNext()) {
            Map.Entry entry = (Map.Entry)itr.next();
            String name = (String)entry.getKey();
            ParamLocationRecognizer.NamedParameterDescription description = (ParamLocationRecognizer.NamedParameterDescription)entry.getValue();
            namedParamDescriptorMap.put(name, new NamedParameterDescriptor(name, parameterTranslations.getNamedParameterExpectedType(name), description.buildPositionsArray(), description.isJpaStyle()));
         }

         return new ParameterMetadata(ordinalParamDescriptors, namedParamDescriptorMap);
      }
   }

   public QueryTranslator[] getTranslators() {
      QueryTranslator[] copy = new QueryTranslator[this.translators.length];
      System.arraycopy(this.translators, 0, copy, 0, copy.length);
      return copy;
   }

   public Class getDynamicInstantiationResultType() {
      return this.translators[0].getDynamicInstantiationResultType();
   }
}
