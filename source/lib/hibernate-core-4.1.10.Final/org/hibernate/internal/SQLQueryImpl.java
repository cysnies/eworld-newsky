package org.hibernate.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.Query;
import org.hibernate.QueryException;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.query.spi.ParameterMetadata;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryJoinReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryScalarReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.type.Type;

public class SQLQueryImpl extends AbstractQueryImpl implements SQLQuery {
   private List queryReturns;
   private List queryReturnBuilders;
   private boolean autoDiscoverTypes;
   private Collection querySpaces;
   private final boolean callable;
   private final LockOptions lockOptions;

   SQLQueryImpl(NamedSQLQueryDefinition queryDef, SessionImplementor session, ParameterMetadata parameterMetadata) {
      super(queryDef.getQueryString(), queryDef.getFlushMode(), session, parameterMetadata);
      this.lockOptions = new LockOptions();
      if (queryDef.getResultSetRef() != null) {
         ResultSetMappingDefinition definition = session.getFactory().getResultSetMapping(queryDef.getResultSetRef());
         if (definition == null) {
            throw new MappingException("Unable to find resultset-ref definition: " + queryDef.getResultSetRef());
         }

         this.queryReturns = Arrays.asList(definition.getQueryReturns());
      } else if (queryDef.getQueryReturns() != null && queryDef.getQueryReturns().length > 0) {
         this.queryReturns = Arrays.asList(queryDef.getQueryReturns());
      } else {
         this.queryReturns = new ArrayList();
      }

      this.querySpaces = queryDef.getQuerySpaces();
      this.callable = queryDef.isCallable();
   }

   SQLQueryImpl(String sql, String[] returnAliases, Class[] returnClasses, LockMode[] lockModes, SessionImplementor session, Collection querySpaces, FlushMode flushMode, ParameterMetadata parameterMetadata) {
      super(sql, flushMode, session, parameterMetadata);
      this.lockOptions = new LockOptions();
      this.queryReturns = new ArrayList(returnAliases.length);

      for(int i = 0; i < returnAliases.length; ++i) {
         NativeSQLQueryRootReturn ret = new NativeSQLQueryRootReturn(returnAliases[i], returnClasses[i].getName(), lockModes == null ? LockMode.NONE : lockModes[i]);
         this.queryReturns.add(ret);
      }

      this.querySpaces = querySpaces;
      this.callable = false;
   }

   SQLQueryImpl(String sql, String[] returnAliases, Class[] returnClasses, SessionImplementor session, ParameterMetadata parameterMetadata) {
      this(sql, returnAliases, returnClasses, (LockMode[])null, session, (Collection)null, (FlushMode)null, parameterMetadata);
   }

   SQLQueryImpl(String sql, SessionImplementor session, ParameterMetadata parameterMetadata) {
      super(sql, (FlushMode)null, session, parameterMetadata);
      this.lockOptions = new LockOptions();
      this.queryReturns = new ArrayList();
      this.querySpaces = null;
      this.callable = false;
   }

   private NativeSQLQueryReturn[] getQueryReturns() {
      return (NativeSQLQueryReturn[])this.queryReturns.toArray(new NativeSQLQueryReturn[this.queryReturns.size()]);
   }

   public List list() throws HibernateException {
      this.verifyParameters();
      this.before();
      Map namedParams = this.getNamedParams();
      NativeSQLQuerySpecification spec = this.generateQuerySpecification(namedParams);

      List var3;
      try {
         var3 = this.getSession().list(spec, this.getQueryParameters(namedParams));
      } finally {
         this.after();
      }

      return var3;
   }

   private NativeSQLQuerySpecification generateQuerySpecification(Map namedParams) {
      return new NativeSQLQuerySpecification(this.expandParameterLists(namedParams), this.getQueryReturns(), this.querySpaces);
   }

