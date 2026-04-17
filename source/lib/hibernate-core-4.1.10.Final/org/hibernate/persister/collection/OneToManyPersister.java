package org.hibernate.persister.collection;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import org.hibernate.MappingException;
import org.hibernate.cache.CacheException;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.jdbc.batch.internal.BasicBatchKey;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.internal.FilterAliasGenerator;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.jdbc.Expectation;
import org.hibernate.jdbc.Expectations;
import org.hibernate.loader.collection.BatchingCollectionInitializer;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.loader.collection.SubselectOneToManyLoader;
import org.hibernate.loader.entity.CollectionElementLoader;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.sql.Update;

public class OneToManyPersister extends AbstractCollectionPersister {
   private final boolean cascadeDeleteEnabled;
   private final boolean keyIsNullable;
   private final boolean keyIsUpdateable;
   private BasicBatchKey deleteRowBatchKey;
   private BasicBatchKey insertRowBatchKey;

   protected boolean isRowDeleteEnabled() {
      return this.keyIsUpdateable && this.keyIsNullable;
   }

   protected boolean isRowInsertEnabled() {
      return this.keyIsUpdateable;
   }

   public boolean isCascadeDeleteEnabled() {
      return this.cascadeDeleteEnabled;
   }

   public OneToManyPersister(Collection collection, CollectionRegionAccessStrategy cacheAccessStrategy, Configuration cfg, SessionFactoryImplementor factory) throws MappingException, CacheException {
      super(collection, cacheAccessStrategy, cfg, factory);
      this.cascadeDeleteEnabled = collection.getKey().isCascadeDeleteEnabled() && factory.getDialect().supportsCascadeDelete();
      this.keyIsNullable = collection.getKey().isNullable();
      this.keyIsUpdateable = collection.getKey().isUpdateable();
   }

   protected String generateDeleteString() {
      Update update = (new Update(this.getDialect())).setTableName(this.qualifiedTableName).addColumns(this.keyColumnNames, "null").addPrimaryKeyColumns(this.keyColumnNames);
      if (this.hasIndex && !this.indexContainsFormula) {
         update.addColumns(this.indexColumnNames, "null");
      }

      if (this.hasWhere) {
         update.setWhere(this.sqlWhereString);
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         update.setComment("delete one-to-many " + this.getRole());
      }

      return update.toStatementString();
   }

   protected String generateInsertRowString() {
      Update update = (new Update(this.getDialect())).setTableName(this.qualifiedTableName).addColumns(this.keyColumnNames);
      if (this.hasIndex && !this.indexContainsFormula) {
         update.addColumns(this.indexColumnNames);
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         update.setComment("create one-to-many row " + this.getRole());
      }

      return update.addPrimaryKeyColumns(this.elementColumnNames, this.elementColumnWriters).toStatementString();
   }

   protected String generateUpdateRowString() {
      return null;
   }

   protected String generateDeleteRowString() {
      Update update = (new Update(this.getDialect())).setTableName(this.qualifiedTableName).addColumns(this.keyColumnNames, "null");
      if (this.hasIndex && !this.indexContainsFormula) {
         update.addColumns(this.indexColumnNames, "null");
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         update.setComment("delete one-to-many row " + this.getRole());
      }

      String[] rowSelectColumnNames = ArrayHelper.join(this.keyColumnNames, this.elementColumnNames);
      return update.addPrimaryKeyColumns(rowSelectColumnNames).toStatementString();
   }

   public boolean consumesEntityAlias() {
      return true;
   }

   public boolean consumesCollectionAlias() {
      return true;
   }

   public boolean isOneToMany() {
      return true;
   }

   public boolean isManyToMany() {
      return false;
   }

