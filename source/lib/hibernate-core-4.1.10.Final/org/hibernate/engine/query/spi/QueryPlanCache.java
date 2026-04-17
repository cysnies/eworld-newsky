package org.hibernate.engine.query.spi;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FilterImpl;
import org.hibernate.internal.util.collections.BoundedConcurrentHashMap;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class QueryPlanCache implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QueryPlanCache.class.getName());
   public static final int DEFAULT_PARAMETER_METADATA_MAX_COUNT = 128;
   public static final int DEFAULT_QUERY_PLAN_MAX_COUNT = 2048;
   private final SessionFactoryImplementor factory;
   private final BoundedConcurrentHashMap queryPlanCache;
   private final BoundedConcurrentHashMap parameterMetadataCache;

   public QueryPlanCache(SessionFactoryImplementor factory) {
      this.factory = factory;
      Integer maxParameterMetadataCount = ConfigurationHelper.getInteger("hibernate.query.plan_parameter_metadata_max_size", factory.getProperties());
      if (maxParameterMetadataCount == null) {
         maxParameterMetadataCount = ConfigurationHelper.getInt("hibernate.query.plan_cache_max_strong_references", factory.getProperties(), 128);
      }

      Integer maxQueryPlanCount = ConfigurationHelper.getInteger("hibernate.query.plan_cache_max_size", factory.getProperties());
      if (maxQueryPlanCount == null) {
         maxQueryPlanCount = ConfigurationHelper.getInt("hibernate.query.plan_cache_max_soft_references", factory.getProperties(), 2048);
      }

      this.queryPlanCache = new BoundedConcurrentHashMap(maxQueryPlanCount, 20, BoundedConcurrentHashMap.Eviction.LIRS);
      this.parameterMetadataCache = new BoundedConcurrentHashMap(maxParameterMetadataCount, 20, BoundedConcurrentHashMap.Eviction.LIRS);
   }

   public ParameterMetadata getSQLParameterMetadata(String query) {
      ParameterMetadata value = (ParameterMetadata)this.parameterMetadataCache.get(query);
      if (value == null) {
         value = this.buildParameterMetadata(query);
         this.parameterMetadataCache.putIfAbsent(query, value);
      }

      return value;
   }

   private ParameterMetadata buildParameterMetadata(String query) {
      ParamLocationRecognizer recognizer = ParamLocationRecognizer.parseLocations(query);
      int size = recognizer.getOrdinalParameterLocationList().size();
      OrdinalParameterDescriptor[] ordinalDescriptors = new OrdinalParameterDescriptor[size];

      for(int i = 0; i < size; ++i) {
         Integer position = (Integer)recognizer.getOrdinalParameterLocationList().get(i);
         ordinalDescriptors[i] = new OrdinalParameterDescriptor(i, (Type)null, position);
      }

      Iterator itr = recognizer.getNamedParameterDescriptionMap().entrySet().iterator();
      Map<String, NamedParameterDescriptor> namedParamDescriptorMap = new HashMap();

      while(itr.hasNext()) {
         Map.Entry entry = (Map.Entry)itr.next();
         String name = (String)entry.getKey();
         ParamLocationRecognizer.NamedParameterDescription description = (ParamLocationRecognizer.NamedParameterDescription)entry.getValue();
         namedParamDescriptorMap.put(name, new NamedParameterDescriptor(name, (Type)null, description.buildPositionsArray(), description.isJpaStyle()));
      }

      return new ParameterMetadata(ordinalDescriptors, namedParamDescriptorMap);
   }

   public HQLQueryPlan getHQLQueryPlan(String queryString, boolean shallow, Map enabledFilters) throws QueryException, MappingException {
      HQLQueryPlanKey key = new HQLQueryPlanKey(queryString, shallow, enabledFilters);
      HQLQueryPlan value = (HQLQueryPlan)this.queryPlanCache.get(key);
      if (value == null) {
         LOG.tracev("Unable to locate HQL query plan in cache; generating ({0})", queryString);
         value = new HQLQueryPlan(queryString, shallow, enabledFilters, this.factory);
         this.queryPlanCache.putIfAbsent(key, value);
      } else {
         LOG.tracev("Located HQL query plan in cache ({0})", queryString);
      }

      return value;
   }

   public FilterQueryPlan getFilterQueryPlan(String filterString, String collectionRole, boolean shallow, Map enabledFilters) throws QueryException, MappingException {
      FilterQueryPlanKey key = new FilterQueryPlanKey(filterString, collectionRole, shallow, enabledFilters);
      FilterQueryPlan value = (FilterQueryPlan)this.queryPlanCache.get(key);
      if (value == null) {
         LOG.tracev("Unable to locate collection-filter query plan in cache; generating ({0} : {1} )", collectionRole, filterString);
         value = new FilterQueryPlan(filterString, collectionRole, shallow, enabledFilters, this.factory);
         this.queryPlanCache.putIfAbsent(key, value);
      } else {
         LOG.tracev("Located collection-filter query plan in cache ({0} : {1})", collectionRole, filterString);
      }

      return value;
   }

   public NativeSQLQueryPlan getNativeSQLQueryPlan(NativeSQLQuerySpecification spec) {
      NativeSQLQueryPlan value = (NativeSQLQueryPlan)this.queryPlanCache.get(spec);
      if (value == null) {
         LOG.tracev("Unable to locate native-sql query plan in cache; generating ({0})", spec.getQueryString());
         value = new NativeSQLQueryPlan(spec, this.factory);
         this.queryPlanCache.putIfAbsent(spec, value);
      } else {
         LOG.tracev("Located native-sql query plan in cache ({0})", spec.getQueryString());
      }

      return value;
   }

   public void cleanup() {
      LOG.trace("Cleaning QueryPlan Cache");
      this.queryPlanCache.clear();
      this.parameterMetadataCache.clear();
   }

   private static class HQLQueryPlanKey implements Serializable {
      private final String query;
      private final boolean shallow;
      private final Set filterKeys;
      private final int hashCode;

      public HQLQueryPlanKey(String query, boolean shallow, Map enabledFilters) {
         this.query = query;
         this.shallow = shallow;
         if (CollectionHelper.isEmpty(enabledFilters)) {
            this.filterKeys = Collections.emptySet();
         } else {
            Set<DynamicFilterKey> tmp = new HashSet(CollectionHelper.determineProperSizing(enabledFilters), 0.75F);

            for(Object o : enabledFilters.values()) {
               tmp.add(new DynamicFilterKey((FilterImpl)o));
            }

            this.filterKeys = Collections.unmodifiableSet(tmp);
         }

         int hash = query.hashCode();
         hash = 29 * hash + (shallow ? 1 : 0);
         hash = 29 * hash + this.filterKeys.hashCode();
         this.hashCode = hash;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            HQLQueryPlanKey that = (HQLQueryPlanKey)o;
            return this.shallow == that.shallow && this.filterKeys.equals(that.filterKeys) && this.query.equals(that.query);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.hashCode;
      }
   }

   private static class DynamicFilterKey implements Serializable {
      private final String filterName;
      private final Map parameterMetadata;
      private final int hashCode;

      private DynamicFilterKey(FilterImpl filter) {
         this.filterName = filter.getName();
         if (filter.getParameters().isEmpty()) {
            this.parameterMetadata = Collections.emptyMap();
         } else {
            this.parameterMetadata = new HashMap(CollectionHelper.determineProperSizing(filter.getParameters()), 0.75F);

            for(Object o : filter.getParameters().entrySet()) {
               Map.Entry entry = (Map.Entry)o;
               String key = (String)entry.getKey();
               Integer valueCount;
               if (Collection.class.isInstance(entry.getValue())) {
                  valueCount = ((Collection)entry.getValue()).size();
               } else {
                  valueCount = 1;
               }

               this.parameterMetadata.put(key, valueCount);
            }
         }

         int hash = this.filterName.hashCode();
         hash = 31 * hash + this.parameterMetadata.hashCode();
         this.hashCode = hash;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            DynamicFilterKey that = (DynamicFilterKey)o;
            return this.filterName.equals(that.filterName) && this.parameterMetadata.equals(that.parameterMetadata);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.hashCode;
      }
   }

   private static class FilterQueryPlanKey implements Serializable {
      private final String query;
      private final String collectionRole;
      private final boolean shallow;
      private final Set filterNames;
      private final int hashCode;

      public FilterQueryPlanKey(String query, String collectionRole, boolean shallow, Map enabledFilters) {
         this.query = query;
         this.collectionRole = collectionRole;
         this.shallow = shallow;
         if (CollectionHelper.isEmpty(enabledFilters)) {
            this.filterNames = Collections.emptySet();
         } else {
            Set<String> tmp = new HashSet();
            tmp.addAll(enabledFilters.keySet());
            this.filterNames = Collections.unmodifiableSet(tmp);
         }

         int hash = query.hashCode();
         hash = 29 * hash + collectionRole.hashCode();
         hash = 29 * hash + (shallow ? 1 : 0);
         hash = 29 * hash + this.filterNames.hashCode();
         this.hashCode = hash;
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            FilterQueryPlanKey that = (FilterQueryPlanKey)o;
            return this.shallow == that.shallow && this.filterNames.equals(that.filterNames) && this.query.equals(that.query) && this.collectionRole.equals(that.collectionRole);
         } else {
            return false;
         }
      }

      public int hashCode() {
         return this.hashCode;
      }
   }
}
