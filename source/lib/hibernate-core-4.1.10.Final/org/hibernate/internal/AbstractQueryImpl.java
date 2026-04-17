package org.hibernate.internal;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.MarkerObject;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.property.Getter;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.SerializableType;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class AbstractQueryImpl implements Query {
   private static final CoreMessageLogger log = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractQueryImpl.class.getName());
   private static final Object UNSET_PARAMETER = new MarkerObject("<unset parameter>");
   private static final Object UNSET_TYPE = new MarkerObject("<unset type>");
   private final String queryString;
   protected final SessionImplementor session;
   protected final ParameterMetadata parameterMetadata;
   private List values = new ArrayList(4);
   private List types = new ArrayList(4);
   private Map namedParameters = new HashMap(4);
   private Map namedParameterLists = new HashMap(4);
   private Object optionalObject;
   private Serializable optionalId;
   private String optionalEntityName;
   private RowSelection selection;
   private boolean cacheable;
   private String cacheRegion;
   private String comment;
   private FlushMode flushMode;
   private CacheMode cacheMode;
   private FlushMode sessionFlushMode;
   private CacheMode sessionCacheMode;
   private Serializable collectionKey;
   private Boolean readOnly;
   private ResultTransformer resultTransformer;

   public AbstractQueryImpl(String queryString, FlushMode flushMode, SessionImplementor session, ParameterMetadata parameterMetadata) {
      this.session = session;
      this.queryString = queryString;
      this.selection = new RowSelection();
      this.flushMode = flushMode;
      this.cacheMode = null;
      this.parameterMetadata = parameterMetadata;
   }

   public ParameterMetadata getParameterMetadata() {
      return this.parameterMetadata;
   }

   public String toString() {
      return StringHelper.unqualify(this.getClass().getName()) + '(' + this.queryString + ')';
   }

   public final String getQueryString() {
      return this.queryString;
   }

   public RowSelection getSelection() {
      return this.selection;
   }

   public Query setFlushMode(FlushMode flushMode) {
      this.flushMode = flushMode;
      return this;
   }

   public Query setCacheMode(CacheMode cacheMode) {
      this.cacheMode = cacheMode;
      return this;
   }

   public CacheMode getCacheMode() {
      return this.cacheMode;
   }

   public Query setCacheable(boolean cacheable) {
      this.cacheable = cacheable;
      return this;
   }

   public Query setCacheRegion(String cacheRegion) {
      if (cacheRegion != null) {
         this.cacheRegion = cacheRegion.trim();
      }

      return this;
   }

   public Query setComment(String comment) {
      this.comment = comment;
      return this;
   }

   public Query setFirstResult(int firstResult) {
      this.selection.setFirstRow(firstResult);
      return this;
   }

   public Query setMaxResults(int maxResults) {
      if (maxResults < 0) {
         this.selection.setMaxRows((Integer)null);
      } else {
         this.selection.setMaxRows(maxResults);
      }

      return this;
   }

   public Query setTimeout(int timeout) {
      this.selection.setTimeout(timeout);
      return this;
   }

   public Query setFetchSize(int fetchSize) {
      this.selection.setFetchSize(fetchSize);
      return this;
   }

   public Type[] getReturnTypes() throws HibernateException {
      return this.session.getFactory().getReturnTypes(this.queryString);
   }

   public String[] getReturnAliases() throws HibernateException {
      return this.session.getFactory().getReturnAliases(this.queryString);
   }

   public Query setCollectionKey(Serializable collectionKey) {
      this.collectionKey = collectionKey;
      return this;
   }

   public boolean isReadOnly() {
      return this.readOnly == null ? this.getSession().getPersistenceContext().isDefaultReadOnly() : this.readOnly;
   }

   public Query setReadOnly(boolean readOnly) {
      this.readOnly = readOnly;
      return this;
   }

   public Query setResultTransformer(ResultTransformer transformer) {
      this.resultTransformer = transformer;
      return this;
   }

   public void setOptionalEntityName(String optionalEntityName) {
      this.optionalEntityName = optionalEntityName;
   }

   public void setOptionalId(Serializable optionalId) {
      this.optionalId = optionalId;
   }

   public void setOptionalObject(Object optionalObject) {
      this.optionalObject = optionalObject;
   }

   SessionImplementor getSession() {
      return this.session;
   }

   public abstract LockOptions getLockOptions();

   protected Map getNamedParams() {
      return new HashMap(this.namedParameters);
   }

   public String[] getNamedParameters() throws HibernateException {
      return ArrayHelper.toStringArray((Collection)this.parameterMetadata.getNamedParameterNames());
   }

   public boolean hasNamedParameters() {
      return this.parameterMetadata.getNamedParameterNames().size() > 0;
   }

   protected Map getNamedParameterLists() {
      return this.namedParameterLists;
   }

   protected List getValues() {
      return this.values;
   }

   protected List getTypes() {
      return this.types;
   }

   protected void verifyParameters() throws QueryException {
      this.verifyParameters(false);
   }

   protected void verifyParameters(boolean reserveFirstParameter) throws HibernateException {
      if (this.parameterMetadata.getNamedParameterNames().size() != this.namedParameters.size() + this.namedParameterLists.size()) {
         Set missingParams = new HashSet(this.parameterMetadata.getNamedParameterNames());
         missingParams.removeAll(this.namedParameterLists.keySet());
         missingParams.removeAll(this.namedParameters.keySet());
         throw new QueryException("Not all named parameters have been set: " + missingParams, this.getQueryString());
      } else {
         int positionalValueSpan = 0;

         for(int i = 0; i < this.values.size(); ++i) {
            Object object = this.types.get(i);
            if (this.values.get(i) != UNSET_PARAMETER && object != UNSET_TYPE) {
               positionalValueSpan += ((Type)object).getColumnSpan(this.session.getFactory());
            } else if (!reserveFirstParameter || i != 0) {
               throw new QueryException("Unset positional parameter at position: " + i, this.getQueryString());
            }
         }

         if (this.parameterMetadata.getOrdinalParameterCount() != positionalValueSpan) {
            if (reserveFirstParameter && this.parameterMetadata.getOrdinalParameterCount() - 1 != positionalValueSpan) {
               throw new QueryException("Expected positional parameter count: " + (this.parameterMetadata.getOrdinalParameterCount() - 1) + ", actual parameters: " + this.values, this.getQueryString());
            }

            if (!reserveFirstParameter) {
               throw new QueryException("Expected positional parameter count: " + this.parameterMetadata.getOrdinalParameterCount() + ", actual parameters: " + this.values, this.getQueryString());
            }
         }

      }
   }

   public Query setParameter(int position, Object val, Type type) {
      if (this.parameterMetadata.getOrdinalParameterCount() == 0) {
         throw new IllegalArgumentException("No positional parameters in query: " + this.getQueryString());
      } else if (position >= 0 && position <= this.parameterMetadata.getOrdinalParameterCount() - 1) {
         int size = this.values.size();
         if (position < size) {
            this.values.set(position, val);
            this.types.set(position, type);
         } else {
            for(int i = 0; i < position - size; ++i) {
               this.values.add(UNSET_PARAMETER);
               this.types.add(UNSET_TYPE);
            }

            this.values.add(val);
            this.types.add(type);
         }

         return this;
      } else {
         throw new IllegalArgumentException("Positional parameter does not exist: " + position + " in query: " + this.getQueryString());
      }
   }

   public Query setParameter(String name, Object val, Type type) {
      if (!this.parameterMetadata.getNamedParameterNames().contains(name)) {
         throw new IllegalArgumentException("Parameter " + name + " does not exist as a named parameter in [" + this.getQueryString() + "]");
      } else {
         this.namedParameters.put(name, new TypedValue(type, val));
         return this;
      }
   }

   public Query setParameter(int position, Object val) throws HibernateException {
      if (val == null) {
         this.setParameter(position, val, StandardBasicTypes.SERIALIZABLE);
      } else {
         this.setParameter(position, val, this.determineType(position, val));
      }

      return this;
   }

   public Query setParameter(String name, Object val) throws HibernateException {
      if (val == null) {
         Type type = this.parameterMetadata.getNamedParameterExpectedType(name);
         if (type == null) {
            type = StandardBasicTypes.SERIALIZABLE;
         }

         this.setParameter(name, val, type);
      } else {
         this.setParameter(name, val, this.determineType(name, val));
      }

      return this;
   }

   protected Type determineType(int paramPosition, Object paramValue, Type defaultType) {
      Type type = this.parameterMetadata.getOrdinalParameterExpectedType(paramPosition + 1);
      if (type == null) {
         type = defaultType;
      }

      return type;
   }

   protected Type determineType(int paramPosition, Object paramValue) throws HibernateException {
      Type type = this.parameterMetadata.getOrdinalParameterExpectedType(paramPosition + 1);
      if (type == null) {
         type = this.guessType(paramValue);
      }

      return type;
   }

   protected Type determineType(String paramName, Object paramValue, Type defaultType) {
      Type type = this.parameterMetadata.getNamedParameterExpectedType(paramName);
      if (type == null) {
         type = defaultType;
      }

      return type;
   }

   protected Type determineType(String paramName, Object paramValue) throws HibernateException {
      Type type = this.parameterMetadata.getNamedParameterExpectedType(paramName);
      if (type == null) {
         type = this.guessType(paramValue);
      }

      return type;
   }

   protected Type determineType(String paramName, Class clazz) throws HibernateException {
      Type type = this.parameterMetadata.getNamedParameterExpectedType(paramName);
      if (type == null) {
         type = this.guessType(clazz);
      }

      return type;
   }

   private Type guessType(Object param) throws HibernateException {
      Class clazz = HibernateProxyHelper.getClassWithoutInitializingProxy(param);
      return this.guessType(clazz);
   }

   private Type guessType(Class clazz) throws HibernateException {
      String typename = clazz.getName();
      Type type = this.session.getFactory().getTypeResolver().heuristicType(typename);
      boolean serializable = type != null && type instanceof SerializableType;
      if (type != null && !serializable) {
         return type;
      } else {
         try {
            this.session.getFactory().getEntityPersister(clazz.getName());
         } catch (MappingException var6) {
            if (serializable) {
               return type;
            }

            throw new HibernateException("Could not determine a type for class: " + typename);
         }

         return ((Session)this.session).getTypeHelper().entity(clazz);
      }
   }

   public Query setString(int position, String val) {
      this.setParameter(position, val, StandardBasicTypes.STRING);
      return this;
   }

   public Query setCharacter(int position, char val) {
      this.setParameter(position, new Character(val), StandardBasicTypes.CHARACTER);
      return this;
   }

   public Query setBoolean(int position, boolean val) {
      Boolean valueToUse = val;
      Type typeToUse = this.determineType(position, valueToUse, StandardBasicTypes.BOOLEAN);
      this.setParameter(position, valueToUse, typeToUse);
      return this;
   }

   public Query setByte(int position, byte val) {
      this.setParameter(position, val, StandardBasicTypes.BYTE);
      return this;
   }

   public Query setShort(int position, short val) {
      this.setParameter(position, val, StandardBasicTypes.SHORT);
      return this;
   }

   public Query setInteger(int position, int val) {
      this.setParameter(position, val, StandardBasicTypes.INTEGER);
      return this;
   }

   public Query setLong(int position, long val) {
      this.setParameter(position, val, StandardBasicTypes.LONG);
      return this;
   }

   public Query setFloat(int position, float val) {
      this.setParameter(position, val, StandardBasicTypes.FLOAT);
      return this;
   }

   public Query setDouble(int position, double val) {
      this.setParameter(position, val, StandardBasicTypes.DOUBLE);
      return this;
   }

   public Query setBinary(int position, byte[] val) {
      this.setParameter(position, val, StandardBasicTypes.BINARY);
      return this;
   }

   public Query setText(int position, String val) {
      this.setParameter(position, val, StandardBasicTypes.TEXT);
      return this;
   }

   public Query setSerializable(int position, Serializable val) {
      this.setParameter(position, val, StandardBasicTypes.SERIALIZABLE);
      return this;
   }

   public Query setDate(int position, Date date) {
      this.setParameter(position, date, StandardBasicTypes.DATE);
      return this;
   }

   public Query setTime(int position, Date date) {
      this.setParameter(position, date, StandardBasicTypes.TIME);
      return this;
   }

   public Query setTimestamp(int position, Date date) {
      this.setParameter(position, date, StandardBasicTypes.TIMESTAMP);
      return this;
   }

   public Query setEntity(int position, Object val) {
      this.setParameter(position, val, ((Session)this.session).getTypeHelper().entity(this.resolveEntityName(val)));
      return this;
   }

   private String resolveEntityName(Object val) {
      if (val == null) {
         throw new IllegalArgumentException("entity for parameter binding cannot be null");
      } else {
         return this.session.bestGuessEntityName(val);
      }
   }

   public Query setLocale(int position, Locale locale) {
      this.setParameter(position, locale, StandardBasicTypes.LOCALE);
      return this;
   }

   public Query setCalendar(int position, Calendar calendar) {
      this.setParameter(position, calendar, StandardBasicTypes.CALENDAR);
      return this;
   }

   public Query setCalendarDate(int position, Calendar calendar) {
      this.setParameter(position, calendar, StandardBasicTypes.CALENDAR_DATE);
      return this;
   }

   public Query setBinary(String name, byte[] val) {
      this.setParameter(name, val, StandardBasicTypes.BINARY);
      return this;
   }

   public Query setText(String name, String val) {
      this.setParameter(name, val, StandardBasicTypes.TEXT);
      return this;
   }

   public Query setBoolean(String name, boolean val) {
      Boolean valueToUse = val;
      Type typeToUse = this.determineType(name, valueToUse, StandardBasicTypes.BOOLEAN);
      this.setParameter(name, valueToUse, typeToUse);
      return this;
   }

   public Query setByte(String name, byte val) {
      this.setParameter(name, val, StandardBasicTypes.BYTE);
      return this;
   }

   public Query setCharacter(String name, char val) {
      this.setParameter(name, val, StandardBasicTypes.CHARACTER);
      return this;
   }

   public Query setDate(String name, Date date) {
      this.setParameter(name, date, StandardBasicTypes.DATE);
      return this;
   }

   public Query setDouble(String name, double val) {
      this.setParameter(name, val, StandardBasicTypes.DOUBLE);
      return this;
   }

   public Query setEntity(String name, Object val) {
      this.setParameter(name, val, ((Session)this.session).getTypeHelper().entity(this.resolveEntityName(val)));
      return this;
   }

   public Query setFloat(String name, float val) {
      this.setParameter(name, val, StandardBasicTypes.FLOAT);
      return this;
   }

   public Query setInteger(String name, int val) {
      this.setParameter(name, val, StandardBasicTypes.INTEGER);
      return this;
   }

   public Query setLocale(String name, Locale locale) {
      this.setParameter(name, locale, StandardBasicTypes.LOCALE);
      return this;
   }

   public Query setCalendar(String name, Calendar calendar) {
      this.setParameter(name, calendar, StandardBasicTypes.CALENDAR);
      return this;
   }

   public Query setCalendarDate(String name, Calendar calendar) {
      this.setParameter(name, calendar, StandardBasicTypes.CALENDAR_DATE);
      return this;
   }

   public Query setLong(String name, long val) {
      this.setParameter(name, val, StandardBasicTypes.LONG);
      return this;
   }

   public Query setSerializable(String name, Serializable val) {
      this.setParameter(name, val, StandardBasicTypes.SERIALIZABLE);
      return this;
   }

   public Query setShort(String name, short val) {
      this.setParameter(name, val, StandardBasicTypes.SHORT);
      return this;
   }

   public Query setString(String name, String val) {
      this.setParameter(name, val, StandardBasicTypes.STRING);
      return this;
   }

   public Query setTime(String name, Date date) {
      this.setParameter(name, date, StandardBasicTypes.TIME);
      return this;
   }

   public Query setTimestamp(String name, Date date) {
      this.setParameter(name, date, StandardBasicTypes.TIMESTAMP);
      return this;
   }

   public Query setBigDecimal(int position, BigDecimal number) {
      this.setParameter(position, number, StandardBasicTypes.BIG_DECIMAL);
      return this;
   }

   public Query setBigDecimal(String name, BigDecimal number) {
      this.setParameter(name, number, StandardBasicTypes.BIG_DECIMAL);
      return this;
   }

   public Query setBigInteger(int position, BigInteger number) {
      this.setParameter(position, number, StandardBasicTypes.BIG_INTEGER);
      return this;
   }

   public Query setBigInteger(String name, BigInteger number) {
      this.setParameter(name, number, StandardBasicTypes.BIG_INTEGER);
      return this;
   }

   public Query setParameterList(String name, Collection vals, Type type) throws HibernateException {
      if (!this.parameterMetadata.getNamedParameterNames().contains(name)) {
         throw new IllegalArgumentException("Parameter " + name + " does not exist as a named parameter in [" + this.getQueryString() + "]");
      } else {
         this.namedParameterLists.put(name, new TypedValue(type, vals));
         return this;
      }
   }

   protected String expandParameterLists(Map namedParamsCopy) {
      String query = this.queryString;

      for(Map.Entry me : this.namedParameterLists.entrySet()) {
         query = this.expandParameterList(query, (String)me.getKey(), (TypedValue)me.getValue(), namedParamsCopy);
      }

      return query;
   }

   private String expandParameterList(String query, String name, TypedValue typedList, Map namedParamsCopy) {
      Collection vals = (Collection)typedList.getValue();
      Dialect dialect = this.session.getFactory().getDialect();
      int inExprLimit = dialect.getInExpressionCountLimit();
      if (inExprLimit > 0 && vals.size() > inExprLimit) {
         log.tooManyInExpressions(dialect.getClass().getName(), inExprLimit, name, vals.size());
      }

      Type type = typedList.getType();
      boolean isJpaPositionalParam = this.parameterMetadata.getNamedParameterDescriptor(name).isJpaStyle();
      String paramPrefix = isJpaPositionalParam ? "?" : ":";
      String placeholder = (new StringBuilder(paramPrefix.length() + name.length())).append(paramPrefix).append(name).toString();
      if (query == null) {
         return query;
      } else {
         int loc = query.indexOf(placeholder);
         if (loc < 0) {
            return query;
         } else {
            String beforePlaceholder = query.substring(0, loc);
            String afterPlaceholder = query.substring(loc + placeholder.length());
            boolean isEnclosedInParens = StringHelper.getLastNonWhitespaceCharacter(beforePlaceholder) == '(' && StringHelper.getFirstNonWhitespaceCharacter(afterPlaceholder) == ')';
            if (vals.size() == 1 && isEnclosedInParens) {
               namedParamsCopy.put(name, new TypedValue(type, vals.iterator().next()));
               return query;
            } else {
               StringBuilder list = new StringBuilder(16);
               Iterator iter = vals.iterator();
               int i = 0;

               while(iter.hasNext()) {
                  String alias = (isJpaPositionalParam ? 'x' + name : name) + i++ + '_';
                  namedParamsCopy.put(alias, new TypedValue(type, iter.next()));
                  list.append(":").append(alias);
                  if (iter.hasNext()) {
                     list.append(", ");
                  }
               }

               return StringHelper.replace(beforePlaceholder, afterPlaceholder, placeholder.toString(), list.toString(), true, true);
            }
         }
      }
   }

   public Query setParameterList(String name, Collection vals) throws HibernateException {
      if (vals == null) {
         throw new QueryException("Collection must be not null!");
      } else {
         if (vals.size() == 0) {
            this.setParameterList(name, (Collection)vals, (Type)null);
         } else {
            this.setParameterList(name, vals, this.determineType(name, vals.iterator().next()));
         }

         return this;
      }
   }

   public Query setParameterList(String name, Object[] vals, Type type) throws HibernateException {
      return this.setParameterList(name, (Collection)Arrays.asList(vals), type);
   }

   public Query setParameterList(String name, Object[] vals) throws HibernateException {
      return this.setParameterList(name, (Collection)Arrays.asList(vals));
   }

   public Query setProperties(Map map) throws HibernateException {
      String[] params = this.getNamedParameters();

      for(int i = 0; i < params.length; ++i) {
         String namedParam = params[i];
         Object object = map.get(namedParam);
         if (object != null) {
            Class retType = object.getClass();
            if (Collection.class.isAssignableFrom(retType)) {
               this.setParameterList(namedParam, (Collection)object);
            } else if (retType.isArray()) {
               this.setParameterList(namedParam, object);
            } else {
               this.setParameter(namedParam, object, this.determineType(namedParam, retType));
            }
         }
      }

      return this;
   }

   public Query setProperties(Object bean) throws HibernateException {
      Class clazz = bean.getClass();
      String[] params = this.getNamedParameters();

      for(int i = 0; i < params.length; ++i) {
         String namedParam = params[i];

         try {
            Getter getter = ReflectHelper.getGetter(clazz, namedParam);
            Class retType = getter.getReturnType();
            Object object = getter.get(bean);
            if (Collection.class.isAssignableFrom(retType)) {
               this.setParameterList(namedParam, (Collection)object);
            } else if (retType.isArray()) {
               this.setParameterList(namedParam, object);
            } else {
               this.setParameter(namedParam, object, this.determineType(namedParam, retType));
            }
         } catch (PropertyNotFoundException var9) {
         }
      }

      return this;
   }

   public Query setParameters(Object[] values, Type[] types) {
      this.values = Arrays.asList(values);
      this.types = Arrays.asList(types);
      return this;
   }

   public Object uniqueResult() throws HibernateException {
      return uniqueElement(this.list());
   }

   static Object uniqueElement(List list) throws NonUniqueResultException {
      int size = list.size();
      if (size == 0) {
         return null;
      } else {
         Object first = list.get(0);

         for(int i = 1; i < size; ++i) {
            if (list.get(i) != first) {
               throw new NonUniqueResultException(list.size());
            }
         }

         return first;
      }
   }

   protected RowSelection getRowSelection() {
      return this.selection;
   }

   public Type[] typeArray() {
      return ArrayHelper.toTypeArray(this.getTypes());
   }

   public Object[] valueArray() {
      return this.getValues().toArray();
   }

   public QueryParameters getQueryParameters(Map namedParams) {
      return new QueryParameters(this.typeArray(), this.valueArray(), namedParams, this.getLockOptions(), this.getSelection(), true, this.isReadOnly(), this.cacheable, this.cacheRegion, this.comment, this.collectionKey == null ? null : new Serializable[]{this.collectionKey}, this.optionalObject, this.optionalEntityName, this.optionalId, this.resultTransformer);
   }

   protected void before() {
      if (this.flushMode != null) {
         this.sessionFlushMode = this.getSession().getFlushMode();
         this.getSession().setFlushMode(this.flushMode);
      }

      if (this.cacheMode != null) {
         this.sessionCacheMode = this.getSession().getCacheMode();
         this.getSession().setCacheMode(this.cacheMode);
      }

   }

   protected void after() {
      if (this.sessionFlushMode != null) {
         this.getSession().setFlushMode(this.sessionFlushMode);
         this.sessionFlushMode = null;
      }

      if (this.sessionCacheMode != null) {
         this.getSession().setCacheMode(this.sessionCacheMode);
         this.sessionCacheMode = null;
      }

   }
}
