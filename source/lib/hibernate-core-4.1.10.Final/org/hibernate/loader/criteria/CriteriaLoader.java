package org.hibernate.loader.criteria;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.loader.Loader;
import org.hibernate.loader.OuterJoinLoader;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.Lockable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

public class CriteriaLoader extends OuterJoinLoader {
   private final CriteriaQueryTranslator translator;
   private final Set querySpaces;
   private final Type[] resultTypes;
   private final String[] userAliases;
   private final boolean[] includeInResultRow;
   private final int resultRowLength;

   public CriteriaLoader(OuterJoinLoadable persister, SessionFactoryImplementor factory, CriteriaImpl criteria, String rootEntityName, LoadQueryInfluencers loadQueryInfluencers) throws HibernateException {
      super(factory, loadQueryInfluencers);
      this.translator = new CriteriaQueryTranslator(factory, criteria, rootEntityName, "this_");
      this.querySpaces = this.translator.getQuerySpaces();
      CriteriaJoinWalker walker = new CriteriaJoinWalker(persister, this.translator, factory, criteria, rootEntityName, loadQueryInfluencers);
      this.initFromWalker(walker);
      this.userAliases = walker.getUserAliases();
      this.resultTypes = walker.getResultTypes();
      this.includeInResultRow = walker.includeInResultRow();
      this.resultRowLength = ArrayHelper.countTrue(this.includeInResultRow);
      this.postInstantiate();
   }

   public ScrollableResults scroll(SessionImplementor session, ScrollMode scrollMode) throws HibernateException {
      QueryParameters qp = this.translator.getQueryParameters();
      qp.setScrollMode(scrollMode);
      return this.scroll(qp, this.resultTypes, (HolderInstantiator)null, session);
   }

   public List list(SessionImplementor session) throws HibernateException {
      return this.list(session, this.translator.getQueryParameters(), this.querySpaces, this.resultTypes);
   }

   protected String[] getResultRowAliases() {
      return this.userAliases;
   }

   protected ResultTransformer resolveResultTransformer(ResultTransformer resultTransformer) {
      return this.translator.getRootCriteria().getResultTransformer();
   }

   protected boolean areResultSetRowsTransformedImmediately() {
      return true;
   }

   protected boolean[] includeInResultRow() {
      return this.includeInResultRow;
   }

   protected Object getResultColumnOrRow(Object[] row, ResultTransformer transformer, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      return this.resolveResultTransformer(transformer).transformTuple(this.getResultRow(row, rs, session), this.getResultRowAliases());
   }

   protected Object[] getResultRow(Object[] row, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      Object[] result;
      if (this.translator.hasProjection()) {
         Type[] types = this.translator.getProjectedTypes();
         result = new Object[types.length];
         String[] columnAliases = this.translator.getProjectedColumnAliases();
         int i = 0;

         for(int pos = 0; i < result.length; ++i) {
            int numColumns = types[i].getColumnSpan(session.getFactory());
            if (numColumns > 1) {
               String[] typeColumnAliases = ArrayHelper.slice(columnAliases, pos, numColumns);
               result[i] = types[i].nullSafeGet(rs, (String[])typeColumnAliases, session, (Object)null);
            } else {
               result[i] = types[i].nullSafeGet(rs, (String)columnAliases[pos], session, (Object)null);
            }

            pos += numColumns;
         }
      } else {
         result = this.toResultRow(row);
      }

      return result;
   }

   private Object[] toResultRow(Object[] row) {
      if (this.resultRowLength == row.length) {
         return row;
      } else {
         Object[] result = new Object[this.resultRowLength];
         int j = 0;

         for(int i = 0; i < row.length; ++i) {
            if (this.includeInResultRow[i]) {
               result[j++] = row[i];
            }
         }

         return result;
      }
   }

   public Set getQuerySpaces() {
      return this.querySpaces;
   }

   protected String applyLocks(String sql, QueryParameters parameters, Dialect dialect, List afterLoadActions) throws QueryException {
      LockOptions lockOptions = parameters.getLockOptions();
      if (lockOptions != null && (lockOptions.getLockMode() != LockMode.NONE || lockOptions.getAliasLockCount() != 0)) {
         if (dialect.useFollowOnLocking()) {
            LOG.usingFollowOnLocking();
            LockMode lockMode = this.determineFollowOnLockMode(lockOptions);
            final LockOptions lockOptionsToUse = new LockOptions(lockMode);
            lockOptionsToUse.setTimeOut(lockOptions.getTimeOut());
            lockOptionsToUse.setScope(lockOptions.getScope());
            afterLoadActions.add(new Loader.AfterLoadAction() {
               public void afterLoad(SessionImplementor session, Object entity, Loadable persister) {
                  ((Session)session).buildLockRequest(lockOptionsToUse).lock(persister.getEntityName(), entity);
               }
            });
            parameters.setLockOptions(new LockOptions());
            return sql;
         } else {
            LockOptions locks = new LockOptions(lockOptions.getLockMode());
            locks.setScope(lockOptions.getScope());
            locks.setTimeOut(lockOptions.getTimeOut());
            Map keyColumnNames = dialect.forUpdateOfColumns() ? new HashMap() : null;
            String[] drivingSqlAliases = this.getAliases();

            for(int i = 0; i < drivingSqlAliases.length; ++i) {
               LockMode lockMode = lockOptions.getAliasSpecificLockMode(drivingSqlAliases[i]);
               if (lockMode != null) {
                  Lockable drivingPersister = (Lockable)this.getEntityPersisters()[i];
                  String rootSqlAlias = drivingPersister.getRootTableAlias(drivingSqlAliases[i]);
                  locks.setAliasSpecificLockMode(rootSqlAlias, lockMode);
                  if (keyColumnNames != null) {
                     keyColumnNames.put(rootSqlAlias, drivingPersister.getRootTableIdentifierColumnNames());
                  }
               }
            }

            return dialect.applyLocksToSql(sql, locks, keyColumnNames);
         }
      } else {
         return sql;
      }
   }

   protected LockMode determineFollowOnLockMode(LockOptions lockOptions) {
      LockMode lockModeToUse = lockOptions.findGreatestLockMode();
      if (lockOptions.getAliasLockCount() > 1) {
         LOG.aliasSpecificLockingWithFollowOnLocking(lockModeToUse);
      }

      return lockModeToUse;
   }

   protected LockMode[] getLockModes(LockOptions lockOptions) {
      String[] entityAliases = this.getAliases();
      if (entityAliases == null) {
         return null;
      } else {
         int size = entityAliases.length;
         LockMode[] lockModesArray = new LockMode[size];

         for(int i = 0; i < size; ++i) {
            LockMode lockMode = lockOptions.getAliasSpecificLockMode(entityAliases[i]);
            lockModesArray[i] = lockMode == null ? lockOptions.getLockMode() : lockMode;
         }

         return lockModesArray;
      }
   }

   protected boolean isSubselectLoadingEnabled() {
      return this.hasSubselectLoadableCollections();
   }

   protected List getResultList(List results, ResultTransformer resultTransformer) {
      return this.resolveResultTransformer(resultTransformer).transformList(results);
   }
}
