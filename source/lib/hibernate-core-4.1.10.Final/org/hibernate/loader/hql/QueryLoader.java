package org.hibernate.loader.hql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.ScrollableResults;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.hql.internal.ast.QueryTranslatorImpl;
import org.hibernate.hql.internal.ast.tree.AggregatedSelectExpression;
import org.hibernate.hql.internal.ast.tree.FromElement;
import org.hibernate.hql.internal.ast.tree.QueryNode;
import org.hibernate.hql.internal.ast.tree.SelectClause;
import org.hibernate.internal.IteratorImpl;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.BasicLoader;
import org.hibernate.loader.Loader;
import org.hibernate.param.ParameterSpecification;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class QueryLoader extends BasicLoader {
   private QueryTranslatorImpl queryTranslator;
   private Queryable[] entityPersisters;
   private String[] entityAliases;
   private String[] sqlAliases;
   private String[] sqlAliasSuffixes;
   private boolean[] includeInSelect;
   private String[] collectionSuffixes;
   private boolean hasScalars;
   private String[][] scalarColumnNames;
   private Type[] queryReturnTypes;
   private final Map sqlAliasByEntityAlias = new HashMap(8);
   private EntityType[] ownerAssociationTypes;
   private int[] owners;
   private boolean[] entityEagerPropertyFetches;
   private int[] collectionOwners;
   private QueryableCollection[] collectionPersisters;
   private int selectLength;
   private AggregatedSelectExpression aggregatedSelectExpression;
   private String[] queryReturnAliases;
   private LockMode[] defaultLockModes;

   public QueryLoader(QueryTranslatorImpl queryTranslator, SessionFactoryImplementor factory, SelectClause selectClause) {
      super(factory);
      this.queryTranslator = queryTranslator;
      this.initialize(selectClause);
      this.postInstantiate();
   }

   private void initialize(SelectClause selectClause) {
      List fromElementList = selectClause.getFromElementsForLoad();
      this.hasScalars = selectClause.isScalarSelect();
      this.scalarColumnNames = selectClause.getColumnNames();
      this.queryReturnTypes = selectClause.getQueryReturnTypes();
      this.aggregatedSelectExpression = selectClause.getAggregatedSelectExpression();
      this.queryReturnAliases = selectClause.getQueryReturnAliases();
      List collectionFromElements = selectClause.getCollectionFromElements();
      if (collectionFromElements != null && collectionFromElements.size() != 0) {
         int length = collectionFromElements.size();
         this.collectionPersisters = new QueryableCollection[length];
         this.collectionOwners = new int[length];
         this.collectionSuffixes = new String[length];

         for(int i = 0; i < length; ++i) {
            FromElement collectionFromElement = (FromElement)collectionFromElements.get(i);
            this.collectionPersisters[i] = collectionFromElement.getQueryableCollection();
            this.collectionOwners[i] = fromElementList.indexOf(collectionFromElement.getOrigin());
            this.collectionSuffixes[i] = collectionFromElement.getCollectionSuffix();
         }
      }

      int size = fromElementList.size();
      this.entityPersisters = new Queryable[size];
      this.entityEagerPropertyFetches = new boolean[size];
      this.entityAliases = new String[size];
      this.sqlAliases = new String[size];
      this.sqlAliasSuffixes = new String[size];
      this.includeInSelect = new boolean[size];
      this.owners = new int[size];
      this.ownerAssociationTypes = new EntityType[size];

      for(int i = 0; i < size; ++i) {
         FromElement element = (FromElement)fromElementList.get(i);
         this.entityPersisters[i] = (Queryable)element.getEntityPersister();
         if (this.entityPersisters[i] == null) {
            throw new IllegalStateException("No entity persister for " + element.toString());
         }

         this.entityEagerPropertyFetches[i] = element.isAllPropertyFetch();
         this.sqlAliases[i] = element.getTableAlias();
         this.entityAliases[i] = element.getClassAlias();
         this.sqlAliasByEntityAlias.put(this.entityAliases[i], this.sqlAliases[i]);
         this.sqlAliasSuffixes[i] = size == 1 ? "" : Integer.toString(i) + "_";
         this.includeInSelect[i] = !element.isFetch();
         if (this.includeInSelect[i]) {
            ++this.selectLength;
         }

         this.owners[i] = -1;
         if (element.isFetch() && !element.isCollectionJoin() && element.getQueryableCollection() == null && element.getDataType().isEntityType()) {
            EntityType entityType = (EntityType)element.getDataType();
            if (entityType.isOneToOne()) {
               this.owners[i] = fromElementList.indexOf(element.getOrigin());
            }

            this.ownerAssociationTypes[i] = entityType;
         }
      }

      this.defaultLockModes = ArrayHelper.fillArray(LockMode.NONE, size);
   }

   public AggregatedSelectExpression getAggregatedSelectExpression() {
      return this.aggregatedSelectExpression;
   }

   public final void validateScrollability() throws HibernateException {
      this.queryTranslator.validateScrollability();
   }

   protected boolean needsFetchingScroll() {
      return this.queryTranslator.containsCollectionFetches();
   }

   public Loadable[] getEntityPersisters() {
      return this.entityPersisters;
   }

   public String[] getAliases() {
      return this.sqlAliases;
   }

   public String[] getSqlAliasSuffixes() {
      return this.sqlAliasSuffixes;
   }

   public String[] getSuffixes() {
      return this.getSqlAliasSuffixes();
   }

   public String[] getCollectionSuffixes() {
      return this.collectionSuffixes;
   }

   protected String getQueryIdentifier() {
      return this.queryTranslator.getQueryIdentifier();
   }

   protected String getSQLString() {
      return this.queryTranslator.getSQLString();
   }

   protected CollectionPersister[] getCollectionPersisters() {
      return this.collectionPersisters;
   }

   protected int[] getCollectionOwners() {
      return this.collectionOwners;
   }

   protected boolean[] getEntityEagerPropertyFetches() {
      return this.entityEagerPropertyFetches;
   }

   protected int[] getOwners() {
      return this.owners;
   }

   protected EntityType[] getOwnerAssociationTypes() {
      return this.ownerAssociationTypes;
   }

   protected boolean isSubselectLoadingEnabled() {
      return this.hasSubselectLoadableCollections();
   }

   protected LockMode[] getLockModes(LockOptions lockOptions) {
      if (lockOptions == null) {
         return this.defaultLockModes;
      } else if (lockOptions.getAliasLockCount() != 0 || lockOptions.getLockMode() != null && !LockMode.NONE.equals(lockOptions.getLockMode())) {
         LockMode[] lockModesArray = new LockMode[this.entityAliases.length];

         for(int i = 0; i < this.entityAliases.length; ++i) {
            LockMode lockMode = lockOptions.getEffectiveLockMode(this.entityAliases[i]);
            if (lockMode == null) {
               lockMode = LockMode.NONE;
            }

            lockModesArray[i] = lockMode;
         }

         return lockModesArray;
      } else {
         return this.defaultLockModes;
      }
   }

   protected String applyLocks(String sql, QueryParameters parameters, Dialect dialect, List afterLoadActions) throws QueryException {
      LockOptions lockOptions = parameters.getLockOptions();
      if (lockOptions != null && (lockOptions.getLockMode() != LockMode.NONE || lockOptions.getAliasLockCount() != 0)) {
         if (this.shouldUseFollowOnLocking(parameters, dialect, afterLoadActions)) {
            return sql;
         } else {
            LockOptions locks = new LockOptions(lockOptions.getLockMode());
            Map keyColumnNames = dialect.forUpdateOfColumns() ? new HashMap() : null;
            locks.setScope(lockOptions.getScope());
            locks.setTimeOut(lockOptions.getTimeOut());

            for(Map.Entry entry : this.sqlAliasByEntityAlias.entrySet()) {
               String userAlias = (String)entry.getKey();
               String drivingSqlAlias = (String)entry.getValue();
               if (drivingSqlAlias == null) {
                  throw new IllegalArgumentException("could not locate alias to apply lock mode : " + userAlias);
               }

               QueryNode select = (QueryNode)this.queryTranslator.getSqlAST();
               Lockable drivingPersister = (Lockable)select.getFromClause().findFromElementByUserOrSqlAlias(userAlias, drivingSqlAlias).getQueryable();
               String sqlAlias = drivingPersister.getRootTableAlias(drivingSqlAlias);
               LockMode effectiveLockMode = lockOptions.getEffectiveLockMode(userAlias);
               locks.setAliasSpecificLockMode(sqlAlias, effectiveLockMode);
               if (keyColumnNames != null) {
                  keyColumnNames.put(sqlAlias, drivingPersister.getRootTableIdentifierColumnNames());
               }
            }

            return dialect.applyLocksToSql(sql, locks, keyColumnNames);
         }
      } else {
         return sql;
      }
   }

   protected void applyPostLoadLocks(Object[] row, LockMode[] lockModesArray, SessionImplementor session) {
   }

   protected boolean upgradeLocks() {
      return true;
   }

   private boolean hasSelectNew() {
      return this.aggregatedSelectExpression != null && this.aggregatedSelectExpression.getResultTransformer() != null;
   }

   protected String[] getResultRowAliases() {
      return this.queryReturnAliases;
   }

   protected ResultTransformer resolveResultTransformer(ResultTransformer resultTransformer) {
      ResultTransformer implicitResultTransformer = this.aggregatedSelectExpression == null ? null : this.aggregatedSelectExpression.getResultTransformer();
      return HolderInstantiator.resolveResultTransformer(implicitResultTransformer, resultTransformer);
   }

   protected boolean[] includeInResultRow() {
      boolean[] includeInResultTuple = this.includeInSelect;
      if (this.hasScalars) {
         includeInResultTuple = new boolean[this.queryReturnTypes.length];
         Arrays.fill(includeInResultTuple, true);
      }

      return includeInResultTuple;
   }

   protected Object getResultColumnOrRow(Object[] row, ResultTransformer transformer, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      Object[] resultRow = this.getResultRow(row, rs, session);
      boolean hasTransform = this.hasSelectNew() || transformer != null;
      return !hasTransform && resultRow.length == 1 ? resultRow[0] : resultRow;
   }

   protected Object[] getResultRow(Object[] row, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      Object[] resultRow;
      if (this.hasScalars) {
         String[][] scalarColumns = this.scalarColumnNames;
         int queryCols = this.queryReturnTypes.length;
         resultRow = new Object[queryCols];

         for(int i = 0; i < queryCols; ++i) {
            resultRow[i] = this.queryReturnTypes[i].nullSafeGet(rs, (String[])scalarColumns[i], session, (Object)null);
         }
      } else {
         resultRow = this.toResultRow(row);
      }

      return resultRow;
   }

   protected List getResultList(List results, ResultTransformer resultTransformer) throws QueryException {
      HolderInstantiator holderInstantiator = this.buildHolderInstantiator(resultTransformer);
      if (!holderInstantiator.isRequired()) {
         return results;
      } else {
         for(int i = 0; i < results.size(); ++i) {
            Object[] row = results.get(i);
            Object result = holderInstantiator.instantiate(row);
            results.set(i, result);
         }

         return !this.hasSelectNew() && resultTransformer != null ? resultTransformer.transformList(results) : results;
      }
   }

   private HolderInstantiator buildHolderInstantiator(ResultTransformer queryLocalResultTransformer) {
      ResultTransformer implicitResultTransformer = this.aggregatedSelectExpression == null ? null : this.aggregatedSelectExpression.getResultTransformer();
      return HolderInstantiator.getHolderInstantiator(implicitResultTransformer, queryLocalResultTransformer, this.queryReturnAliases);
   }

   public List list(SessionImplementor session, QueryParameters queryParameters) throws HibernateException {
      this.checkQuery(queryParameters);
      return this.list(session, queryParameters, this.queryTranslator.getQuerySpaces(), this.queryReturnTypes);
   }

   private void checkQuery(QueryParameters queryParameters) {
      if (this.hasSelectNew() && queryParameters.getResultTransformer() != null) {
         throw new QueryException("ResultTransformer is not allowed for 'select new' queries.");
      }
   }

   public Iterator iterate(QueryParameters queryParameters, EventSource session) throws HibernateException {
      this.checkQuery(queryParameters);
      boolean stats = session.getFactory().getStatistics().isStatisticsEnabled();
      long startTime = 0L;
      if (stats) {
         startTime = System.currentTimeMillis();
      }

      try {
         if (queryParameters.isCallable()) {
            throw new QueryException("iterate() not supported for callable statements");
         } else {
            ResultSet rs = this.executeQueryStatement(queryParameters, false, Collections.emptyList(), session);
            PreparedStatement st = (PreparedStatement)rs.getStatement();
            Iterator result = new IteratorImpl(rs, st, session, queryParameters.isReadOnly(session), this.queryReturnTypes, this.queryTranslator.getColumnNames(), this.buildHolderInstantiator(queryParameters.getResultTransformer()));
            if (stats) {
               session.getFactory().getStatisticsImplementor().queryExecuted(this.getQueryIdentifier(), 0, System.currentTimeMillis() - startTime);
            }

            return result;
         }
      } catch (SQLException sqle) {
         throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not execute query using iterate", this.getSQLString());
      }
   }

   public ScrollableResults scroll(QueryParameters queryParameters, SessionImplementor session) throws HibernateException {
      this.checkQuery(queryParameters);
      return this.scroll(queryParameters, this.queryReturnTypes, this.buildHolderInstantiator(queryParameters.getResultTransformer()), session);
   }

   private Object[] toResultRow(Object[] row) {
      if (this.selectLength == row.length) {
         return row;
      } else {
         Object[] result = new Object[this.selectLength];
         int j = 0;

         for(int i = 0; i < row.length; ++i) {
            if (this.includeInSelect[i]) {
               result[j++] = row[i];
            }
         }

         return result;
      }
   }

   public int[] getNamedParameterLocs(String name) throws QueryException {
      return this.queryTranslator.getParameterTranslations().getNamedParameterSqlLocations(name);
   }

   protected int bindParameterValues(PreparedStatement statement, QueryParameters queryParameters, int startIndex, SessionImplementor session) throws SQLException {
      int position = startIndex;

      for(ParameterSpecification spec : this.queryTranslator.getCollectedParameterSpecifications()) {
         position += spec.bind(statement, queryParameters, session, position);
      }

      return position - startIndex;
   }

   private int bindFilterParameterValues(PreparedStatement st, QueryParameters queryParameters, int position, SessionImplementor session) throws SQLException {
      int filteredParamCount = queryParameters.getFilteredPositionalParameterTypes() == null ? 0 : queryParameters.getFilteredPositionalParameterTypes().length;
      int nonfilteredParamCount = queryParameters.getPositionalParameterTypes() == null ? 0 : queryParameters.getPositionalParameterTypes().length;
      int filterParamCount = filteredParamCount - nonfilteredParamCount;

      for(int i = 0; i < filterParamCount; ++i) {
         Type type = queryParameters.getFilteredPositionalParameterTypes()[i];
         Object value = queryParameters.getFilteredPositionalParameterValues()[i];
         type.nullSafeSet(st, value, position, session);
         position += type.getColumnSpan(this.getFactory());
      }

      return position;
   }
}
