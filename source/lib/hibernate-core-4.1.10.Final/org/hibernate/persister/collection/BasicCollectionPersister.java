package org.hibernate.persister.collection;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Iterator;
import org.hibernate.HibernateException;
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
import org.hibernate.internal.StaticFilterAliasGenerator;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.jdbc.Expectation;
import org.hibernate.jdbc.Expectations;
import org.hibernate.loader.collection.BatchingCollectionInitializer;
import org.hibernate.loader.collection.CollectionInitializer;
import org.hibernate.loader.collection.SubselectCollectionLoader;
import org.hibernate.mapping.Collection;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.sql.Delete;
import org.hibernate.sql.Insert;
import org.hibernate.sql.SelectFragment;
import org.hibernate.sql.Update;
import org.hibernate.type.AssociationType;

public class BasicCollectionPersister extends AbstractCollectionPersister {
   private BasicBatchKey updateBatchKey;

   public boolean isCascadeDeleteEnabled() {
      return false;
   }

   public BasicCollectionPersister(Collection collection, CollectionRegionAccessStrategy cacheAccessStrategy, Configuration cfg, SessionFactoryImplementor factory) throws MappingException, CacheException {
      super(collection, cacheAccessStrategy, cfg, factory);
   }

   protected String generateDeleteString() {
      Delete delete = (new Delete()).setTableName(this.qualifiedTableName).addPrimaryKeyColumns(this.keyColumnNames);
      if (this.hasWhere) {
         delete.setWhere(this.sqlWhereString);
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         delete.setComment("delete collection " + this.getRole());
      }

      return delete.toStatementString();
   }

   protected String generateInsertRowString() {
      Insert insert = (new Insert(this.getDialect())).setTableName(this.qualifiedTableName).addColumns(this.keyColumnNames);
      if (this.hasIdentifier) {
         insert.addColumn(this.identifierColumnName);
      }

      if (this.hasIndex) {
         insert.addColumns(this.indexColumnNames, this.indexColumnIsSettable);
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         insert.setComment("insert collection row " + this.getRole());
      }

      insert.addColumns(this.elementColumnNames, this.elementColumnIsSettable, this.elementColumnWriters);
      return insert.toStatementString();
   }

   protected String generateUpdateRowString() {
      Update update = (new Update(this.getDialect())).setTableName(this.qualifiedTableName);
      update.addColumns(this.elementColumnNames, this.elementColumnIsSettable, this.elementColumnWriters);
      if (this.hasIdentifier) {
         update.addPrimaryKeyColumns(new String[]{this.identifierColumnName});
      } else if (this.hasIndex && !this.indexContainsFormula) {
         update.addPrimaryKeyColumns(ArrayHelper.join(this.keyColumnNames, this.indexColumnNames));
      } else {
         update.addPrimaryKeyColumns(this.keyColumnNames);
         update.addPrimaryKeyColumns(this.elementColumnNames, this.elementColumnIsInPrimaryKey, this.elementColumnWriters);
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         update.setComment("update collection row " + this.getRole());
      }

      return update.toStatementString();
   }

   protected String generateDeleteRowString() {
      Delete delete = (new Delete()).setTableName(this.qualifiedTableName);
      if (this.hasIdentifier) {
         delete.addPrimaryKeyColumns(new String[]{this.identifierColumnName});
      } else if (this.hasIndex && !this.indexContainsFormula) {
         delete.addPrimaryKeyColumns(ArrayHelper.join(this.keyColumnNames, this.indexColumnNames));
      } else {
         delete.addPrimaryKeyColumns(this.keyColumnNames);
         delete.addPrimaryKeyColumns(this.elementColumnNames, this.elementColumnIsInPrimaryKey, this.elementColumnWriters);
      }

      if (this.getFactory().getSettings().isCommentsEnabled()) {
         delete.setComment("delete collection row " + this.getRole());
      }

      return delete.toStatementString();
   }

   public boolean consumesEntityAlias() {
      return false;
   }

   public boolean consumesCollectionAlias() {
      return true;
   }

   public boolean isOneToMany() {
      return false;
   }

   public boolean isManyToMany() {
      return this.elementType.isEntityType();
   }