   public ScrollableResults scroll(ScrollMode scrollMode) throws HibernateException {
      this.verifyParameters();
      this.before();
      Map namedParams = this.getNamedParams();
      NativeSQLQuerySpecification spec = this.generateQuerySpecification(namedParams);
      QueryParameters qp = this.getQueryParameters(namedParams);
      qp.setScrollMode(scrollMode);

      ScrollableResults var5;
      try {
         var5 = this.getSession().scroll(spec, qp);
      } finally {
         this.after();
      }

      return var5;
   }

   public ScrollableResults scroll() throws HibernateException {
      return this.scroll(ScrollMode.SCROLL_INSENSITIVE);
   }

   public Iterator iterate() throws HibernateException {
      throw new UnsupportedOperationException("SQL queries do not currently support iteration");
   }

   public QueryParameters getQueryParameters(Map namedParams) {
      QueryParameters qp = super.getQueryParameters(namedParams);
      qp.setCallable(this.callable);
      qp.setAutoDiscoverScalarTypes(this.autoDiscoverTypes);
      return qp;
   }

   protected void verifyParameters() {
      this.prepare();
      this.verifyParameters(this.callable);
      boolean noReturns = this.queryReturns == null || this.queryReturns.isEmpty();
      if (noReturns) {
         this.autoDiscoverTypes = noReturns;
      } else {
         for(NativeSQLQueryReturn queryReturn : this.queryReturns) {
            if (queryReturn instanceof NativeSQLQueryScalarReturn) {
               NativeSQLQueryScalarReturn scalar = (NativeSQLQueryScalarReturn)queryReturn;
               if (scalar.getType() == null) {
                  this.autoDiscoverTypes = true;
                  break;
               }
            }
         }
      }

   }

   private void prepare() {
      if (this.queryReturnBuilders != null) {
         if (!this.queryReturnBuilders.isEmpty()) {
            if (this.queryReturns != null) {
               this.queryReturns.clear();
               this.queryReturns = null;
            }

            this.queryReturns = new ArrayList();

            for(ReturnBuilder builder : this.queryReturnBuilders) {
               this.queryReturns.add(builder.buildReturn());
            }

            this.queryReturnBuilders.clear();
         }

         this.queryReturnBuilders = null;
      }

   }

   public String[] getReturnAliases() throws HibernateException {
      throw new UnsupportedOperationException("SQL queries do not currently support returning aliases");
   }

   public Type[] getReturnTypes() throws HibernateException {
      throw new UnsupportedOperationException("not yet implemented for SQL queries");
   }

   public Query setLockMode(String alias, LockMode lockMode) {
      throw new UnsupportedOperationException("cannot set the lock mode for a native SQL query");
   }

   public Query setLockOptions(LockOptions lockOptions) {
      throw new UnsupportedOperationException("cannot set lock options for a native SQL query");
   }

   public LockOptions getLockOptions() {
      return this.lockOptions;
   }

   public SQLQuery addScalar(final String columnAlias, final Type type) {
      if (this.queryReturnBuilders == null) {
         this.queryReturnBuilders = new ArrayList();
      }

      this.queryReturnBuilders.add(new ReturnBuilder() {
         public NativeSQLQueryReturn buildReturn() {
            return new NativeSQLQueryScalarReturn(columnAlias, type);
         }
      });
      return this;
   }

   public SQLQuery addScalar(String columnAlias) {
      return this.addScalar(columnAlias, (Type)null);
   }

   public SQLQuery.RootReturn addRoot(String tableAlias, String entityName) {
      RootReturnBuilder builder = new RootReturnBuilder(tableAlias, entityName);
      if (this.queryReturnBuilders == null) {
         this.queryReturnBuilders = new ArrayList();
      }

      this.queryReturnBuilders.add(builder);
      return builder;
   }

   public SQLQuery.RootReturn addRoot(String tableAlias, Class entityType) {
      return this.addRoot(tableAlias, entityType.getName());
   }