   protected int doUpdateRows(Serializable id, PersistentCollection collection, SessionImplementor session) {
      try {
         int count = 0;
         if (this.isRowDeleteEnabled()) {
            Expectation deleteExpectation = Expectations.appropriateExpectation(this.getDeleteCheckStyle());
            boolean useBatch = deleteExpectation.canBeBatched();
            if (useBatch && this.deleteRowBatchKey == null) {
               this.deleteRowBatchKey = new BasicBatchKey(this.getRole() + "#DELETEROW", deleteExpectation);
            }

            String sql = this.getSQLDeleteRowString();
            PreparedStatement st = null;

            try {
               int i = 0;
               Iterator entries = collection.entries(this);

               for(int offset = 1; entries.hasNext(); ++i) {
                  Object entry = entries.next();
                  if (collection.needsUpdating(entry, i, this.elementType)) {
                     if (useBatch) {
                        st = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.deleteRowBatchKey).getBatchStatement(sql, this.isDeleteCallable());
                     } else {
                        st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, this.isDeleteCallable());
                     }

                     int loc = this.writeKey(st, id, offset, session);
                     this.writeElementToWhere(st, collection.getSnapshotElement(entry, i), loc, session);
                     if (useBatch) {
                        session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.deleteRowBatchKey).addToBatch();
                     } else {
                        deleteExpectation.verifyOutcome(st.executeUpdate(), st, -1);
                     }

                     ++count;
                  }
               }
            } catch (SQLException e) {
               if (useBatch) {
                  session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
               }

               throw e;
            } finally {
               if (!useBatch) {
                  st.close();
               }

            }
         }

         if (this.isRowInsertEnabled()) {
            Expectation insertExpectation = Expectations.appropriateExpectation(this.getInsertCheckStyle());
            boolean useBatch = insertExpectation.canBeBatched();
            boolean callable = this.isInsertCallable();
            if (useBatch && this.insertRowBatchKey == null) {
               this.insertRowBatchKey = new BasicBatchKey(this.getRole() + "#INSERTROW", insertExpectation);
            }

            String sql = this.getSQLInsertRowString();
            PreparedStatement st = null;

            try {
               int i = 0;

               for(Iterator entries = collection.entries(this); entries.hasNext(); ++i) {
                  Object entry = entries.next();
                  int offset = 1;
                  if (collection.needsUpdating(entry, i, this.elementType)) {
                     if (useBatch) {
                        st = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.insertRowBatchKey).getBatchStatement(sql, callable);
                     } else {
                        st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
                     }

                     offset += insertExpectation.prepare(st);
                     int loc = this.writeKey(st, id, offset, session);
                     if (this.hasIndex && !this.indexContainsFormula) {
                        loc = this.writeIndexToWhere(st, collection.getIndex(entry, i, this), loc, session);
                     }

                     this.writeElementToWhere(st, collection.getElement(entry), loc, session);
                     if (useBatch) {
                        session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.insertRowBatchKey).addToBatch();
                     } else {
                        insertExpectation.verifyOutcome(st.executeUpdate(), st, -1);
                     }

                     ++count;
                  }
               }
            } catch (SQLException sqle) {
               if (useBatch) {
                  session.getTransactionCoordinator().getJdbcCoordinator().abortBatch();
               }

               throw sqle;
            } finally {
               if (!useBatch) {
                  st.close();
               }

            }
         }

         return count;
      } catch (SQLException sqle) {
         throw this.getFactory().getSQLExceptionHelper().convert(sqle, "could not update collection rows: " + MessageHelper.collectionInfoString(this, collection, id, session), this.getSQLInsertRowString());
      }
   }

   public String selectFragment(Joinable rhs, String rhsAlias, String lhsAlias, String entitySuffix, String collectionSuffix, boolean includeCollectionColumns) {
      StringBuilder buf = new StringBuilder();
      if (includeCollectionColumns) {
         buf.append(this.selectFragment(lhsAlias, collectionSuffix)).append(", ");
      }

      OuterJoinLoadable ojl = (OuterJoinLoadable)this.getElementPersister();
      return buf.append(ojl.selectFragment(lhsAlias, entitySuffix)).toString();
   }

   protected CollectionInitializer createCollectionInitializer(LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      return BatchingCollectionInitializer.createBatchingOneToManyInitializer(this, this.batchSize, this.getFactory(), loadQueryInfluencers);
   }

   public String fromJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
      return ((Joinable)this.getElementPersister()).fromJoinFragment(alias, innerJoin, includeSubclasses);
   }

   public String whereJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
      return ((Joinable)this.getElementPersister()).whereJoinFragment(alias, innerJoin, includeSubclasses);
   }

   public String getTableName() {
      return ((Joinable)this.getElementPersister()).getTableName();
   }

   public String filterFragment(String alias) throws MappingException {
      String result = super.filterFragment(alias);
      if (this.getElementPersister() instanceof Joinable) {
         result = result + ((Joinable)this.getElementPersister()).oneToManyFilterFragment(alias);
      }

      return result;
   }

   protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
      return new SubselectOneToManyLoader(this, subselect.toSubselectString(this.getCollectionType().getLHSPropertyName()), subselect.getResult(), subselect.getQueryParameters(), subselect.getNamedParameterLocMap(), session.getFactory(), session.getLoadQueryInfluencers());
   }

   public Object getElementByIndex(Serializable key, Object index, SessionImplementor session, Object owner) {
      return (new CollectionElementLoader(this, this.getFactory(), session.getLoadQueryInfluencers())).loadElement(session, key, this.incrementIndexByBase(index));
   }

   public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {
      return this.getElementPersister().getFilterAliasGenerator(rootAlias);
   }
}