   protected int doUpdateRows(Serializable id, PersistentCollection collection, SessionImplementor session) throws HibernateException {
      if (ArrayHelper.isAllFalse(this.elementColumnIsSettable)) {
         return 0;
      } else {
         try {
            PreparedStatement st = null;
            Expectation expectation = Expectations.appropriateExpectation(this.getUpdateCheckStyle());
            boolean callable = this.isUpdateCallable();
            boolean useBatch = expectation.canBeBatched();
            Iterator entries = collection.entries(this);
            String sql = this.getSQLUpdateRowString();
            int i = 0;

            int count;
            for(count = 0; entries.hasNext(); ++i) {
               Object entry = entries.next();
               if (collection.needsUpdating(entry, i, this.elementType)) {
                  int offset = 1;
                  if (useBatch) {
                     if (this.updateBatchKey == null) {
                        this.updateBatchKey = new BasicBatchKey(this.getRole() + "#UPDATE", expectation);
                     }

                     st = session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.updateBatchKey).getBatchStatement(sql, callable);
                  } else {
                     st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareStatement(sql, callable);
                  }

                  try {
                     offset += expectation.prepare(st);
                     int loc = this.writeElement(st, collection.getElement(entry), offset, session);
                     if (this.hasIdentifier) {
                        this.writeIdentifier(st, collection.getIdentifier(entry, i), loc, session);
                     } else {
                        loc = this.writeKey(st, id, loc, session);
                        if (this.hasIndex && !this.indexContainsFormula) {
                           this.writeIndexToWhere(st, collection.getIndex(entry, i, this), loc, session);
                        } else {
                           this.writeElementToWhere(st, collection.getSnapshotElement(entry, i), loc, session);
                        }
                     }

                     if (useBatch) {
                        session.getTransactionCoordinator().getJdbcCoordinator().getBatch(this.updateBatchKey).addToBatch();
                     } else {
                        expectation.verifyOutcome(st.executeUpdate(), st, -1);
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

                  ++count;
               }
            }

            return count;
         } catch (SQLException sqle) {
            throw this.getSQLExceptionHelper().convert(sqle, "could not update collection rows: " + MessageHelper.collectionInfoString(this, collection, id, session), this.getSQLUpdateRowString());
         }
      }
   }

   public String selectFragment(Joinable rhs, String rhsAlias, String lhsAlias, String entitySuffix, String collectionSuffix, boolean includeCollectionColumns) {
      if (rhs != null && this.isManyToMany() && !rhs.isCollection()) {
         AssociationType elementType = (AssociationType)this.getElementType();
         if (rhs.equals(elementType.getAssociatedJoinable(this.getFactory()))) {
            return this.manyToManySelectFragment(rhs, rhsAlias, lhsAlias, collectionSuffix);
         }
      }

      return includeCollectionColumns ? this.selectFragment(lhsAlias, collectionSuffix) : "";
   }

   private String manyToManySelectFragment(Joinable rhs, String rhsAlias, String lhsAlias, String collectionSuffix) {
      SelectFragment frag = this.generateSelectFragment(lhsAlias, collectionSuffix);
      String[] elementColumnNames = rhs.getKeyColumnNames();
      frag.addColumns(rhsAlias, elementColumnNames, this.elementColumnAliases);
      this.appendIndexColumns(frag, lhsAlias);
      this.appendIdentifierColumns(frag, lhsAlias);
      return frag.toFragmentString().substring(2);
   }

   protected CollectionInitializer createCollectionInitializer(LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      return BatchingCollectionInitializer.createBatchingCollectionInitializer(this, this.batchSize, this.getFactory(), loadQueryInfluencers);
   }

   public String fromJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
      return "";
   }

   public String whereJoinFragment(String alias, boolean innerJoin, boolean includeSubclasses) {
      return "";
   }

   protected CollectionInitializer createSubselectInitializer(SubselectFetch subselect, SessionImplementor session) {
      return new SubselectCollectionLoader(this, subselect.toSubselectString(this.getCollectionType().getLHSPropertyName()), subselect.getResult(), subselect.getQueryParameters(), subselect.getNamedParameterLocMap(), session.getFactory(), session.getLoadQueryInfluencers());
   }

   public FilterAliasGenerator getFilterAliasGenerator(String rootAlias) {
      return new StaticFilterAliasGenerator(rootAlias);
   }
}
