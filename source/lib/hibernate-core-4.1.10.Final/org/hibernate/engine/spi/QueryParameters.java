package org.hibernate.engine.spi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.ScrollMode;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FilterImpl;
import org.hibernate.internal.util.EntityPrinter;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public final class QueryParameters {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, QueryParameters.class.getName());
   private Type[] positionalParameterTypes;
   private Object[] positionalParameterValues;
   private Map namedParameters;
   private LockOptions lockOptions;
   private RowSelection rowSelection;
   private boolean cacheable;
   private String cacheRegion;
   private String comment;
   private ScrollMode scrollMode;
   private Serializable[] collectionKeys;
   private Object optionalObject;
   private String optionalEntityName;
   private Serializable optionalId;
   private boolean isReadOnlyInitialized;
   private boolean readOnly;
   private boolean callable;
   private boolean autodiscovertypes;
   private boolean isNaturalKeyLookup;
   private final ResultTransformer resultTransformer;
   private String processedSQL;
   private Type[] processedPositionalParameterTypes;
   private Object[] processedPositionalParameterValues;

   public QueryParameters() {
      this(ArrayHelper.EMPTY_TYPE_ARRAY, ArrayHelper.EMPTY_OBJECT_ARRAY);
   }

   public QueryParameters(Type type, Object value) {
      this(new Type[]{type}, new Object[]{value});
   }

   public QueryParameters(Type[] positionalParameterTypes, Object[] positionalParameterValues, Object optionalObject, String optionalEntityName, Serializable optionalObjectId) {
      this(positionalParameterTypes, positionalParameterValues);
      this.optionalObject = optionalObject;
      this.optionalId = optionalObjectId;
      this.optionalEntityName = optionalEntityName;
   }

   public QueryParameters(Type[] positionalParameterTypes, Object[] positionalParameterValues) {
      this(positionalParameterTypes, positionalParameterValues, (LockOptions)null, (RowSelection)null, false, false, false, (String)null, (String)null, false, (ResultTransformer)null);
   }

   public QueryParameters(Type[] positionalParameterTypes, Object[] positionalParameterValues, Serializable[] collectionKeys) {
      this(positionalParameterTypes, positionalParameterValues, (Map)null, collectionKeys);
   }

   public QueryParameters(Type[] positionalParameterTypes, Object[] positionalParameterValues, Map namedParameters, Serializable[] collectionKeys) {
      this(positionalParameterTypes, positionalParameterValues, namedParameters, (LockOptions)null, (RowSelection)null, false, false, false, (String)null, (String)null, collectionKeys, (ResultTransformer)null);
   }

   public QueryParameters(Type[] positionalParameterTypes, Object[] positionalParameterValues, LockOptions lockOptions, RowSelection rowSelection, boolean isReadOnlyInitialized, boolean readOnly, boolean cacheable, String cacheRegion, String comment, boolean isLookupByNaturalKey, ResultTransformer transformer) {
      this(positionalParameterTypes, positionalParameterValues, (Map)null, lockOptions, rowSelection, isReadOnlyInitialized, readOnly, cacheable, cacheRegion, comment, (Serializable[])null, transformer);
      this.isNaturalKeyLookup = isLookupByNaturalKey;
   }

   public QueryParameters(Type[] positionalParameterTypes, Object[] positionalParameterValues, Map namedParameters, LockOptions lockOptions, RowSelection rowSelection, boolean isReadOnlyInitialized, boolean readOnly, boolean cacheable, String cacheRegion, String comment, Serializable[] collectionKeys, ResultTransformer transformer) {
      this.callable = false;
      this.autodiscovertypes = false;
      this.positionalParameterTypes = positionalParameterTypes;
      this.positionalParameterValues = positionalParameterValues;
      this.namedParameters = namedParameters;
      this.lockOptions = lockOptions;
      this.rowSelection = rowSelection;
      this.cacheable = cacheable;
      this.cacheRegion = cacheRegion;
      this.comment = comment;
      this.collectionKeys = collectionKeys;
      this.isReadOnlyInitialized = isReadOnlyInitialized;
      this.readOnly = readOnly;
      this.resultTransformer = transformer;
   }

   public QueryParameters(Type[] positionalParameterTypes, Object[] positionalParameterValues, Map namedParameters, LockOptions lockOptions, RowSelection rowSelection, boolean isReadOnlyInitialized, boolean readOnly, boolean cacheable, String cacheRegion, String comment, Serializable[] collectionKeys, Object optionalObject, String optionalEntityName, Serializable optionalId, ResultTransformer transformer) {
      this(positionalParameterTypes, positionalParameterValues, namedParameters, lockOptions, rowSelection, isReadOnlyInitialized, readOnly, cacheable, cacheRegion, comment, collectionKeys, transformer);
      this.optionalEntityName = optionalEntityName;
      this.optionalId = optionalId;
      this.optionalObject = optionalObject;
   }

   public boolean hasRowSelection() {
      return this.rowSelection != null;
   }

   public Map getNamedParameters() {
      return this.namedParameters;
   }

   public Type[] getPositionalParameterTypes() {
      return this.positionalParameterTypes;
   }

   public Object[] getPositionalParameterValues() {
      return this.positionalParameterValues;
   }

   public RowSelection getRowSelection() {
      return this.rowSelection;
   }

   public ResultTransformer getResultTransformer() {
      return this.resultTransformer;
   }

   public void setNamedParameters(Map map) {
      this.namedParameters = map;
   }

   public void setPositionalParameterTypes(Type[] types) {
      this.positionalParameterTypes = types;
   }

   public void setPositionalParameterValues(Object[] objects) {
      this.positionalParameterValues = objects;
   }

   public void setRowSelection(RowSelection selection) {
      this.rowSelection = selection;
   }

   public LockOptions getLockOptions() {
      return this.lockOptions;
   }

   public void setLockOptions(LockOptions lockOptions) {
      this.lockOptions = lockOptions;
   }

   public void traceParameters(SessionFactoryImplementor factory) throws HibernateException {
      EntityPrinter print = new EntityPrinter(factory);
      if (this.positionalParameterValues.length != 0) {
         LOG.tracev("Parameters: {0}", print.toString(this.positionalParameterTypes, this.positionalParameterValues));
      }

      if (this.namedParameters != null) {
         LOG.tracev("Named parameters: {0}", print.toString(this.namedParameters));
      }

   }

   public boolean isCacheable() {
      return this.cacheable;
   }

   public void setCacheable(boolean b) {
      this.cacheable = b;
   }

   public String getCacheRegion() {
      return this.cacheRegion;
   }

   public void setCacheRegion(String cacheRegion) {
      this.cacheRegion = cacheRegion;
   }

   public void validateParameters() throws QueryException {
      int types = this.positionalParameterTypes == null ? 0 : this.positionalParameterTypes.length;
      int values = this.positionalParameterValues == null ? 0 : this.positionalParameterValues.length;
      if (types != values) {
         throw new QueryException("Number of positional parameter types:" + types + " does not match number of positional parameters: " + values);
      }
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public ScrollMode getScrollMode() {
      return this.scrollMode;
   }

   public void setScrollMode(ScrollMode scrollMode) {
      this.scrollMode = scrollMode;
   }

   public Serializable[] getCollectionKeys() {
      return this.collectionKeys;
   }

   public void setCollectionKeys(Serializable[] collectionKeys) {
      this.collectionKeys = collectionKeys;
   }

   public String getOptionalEntityName() {
      return this.optionalEntityName;
   }

   public void setOptionalEntityName(String optionalEntityName) {
      this.optionalEntityName = optionalEntityName;
   }

   public Serializable getOptionalId() {
      return this.optionalId;
   }

   public void setOptionalId(Serializable optionalId) {
      this.optionalId = optionalId;
   }

   public Object getOptionalObject() {
      return this.optionalObject;
   }

   public void setOptionalObject(Object optionalObject) {
      this.optionalObject = optionalObject;
   }

   public boolean isReadOnlyInitialized() {
      return this.isReadOnlyInitialized;
   }

   public boolean isReadOnly() {
      if (!this.isReadOnlyInitialized()) {
         throw new IllegalStateException("cannot call isReadOnly() when isReadOnlyInitialized() returns false");
      } else {
         return this.readOnly;
      }
   }

   public boolean isReadOnly(SessionImplementor session) {
      return this.isReadOnlyInitialized ? this.isReadOnly() : session.getPersistenceContext().isDefaultReadOnly();
   }

   public void setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
      this.isReadOnlyInitialized = true;
   }

   public void setCallable(boolean callable) {
      this.callable = callable;
   }

   public boolean isCallable() {
      return this.callable;
   }

   public boolean hasAutoDiscoverScalarTypes() {
      return this.autodiscovertypes;
   }

   public void processFilters(String sql, SessionImplementor session) {
      this.processFilters(sql, session.getLoadQueryInfluencers().getEnabledFilters(), session.getFactory());
   }

   public void processFilters(String sql, Map filters, SessionFactoryImplementor factory) {
      if (filters.size() != 0 && sql.contains(":")) {
         Dialect dialect = factory.getDialect();
         String symbols = " \n\r\f\t,()=<>&|+-=/*'^![]#~\\" + dialect.openQuote() + dialect.closeQuote();
         StringTokenizer tokens = new StringTokenizer(sql, symbols, true);
         StringBuilder result = new StringBuilder();
         List parameters = new ArrayList();
         List parameterTypes = new ArrayList();
         int positionalIndex = 0;

         while(tokens.hasMoreTokens()) {
            String token = tokens.nextToken();
            if (token.startsWith(":")) {
               String filterParameterName = token.substring(1);
               String[] parts = LoadQueryInfluencers.parseFilterParameterName(filterParameterName);
               FilterImpl filter = (FilterImpl)filters.get(parts[0]);
               Object value = filter.getParameter(parts[1]);
               Type type = filter.getFilterDefinition().getParameterType(parts[1]);
               if (value != null && Collection.class.isAssignableFrom(value.getClass())) {
                  Iterator itr = ((Collection)value).iterator();

                  while(itr.hasNext()) {
                     Object elementValue = itr.next();
                     result.append('?');
                     parameters.add(elementValue);
                     parameterTypes.add(type);
                     if (itr.hasNext()) {
                        result.append(", ");
                     }
                  }
               } else {
                  result.append('?');
                  parameters.add(value);
                  parameterTypes.add(type);
               }
            } else {
               if ("?".equals(token) && positionalIndex < this.getPositionalParameterValues().length) {
                  parameters.add(this.getPositionalParameterValues()[positionalIndex]);
                  parameterTypes.add(this.getPositionalParameterTypes()[positionalIndex]);
                  ++positionalIndex;
               }

               result.append(token);
            }
         }

         this.processedPositionalParameterValues = parameters.toArray();
         this.processedPositionalParameterTypes = (Type[])parameterTypes.toArray(new Type[parameterTypes.size()]);
         this.processedSQL = result.toString();
      } else {
         this.processedPositionalParameterValues = this.getPositionalParameterValues();
         this.processedPositionalParameterTypes = this.getPositionalParameterTypes();
         this.processedSQL = sql;
      }

   }

   public String getFilteredSQL() {
      return this.processedSQL;
   }

   public Object[] getFilteredPositionalParameterValues() {
      return this.processedPositionalParameterValues;
   }

   public Type[] getFilteredPositionalParameterTypes() {
      return this.processedPositionalParameterTypes;
   }

   public boolean isNaturalKeyLookup() {
      return this.isNaturalKeyLookup;
   }

   public void setNaturalKeyLookup(boolean isNaturalKeyLookup) {
      this.isNaturalKeyLookup = isNaturalKeyLookup;
   }

   public void setAutoDiscoverScalarTypes(boolean autodiscovertypes) {
      this.autodiscovertypes = autodiscovertypes;
   }

   public QueryParameters createCopyUsing(RowSelection selection) {
      QueryParameters copy = new QueryParameters(this.positionalParameterTypes, this.positionalParameterValues, this.namedParameters, this.lockOptions, selection, this.isReadOnlyInitialized, this.readOnly, this.cacheable, this.cacheRegion, this.comment, this.collectionKeys, this.optionalObject, this.optionalEntityName, this.optionalId, this.resultTransformer);
      copy.processedSQL = this.processedSQL;
      copy.processedPositionalParameterTypes = this.processedPositionalParameterTypes;
      copy.processedPositionalParameterValues = this.processedPositionalParameterValues;
      return copy;
   }
}