   public SQLQuery addEntity(String entityName) {
      return this.addEntity(StringHelper.unqualify(entityName), entityName);
   }

   public SQLQuery addEntity(String alias, String entityName) {
      this.addRoot(alias, entityName);
      return this;
   }

   public SQLQuery addEntity(String alias, String entityName, LockMode lockMode) {
      this.addRoot(alias, entityName).setLockMode(lockMode);
      return this;
   }

   public SQLQuery addEntity(Class entityType) {
      return this.addEntity(entityType.getName());
   }

   public SQLQuery addEntity(String alias, Class entityClass) {
      return this.addEntity(alias, entityClass.getName());
   }

   public SQLQuery addEntity(String alias, Class entityClass, LockMode lockMode) {
      return this.addEntity(alias, entityClass.getName(), lockMode);
   }

   public SQLQuery.FetchReturn addFetch(String tableAlias, String ownerTableAlias, String joinPropertyName) {
      FetchReturnBuilder builder = new FetchReturnBuilder(tableAlias, ownerTableAlias, joinPropertyName);
      if (this.queryReturnBuilders == null) {
         this.queryReturnBuilders = new ArrayList();
      }

      this.queryReturnBuilders.add(builder);
      return builder;
   }

   public SQLQuery addJoin(String tableAlias, String ownerTableAlias, String joinPropertyName) {
      this.addFetch(tableAlias, ownerTableAlias, joinPropertyName);
      return this;
   }

   public SQLQuery addJoin(String alias, String path) {
      this.createFetchJoin(alias, path);
      return this;
   }

   private SQLQuery.FetchReturn createFetchJoin(String tableAlias, String path) {
      int loc = path.indexOf(46);
      if (loc < 0) {
         throw new QueryException("not a property path: " + path);
      } else {
         String ownerTableAlias = path.substring(0, loc);
         String joinedPropertyName = path.substring(loc + 1);
         return this.addFetch(tableAlias, ownerTableAlias, joinedPropertyName);
      }
   }

   public SQLQuery addJoin(String alias, String path, LockMode lockMode) {
      this.createFetchJoin(alias, path).setLockMode(lockMode);
      return this;
   }

   public SQLQuery setResultSetMapping(String name) {
      ResultSetMappingDefinition mapping = this.session.getFactory().getResultSetMapping(name);
      if (mapping == null) {
         throw new MappingException("Unknown SqlResultSetMapping [" + name + "]");
      } else {
         NativeSQLQueryReturn[] returns = mapping.getQueryReturns();
         this.queryReturns.addAll(Arrays.asList(returns));
         return this;
      }
   }

   public SQLQuery addSynchronizedQuerySpace(String querySpace) {
      if (this.querySpaces == null) {
         this.querySpaces = new ArrayList();
      }

      this.querySpaces.add(querySpace);
      return this;
   }

   public SQLQuery addSynchronizedEntityName(String entityName) {
      return this.addQuerySpaces(this.getSession().getFactory().getEntityPersister(entityName).getQuerySpaces());
   }

   public SQLQuery addSynchronizedEntityClass(Class entityClass) {
      return this.addQuerySpaces(this.getSession().getFactory().getEntityPersister(entityClass.getName()).getQuerySpaces());
   }

   private SQLQuery addQuerySpaces(Serializable[] spaces) {
      if (spaces != null) {
         if (this.querySpaces == null) {
            this.querySpaces = new ArrayList();
         }

         this.querySpaces.addAll(Arrays.asList((String[])spaces));
      }

      return this;
   }

   public int executeUpdate() throws HibernateException {
      Map namedParams = this.getNamedParams();
      this.before();

      int var2;
      try {
         var2 = this.getSession().executeNativeUpdate(this.generateQuerySpecification(namedParams), this.getQueryParameters(namedParams));
      } finally {
         this.after();
      }

      return var2;
   }

