package org.hibernate.loader.custom;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.loader.EntityAliases;
import org.hibernate.loader.Loader;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class CustomLoader extends Loader {
   private final String sql;
   private final Set querySpaces = new HashSet();
   private final Map namedParameterBindPoints;
   private final Queryable[] entityPersisters;
   private final int[] entiytOwners;
   private final EntityAliases[] entityAliases;
   private final QueryableCollection[] collectionPersisters;
   private final int[] collectionOwners;
   private final CollectionAliases[] collectionAliases;
   private final LockMode[] lockModes;
   private boolean[] includeInResultRow;
   private final ResultRowProcessor rowProcessor;
   private Type[] resultTypes;
   private String[] transformerAliases;

   public CustomLoader(CustomQuery customQuery, SessionFactoryImplementor factory) {
      super(factory);
      this.sql = customQuery.getSQL();
      this.querySpaces.addAll(customQuery.getQuerySpaces());
      this.namedParameterBindPoints = customQuery.getNamedParameterBindPoints();
      List entityPersisters = new ArrayList();
      List entityOwners = new ArrayList();
      List entityAliases = new ArrayList();
      List collectionPersisters = new ArrayList();
      List collectionOwners = new ArrayList();
      List collectionAliases = new ArrayList();
      List lockModes = new ArrayList();
      List resultColumnProcessors = new ArrayList();
      List nonScalarReturnList = new ArrayList();
      List resultTypes = new ArrayList();
      List specifiedAliases = new ArrayList();
      int returnableCounter = 0;
      boolean hasScalars = false;
      List includeInResultRowList = new ArrayList();

      for(Return rtn : customQuery.getCustomQueryReturns()) {
         if (rtn instanceof ScalarReturn) {
            ScalarReturn scalarRtn = (ScalarReturn)rtn;
            resultTypes.add(scalarRtn.getType());
            specifiedAliases.add(scalarRtn.getColumnAlias());
            resultColumnProcessors.add(new ScalarResultColumnProcessor(StringHelper.unquote(scalarRtn.getColumnAlias(), factory.getDialect()), scalarRtn.getType()));
            includeInResultRowList.add(true);
            hasScalars = true;
         } else if (rtn instanceof RootReturn) {
            RootReturn rootRtn = (RootReturn)rtn;
            Queryable persister = (Queryable)factory.getEntityPersister(rootRtn.getEntityName());
            entityPersisters.add(persister);
            lockModes.add(rootRtn.getLockMode());
            resultColumnProcessors.add(new NonScalarResultColumnProcessor(returnableCounter++));
            nonScalarReturnList.add(rtn);
            entityOwners.add(-1);
            resultTypes.add(persister.getType());
            specifiedAliases.add(rootRtn.getAlias());
            entityAliases.add(rootRtn.getEntityAliases());
            ArrayHelper.addAll(this.querySpaces, persister.getQuerySpaces());
            includeInResultRowList.add(true);
         } else if (rtn instanceof CollectionReturn) {
            CollectionReturn collRtn = (CollectionReturn)rtn;
            String role = collRtn.getOwnerEntityName() + "." + collRtn.getOwnerProperty();
            QueryableCollection persister = (QueryableCollection)factory.getCollectionPersister(role);
            collectionPersisters.add(persister);
            lockModes.add(collRtn.getLockMode());
            resultColumnProcessors.add(new NonScalarResultColumnProcessor(returnableCounter++));
            nonScalarReturnList.add(rtn);
            collectionOwners.add(-1);
            resultTypes.add(persister.getType());
            specifiedAliases.add(collRtn.getAlias());
            collectionAliases.add(collRtn.getCollectionAliases());
            Type elementType = persister.getElementType();
            if (elementType.isEntityType()) {
               Queryable elementPersister = (Queryable)((EntityType)elementType).getAssociatedJoinable(factory);
               entityPersisters.add(elementPersister);
               entityOwners.add(-1);
               entityAliases.add(collRtn.getElementEntityAliases());
               ArrayHelper.addAll(this.querySpaces, elementPersister.getQuerySpaces());
            }

            includeInResultRowList.add(true);
         } else if (rtn instanceof EntityFetchReturn) {
            EntityFetchReturn fetchRtn = (EntityFetchReturn)rtn;
            NonScalarReturn ownerDescriptor = fetchRtn.getOwner();
            int ownerIndex = nonScalarReturnList.indexOf(ownerDescriptor);
            entityOwners.add(ownerIndex);
            lockModes.add(fetchRtn.getLockMode());
            Queryable ownerPersister = this.determineAppropriateOwnerPersister(ownerDescriptor);
            EntityType fetchedType = (EntityType)ownerPersister.getPropertyType(fetchRtn.getOwnerProperty());
            String entityName = fetchedType.getAssociatedEntityName(this.getFactory());
            Queryable persister = (Queryable)factory.getEntityPersister(entityName);
            entityPersisters.add(persister);
            nonScalarReturnList.add(rtn);
            specifiedAliases.add(fetchRtn.getAlias());
            entityAliases.add(fetchRtn.getEntityAliases());
            ArrayHelper.addAll(this.querySpaces, persister.getQuerySpaces());
            includeInResultRowList.add(false);
         } else {
            if (!(rtn instanceof CollectionFetchReturn)) {
               throw new HibernateException("unexpected custom query return type : " + rtn.getClass().getName());
            }

            CollectionFetchReturn fetchRtn = (CollectionFetchReturn)rtn;
            NonScalarReturn ownerDescriptor = fetchRtn.getOwner();
            int ownerIndex = nonScalarReturnList.indexOf(ownerDescriptor);
            collectionOwners.add(ownerIndex);
            lockModes.add(fetchRtn.getLockMode());
            Queryable ownerPersister = this.determineAppropriateOwnerPersister(ownerDescriptor);
            String role = ownerPersister.getEntityName() + '.' + fetchRtn.getOwnerProperty();
            QueryableCollection persister = (QueryableCollection)factory.getCollectionPersister(role);
            collectionPersisters.add(persister);
            nonScalarReturnList.add(rtn);
            specifiedAliases.add(fetchRtn.getAlias());
            collectionAliases.add(fetchRtn.getCollectionAliases());
            Type elementType = persister.getElementType();
            if (elementType.isEntityType()) {
               Queryable elementPersister = (Queryable)((EntityType)elementType).getAssociatedJoinable(factory);
               entityPersisters.add(elementPersister);
               entityOwners.add(ownerIndex);
               entityAliases.add(fetchRtn.getElementEntityAliases());
               ArrayHelper.addAll(this.querySpaces, elementPersister.getQuerySpaces());
            }

            includeInResultRowList.add(false);
         }
      }

      this.entityPersisters = new Queryable[entityPersisters.size()];

      for(int i = 0; i < entityPersisters.size(); ++i) {
         this.entityPersisters[i] = (Queryable)entityPersisters.get(i);
      }

      this.entiytOwners = ArrayHelper.toIntArray(entityOwners);
      this.entityAliases = new EntityAliases[entityAliases.size()];

      for(int i = 0; i < entityAliases.size(); ++i) {
         this.entityAliases[i] = (EntityAliases)entityAliases.get(i);
      }

      this.collectionPersisters = new QueryableCollection[collectionPersisters.size()];

      for(int i = 0; i < collectionPersisters.size(); ++i) {
         this.collectionPersisters[i] = (QueryableCollection)collectionPersisters.get(i);
      }

      this.collectionOwners = ArrayHelper.toIntArray(collectionOwners);
      this.collectionAliases = new CollectionAliases[collectionAliases.size()];

      for(int i = 0; i < collectionAliases.size(); ++i) {
         this.collectionAliases[i] = (CollectionAliases)collectionAliases.get(i);
      }

      this.lockModes = new LockMode[lockModes.size()];

      for(int i = 0; i < lockModes.size(); ++i) {
         this.lockModes[i] = (LockMode)lockModes.get(i);
      }

      this.resultTypes = ArrayHelper.toTypeArray(resultTypes);
      this.transformerAliases = ArrayHelper.toStringArray((Collection)specifiedAliases);
      this.rowProcessor = new ResultRowProcessor(hasScalars, (ResultColumnProcessor[])resultColumnProcessors.toArray(new ResultColumnProcessor[resultColumnProcessors.size()]));
      this.includeInResultRow = ArrayHelper.toBooleanArray(includeInResultRowList);
   }

   private Queryable determineAppropriateOwnerPersister(NonScalarReturn ownerDescriptor) {
      String entityName = null;
      if (ownerDescriptor instanceof RootReturn) {
         entityName = ((RootReturn)ownerDescriptor).getEntityName();
      } else if (ownerDescriptor instanceof CollectionReturn) {
         CollectionReturn collRtn = (CollectionReturn)ownerDescriptor;
         String role = collRtn.getOwnerEntityName() + "." + collRtn.getOwnerProperty();
         CollectionPersister persister = this.getFactory().getCollectionPersister(role);
         EntityType ownerType = (EntityType)persister.getElementType();
         entityName = ownerType.getAssociatedEntityName(this.getFactory());
      } else if (ownerDescriptor instanceof FetchReturn) {
         FetchReturn fetchRtn = (FetchReturn)ownerDescriptor;
         Queryable persister = this.determineAppropriateOwnerPersister(fetchRtn.getOwner());
         Type ownerType = persister.getPropertyType(fetchRtn.getOwnerProperty());
         if (ownerType.isEntityType()) {
            entityName = ((EntityType)ownerType).getAssociatedEntityName(this.getFactory());
         } else if (ownerType.isCollectionType()) {
            Type ownerCollectionElementType = ((CollectionType)ownerType).getElementType(this.getFactory());
            if (ownerCollectionElementType.isEntityType()) {
               entityName = ((EntityType)ownerCollectionElementType).getAssociatedEntityName(this.getFactory());
            }
         }
      }

      if (entityName == null) {
         throw new HibernateException("Could not determine fetch owner : " + ownerDescriptor);
      } else {
         return (Queryable)this.getFactory().getEntityPersister(entityName);
      }
   }

   protected String getQueryIdentifier() {
      return this.sql;
   }

   protected String getSQLString() {
      return this.sql;
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }

   protected LockMode[] getLockModes(LockOptions lockOptions) {
      return this.lockModes;
   }

   protected Loadable[] getEntityPersisters() {
      return this.entityPersisters;
   }

   protected CollectionPersister[] getCollectionPersisters() {
      return this.collectionPersisters;
   }

   protected int[] getCollectionOwners() {
      return this.collectionOwners;
   }

   protected int[] getOwners() {
      return this.entiytOwners;
   }

   public List list(SessionImplementor session, QueryParameters queryParameters) throws HibernateException {
      return this.list(session, queryParameters, this.querySpaces, this.resultTypes);
   }

   protected String applyLocks(String sql, QueryParameters parameters, Dialect dialect, List afterLoadActions) throws QueryException {
      final LockOptions lockOptions = parameters.getLockOptions();
      if (lockOptions != null && (lockOptions.getLockMode() != LockMode.NONE || lockOptions.getAliasLockCount() != 0)) {
         afterLoadActions.add(new Loader.AfterLoadAction() {
            private final LockOptions originalLockOptions = lockOptions.makeCopy();

            public void afterLoad(SessionImplementor session, Object entity, Loadable persister) {
               ((Session)session).buildLockRequest(this.originalLockOptions).lock(persister.getEntityName(), entity);
            }
         });
         parameters.getLockOptions().setLockMode(LockMode.READ);
         return sql;
      } else {
         return sql;
      }
   }

   public ScrollableResults scroll(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      return this.scroll(queryParameters, this.resultTypes, getHolderInstantiator(queryParameters.getResultTransformer(), this.getReturnAliasesForTransformer()), session);
   }

   private static HolderInstantiator getHolderInstantiator(ResultTransformer resultTransformer, String[] queryReturnAliases) {
      return resultTransformer == null ? HolderInstantiator.NOOP_INSTANTIATOR : new HolderInstantiator(resultTransformer, queryReturnAliases);
   }

   protected String[] getResultRowAliases() {
      return this.transformerAliases;
   }

   protected ResultTransformer resolveResultTransformer(ResultTransformer resultTransformer) {
      return HolderInstantiator.resolveResultTransformer((ResultTransformer)null, resultTransformer);
   }

   protected boolean[] includeInResultRow() {
      return this.includeInResultRow;
   }

   protected Object getResultColumnOrRow(Object[] row, ResultTransformer transformer, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      return this.rowProcessor.buildResultRow(row, rs, transformer != null, session);
   }

   protected Object[] getResultRow(Object[] row, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      return this.rowProcessor.buildResultRow(row, rs, session);
   }

   protected List getResultList(List results, ResultTransformer resultTransformer) throws QueryException {
      HolderInstantiator holderInstantiator = HolderInstantiator.getHolderInstantiator((ResultTransformer)null, resultTransformer, this.getReturnAliasesForTransformer());
      if (!holderInstantiator.isRequired()) {
         return results;
      } else {
         for(int i = 0; i < results.size(); ++i) {
            Object[] row = results.get(i);
            Object result = holderInstantiator.instantiate(row);
            results.set(i, result);
         }

         return resultTransformer.transformList(results);
      }
   }

   private String[] getReturnAliasesForTransformer() {
      return this.transformerAliases;
   }

   protected EntityAliases[] getEntityAliases() {
      return this.entityAliases;
   }

   protected CollectionAliases[] getCollectionAliases() {
      return this.collectionAliases;
   }

   public int[] getNamedParameterLocs(String name) throws QueryException {
      Object loc = this.namedParameterBindPoints.get(name);
      if (loc == null) {
         throw new QueryException("Named parameter does not appear in Query: " + name, this.sql);
      } else {
         return loc instanceof Integer ? new int[]{(Integer)loc} : ArrayHelper.toIntArray((List)loc);
      }
   }

   protected void autoDiscoverTypes(ResultSet rs) {
      try {
         Metadata metadata = new Metadata(this.getFactory(), rs);
         this.rowProcessor.prepareForAutoDiscovery(metadata);
         List<String> aliases = new ArrayList();
         List<Type> types = new ArrayList();

         for(int i = 0; i < this.rowProcessor.columnProcessors.length; ++i) {
            this.rowProcessor.columnProcessors[i].performDiscovery(metadata, types, aliases);
         }

         HashSet<String> aliasesSet = new HashSet();

         for(String alias : aliases) {
            boolean alreadyExisted = !aliasesSet.add(alias);
            if (alreadyExisted) {
               throw new NonUniqueDiscoveredSqlAliasException("Encountered a duplicated sql alias [" + alias + "] during auto-discovery of a native-sql query");
            }
         }

         this.resultTypes = ArrayHelper.toTypeArray(types);
         this.transformerAliases = ArrayHelper.toStringArray((Collection)aliases);
      } catch (SQLException e) {
         throw new HibernateException("Exception while trying to autodiscover types.", e);
      }
   }

   public class ResultRowProcessor {
      private final boolean hasScalars;
      private ResultColumnProcessor[] columnProcessors;

      public ResultRowProcessor(boolean hasScalars, ResultColumnProcessor[] columnProcessors) {
         this.hasScalars = hasScalars || columnProcessors == null || columnProcessors.length == 0;
         this.columnProcessors = columnProcessors;
      }

      public void prepareForAutoDiscovery(Metadata metadata) throws SQLException {
         if (this.columnProcessors == null || this.columnProcessors.length == 0) {
            int columns = metadata.getColumnCount();
            this.columnProcessors = new ResultColumnProcessor[columns];

            for(int i = 1; i <= columns; ++i) {
               this.columnProcessors[i - 1] = CustomLoader.this.new ScalarResultColumnProcessor(i);
            }
         }

      }

      public Object buildResultRow(Object[] data, ResultSet resultSet, boolean hasTransformer, SessionImplementor session) throws SQLException, HibernateException {
         Object[] resultRow = this.buildResultRow(data, resultSet, session);
         return hasTransformer ? resultRow : (resultRow.length == 1 ? resultRow[0] : resultRow);
      }

      public Object[] buildResultRow(Object[] data, ResultSet resultSet, SessionImplementor session) throws SQLException, HibernateException {
         Object[] resultRow;
         if (!this.hasScalars) {
            resultRow = data;
         } else {
            resultRow = new Object[this.columnProcessors.length];

            for(int i = 0; i < this.columnProcessors.length; ++i) {
               resultRow[i] = this.columnProcessors[i].extract(data, resultSet, session);
            }
         }

         return resultRow;
      }
   }

   public class NonScalarResultColumnProcessor implements ResultColumnProcessor {
      private final int position;

      public NonScalarResultColumnProcessor(int position) {
         this.position = position;
      }

      public Object extract(Object[] data, ResultSet resultSet, SessionImplementor session) throws SQLException, HibernateException {
         return data[this.position];
      }

      public void performDiscovery(Metadata metadata, List types, List aliases) {
      }
   }

   public class ScalarResultColumnProcessor implements ResultColumnProcessor {
      private int position = -1;
      private String alias;
      private Type type;

      public ScalarResultColumnProcessor(int position) {
         this.position = position;
      }

      public ScalarResultColumnProcessor(String alias, Type type) {
         this.alias = alias;
         this.type = type;
      }

      public Object extract(Object[] data, ResultSet resultSet, SessionImplementor session) throws SQLException, HibernateException {
         return this.type.nullSafeGet(resultSet, (String)this.alias, session, (Object)null);
      }

      public void performDiscovery(Metadata metadata, List types, List aliases) throws SQLException {
         if (this.alias == null) {
            this.alias = metadata.getColumnName(this.position);
         } else if (this.position < 0) {
            this.position = metadata.resolveColumnPosition(this.alias);
         }

         if (this.type == null) {
            this.type = metadata.getHibernateType(this.position);
         }

         types.add(this.type);
         aliases.add(this.alias);
      }
   }

   private static class Metadata {
      private final SessionFactoryImplementor factory;
      private final ResultSet resultSet;
      private final ResultSetMetaData resultSetMetaData;

      public Metadata(SessionFactoryImplementor factory, ResultSet resultSet) throws HibernateException {
         try {
            this.factory = factory;
            this.resultSet = resultSet;
            this.resultSetMetaData = resultSet.getMetaData();
         } catch (SQLException e) {
            throw new HibernateException("Could not extract result set metadata", e);
         }
      }

      public int getColumnCount() throws HibernateException {
         try {
            return this.resultSetMetaData.getColumnCount();
         } catch (SQLException e) {
            throw new HibernateException("Could not determine result set column count", e);
         }
      }

      public int resolveColumnPosition(String columnName) throws HibernateException {
         try {
            return this.resultSet.findColumn(columnName);
         } catch (SQLException e) {
            throw new HibernateException("Could not resolve column name in result set [" + columnName + "]", e);
         }
      }

      public String getColumnName(int position) throws HibernateException {
         try {
            return this.factory.getDialect().getColumnAliasExtractor().extractColumnAlias(this.resultSetMetaData, position);
         } catch (SQLException e) {
            throw new HibernateException("Could not resolve column name [" + position + "]", e);
         }
      }

      public Type getHibernateType(int columnPos) throws SQLException {
         int columnType = this.resultSetMetaData.getColumnType(columnPos);
         int scale = this.resultSetMetaData.getScale(columnPos);
         int precision = this.resultSetMetaData.getPrecision(columnPos);
         int length = precision;
         if (columnType == 1 && precision == 0) {
            length = this.resultSetMetaData.getColumnDisplaySize(columnPos);
         }

         return this.factory.getTypeResolver().heuristicType(this.factory.getDialect().getHibernateTypeName(columnType, length, precision, scale));
      }
   }

   private interface ResultColumnProcessor {
      Object extract(Object[] var1, ResultSet var2, SessionImplementor var3) throws SQLException, HibernateException;

      void performDiscovery(Metadata var1, List var2, List var3) throws SQLException, HibernateException;
   }
}