   private class RootReturnBuilder implements SQLQuery.RootReturn, ReturnBuilder {
      private final String alias;
      private final String entityName;
      private LockMode lockMode;
      private Map propertyMappings;

      private RootReturnBuilder(String alias, String entityName) {
         this.lockMode = LockMode.READ;
         this.alias = alias;
         this.entityName = entityName;
      }

      public SQLQuery.RootReturn setLockMode(LockMode lockMode) {
         this.lockMode = lockMode;
         return this;
      }

      public SQLQuery.RootReturn setDiscriminatorAlias(String alias) {
         this.addProperty("class", alias);
         return this;
      }

      public SQLQuery.RootReturn addProperty(String propertyName, String columnAlias) {
         this.addProperty(propertyName).addColumnAlias(columnAlias);
         return this;
      }

      public SQLQuery.ReturnProperty addProperty(final String propertyName) {
         if (this.propertyMappings == null) {
            this.propertyMappings = new HashMap();
         }

         return new SQLQuery.ReturnProperty() {
            public SQLQuery.ReturnProperty addColumnAlias(String columnAlias) {
               String[] columnAliases = (String[])RootReturnBuilder.this.propertyMappings.get(propertyName);
               if (columnAliases == null) {
                  columnAliases = new String[]{columnAlias};
               } else {
                  String[] newColumnAliases = new String[columnAliases.length + 1];
                  System.arraycopy(columnAliases, 0, newColumnAliases, 0, columnAliases.length);
                  newColumnAliases[columnAliases.length] = columnAlias;
                  columnAliases = newColumnAliases;
               }

               RootReturnBuilder.this.propertyMappings.put(propertyName, columnAliases);
               return this;
            }
         };
      }

      public NativeSQLQueryReturn buildReturn() {
         return new NativeSQLQueryRootReturn(this.alias, this.entityName, this.propertyMappings, this.lockMode);
      }
   }

   private class FetchReturnBuilder implements SQLQuery.FetchReturn, ReturnBuilder {
      private final String alias;
      private String ownerTableAlias;
      private final String joinedPropertyName;
      private LockMode lockMode;
      private Map propertyMappings;

      private FetchReturnBuilder(String alias, String ownerTableAlias, String joinedPropertyName) {
         this.lockMode = LockMode.READ;
         this.alias = alias;
         this.ownerTableAlias = ownerTableAlias;
         this.joinedPropertyName = joinedPropertyName;
      }

      public SQLQuery.FetchReturn setLockMode(LockMode lockMode) {
         this.lockMode = lockMode;
         return this;
      }

      public SQLQuery.FetchReturn addProperty(String propertyName, String columnAlias) {
         this.addProperty(propertyName).addColumnAlias(columnAlias);
         return this;
      }

      public SQLQuery.ReturnProperty addProperty(final String propertyName) {
         if (this.propertyMappings == null) {
            this.propertyMappings = new HashMap();
         }

         return new SQLQuery.ReturnProperty() {
            public SQLQuery.ReturnProperty addColumnAlias(String columnAlias) {
               String[] columnAliases = (String[])FetchReturnBuilder.this.propertyMappings.get(propertyName);
               if (columnAliases == null) {
                  columnAliases = new String[]{columnAlias};
               } else {
                  String[] newColumnAliases = new String[columnAliases.length + 1];
                  System.arraycopy(columnAliases, 0, newColumnAliases, 0, columnAliases.length);
                  newColumnAliases[columnAliases.length] = columnAlias;
                  columnAliases = newColumnAliases;
               }

               FetchReturnBuilder.this.propertyMappings.put(propertyName, columnAliases);
               return this;
            }
         };
      }

      public NativeSQLQueryReturn buildReturn() {
         return new NativeSQLQueryJoinReturn(this.alias, this.ownerTableAlias, this.joinedPropertyName, this.propertyMappings, this.lockMode);
      }
   }

   private interface ReturnBuilder {
      NativeSQLQueryReturn buildReturn();
   }
}
