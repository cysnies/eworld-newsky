package org.hibernate.loader;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.QueryException;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.hibernate.WrongClassException;
import org.hibernate.cache.spi.FilterKey;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.QueryKey;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.pagination.LimitHandler;
import org.hibernate.dialect.pagination.LimitHelper;
import org.hibernate.dialect.pagination.NoopLimitHandler;
import org.hibernate.engine.internal.TwoPhaseLoad;
import org.hibernate.engine.jdbc.ColumnNameCache;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.RowSelection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SubselectFetch;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.PostLoadEvent;
import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.FetchingScrollableResultsImpl;
import org.hibernate.internal.ScrollableResultsImpl;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.UniqueKeyLoadable;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.transform.CacheableResultTransformer;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.AssociationType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.hibernate.type.VersionType;
import org.jboss.logging.Logger;

public abstract class Loader {
   protected static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, Loader.class.getName());
   private final SessionFactoryImplementor factory;
   private ColumnNameCache columnNameCache;

   public Loader(SessionFactoryImplementor factory) {
      this.factory = factory;
   }

   protected abstract String getSQLString();

   protected abstract Loadable[] getEntityPersisters();

   protected boolean[] getEntityEagerPropertyFetches() {
      return null;
   }

   protected int[] getOwners() {
      return null;
   }

   protected EntityType[] getOwnerAssociationTypes() {
      return null;
   }

   protected CollectionPersister[] getCollectionPersisters() {
      return null;
   }

   protected int[] getCollectionOwners() {
      return null;
   }

   protected int[][] getCompositeKeyManyToOneTargetIndices() {
      return (int[][])null;
   }

   protected abstract LockMode[] getLockModes(LockOptions var1);

   protected String applyLocks(String sql, QueryParameters parameters, Dialect dialect, List afterLoadActions) throws HibernateException {
      return sql;
   }

   protected boolean upgradeLocks() {
      return false;
   }

   protected boolean isSingleRowLoader() {
      return false;
   }

   protected String[] getAliases() {
      return null;
   }

   protected String preprocessSQL(String sql, QueryParameters parameters, Dialect dialect, List afterLoadActions) throws HibernateException {
      sql = this.applyLocks(sql, parameters, dialect, afterLoadActions);
      return this.getFactory().getSettings().isCommentsEnabled() ? this.prependComment(sql, parameters) : sql;
   }

   protected boolean shouldUseFollowOnLocking(QueryParameters parameters, Dialect dialect, List afterLoadActions) {
      if (dialect.useFollowOnLocking()) {
         LOG.usingFollowOnLocking();
         LockMode lockMode = this.determineFollowOnLockMode(parameters.getLockOptions());
         final LockOptions lockOptions = new LockOptions(lockMode);
         lockOptions.setTimeOut(parameters.getLockOptions().getTimeOut());
         lockOptions.setScope(parameters.getLockOptions().getScope());
         afterLoadActions.add(new AfterLoadAction() {
            public void afterLoad(SessionImplementor session, Object entity, Loadable persister) {
               ((Session)session).buildLockRequest(lockOptions).lock(persister.getEntityName(), entity);
            }
         });
         parameters.setLockOptions(new LockOptions());
         return true;
      } else {
         return false;
      }
   }

   protected LockMode determineFollowOnLockMode(LockOptions lockOptions) {
      LockMode lockModeToUse = lockOptions.findGreatestLockMode();
      if (lockOptions.hasAliasSpecificLockModes()) {
         LOG.aliasSpecificLockingWithFollowOnLocking(lockModeToUse);
      }

      return lockModeToUse;
   }

   private String prependComment(String sql, QueryParameters parameters) {
      String comment = parameters.getComment();
      return comment == null ? sql : (new StringBuilder(comment.length() + sql.length() + 5)).append("/* ").append(comment).append(" */ ").append(sql).toString();
   }

   private List doQueryAndInitializeNonLazyCollections(SessionImplementor session, QueryParameters queryParameters, boolean returnProxies) throws HibernateException, SQLException {
      return this.doQueryAndInitializeNonLazyCollections(session, queryParameters, returnProxies, (ResultTransformer)null);
   }

   private List doQueryAndInitializeNonLazyCollections(SessionImplementor session, QueryParameters queryParameters, boolean returnProxies, ResultTransformer forcedResultTransformer) throws HibernateException, SQLException {
      PersistenceContext persistenceContext = session.getPersistenceContext();
      boolean defaultReadOnlyOrig = persistenceContext.isDefaultReadOnly();
      if (queryParameters.isReadOnlyInitialized()) {
         persistenceContext.setDefaultReadOnly(queryParameters.isReadOnly());
      } else {
         queryParameters.setReadOnly(persistenceContext.isDefaultReadOnly());
      }

      persistenceContext.beforeLoad();

      List result;
      try {
         try {
            result = this.doQuery(session, queryParameters, returnProxies, forcedResultTransformer);
         } finally {
            persistenceContext.afterLoad();
         }

         persistenceContext.initializeNonLazyCollections();
      } finally {
         persistenceContext.setDefaultReadOnly(defaultReadOnlyOrig);
      }

      return result;
   }

   public Object loadSingleRow(ResultSet resultSet, SessionImplementor session, QueryParameters queryParameters, boolean returnProxies) throws HibernateException {
      int entitySpan = this.getEntityPersisters().length;
      List hydratedObjects = entitySpan == 0 ? null : new ArrayList(entitySpan);

      Object result;
      try {
         result = this.getRowFromResultSet(resultSet, session, queryParameters, this.getLockModes(queryParameters.getLockOptions()), (EntityKey)null, hydratedObjects, new EntityKey[entitySpan], returnProxies);
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not read next row of results", this.getSQLString());
      }

      this.initializeEntitiesAndCollections(hydratedObjects, resultSet, session, queryParameters.isReadOnly(session));
      session.getPersistenceContext().initializeNonLazyCollections();
      return result;
   }

   private Object sequentialLoad(ResultSet resultSet, SessionImplementor session, QueryParameters queryParameters, boolean returnProxies, EntityKey keyToRead) throws HibernateException {
      int entitySpan = this.getEntityPersisters().length;
      List hydratedObjects = entitySpan == 0 ? null : new ArrayList(entitySpan);
      Object result = null;
      EntityKey[] loadedKeys = new EntityKey[entitySpan];

      try {
         do {
            Object loaded = this.getRowFromResultSet(resultSet, session, queryParameters, this.getLockModes(queryParameters.getLockOptions()), (EntityKey)null, hydratedObjects, loadedKeys, returnProxies);
            if (!keyToRead.equals(loadedKeys[0])) {
               throw new AssertionFailure(String.format("Unexpected key read for row; expected [%s]; actual [%s]", keyToRead, loadedKeys[0]));
            }

            if (result == null) {
               result = loaded;
            }
         } while(resultSet.next() && this.isCurrentRowForSameEntity(keyToRead, 0, resultSet, session));
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not doAfterTransactionCompletion sequential read of results (forward)", this.getSQLString());
      }

      this.initializeEntitiesAndCollections(hydratedObjects, resultSet, session, queryParameters.isReadOnly(session));
      session.getPersistenceContext().initializeNonLazyCollections();
      return result;
   }

   private boolean isCurrentRowForSameEntity(EntityKey keyToRead, int persisterIndex, ResultSet resultSet, SessionImplementor session) throws SQLException {
      EntityKey currentRowKey = this.getKeyFromResultSet(persisterIndex, this.getEntityPersisters()[persisterIndex], (Serializable)null, resultSet, session);
      return keyToRead.equals(currentRowKey);
   }

   public Object loadSequentialRowsForward(ResultSet resultSet, SessionImplementor session, QueryParameters queryParameters, boolean returnProxies) throws HibernateException {
      try {
         if (resultSet.isAfterLast()) {
            return null;
         } else {
            if (resultSet.isBeforeFirst()) {
               resultSet.next();
            }

            EntityKey currentKey = this.getKeyFromResultSet(0, this.getEntityPersisters()[0], (Serializable)null, resultSet, session);
            return this.sequentialLoad(resultSet, session, queryParameters, returnProxies, currentKey);
         }
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not doAfterTransactionCompletion sequential read of results (forward)", this.getSQLString());
      }
   }

   public Object loadSequentialRowsReverse(ResultSet resultSet, SessionImplementor session, QueryParameters queryParameters, boolean returnProxies, boolean isLogicallyAfterLast) throws HibernateException {
      try {
         if (resultSet.isFirst()) {
            return null;
         } else {
            EntityKey keyToRead = null;
            if (resultSet.isAfterLast() && isLogicallyAfterLast) {
               resultSet.last();
               keyToRead = this.getKeyFromResultSet(0, this.getEntityPersisters()[0], (Serializable)null, resultSet, session);
            } else {
               resultSet.previous();
               boolean firstPass = true;
               EntityKey lastKey = this.getKeyFromResultSet(0, this.getEntityPersisters()[0], (Serializable)null, resultSet, session);

               while(resultSet.previous()) {
                  EntityKey checkKey = this.getKeyFromResultSet(0, this.getEntityPersisters()[0], (Serializable)null, resultSet, session);
                  if (firstPass) {
                     firstPass = false;
                     keyToRead = checkKey;
                  }

                  if (!lastKey.equals(checkKey)) {
                     break;
                  }
               }
            }

            while(resultSet.previous()) {
               EntityKey checkKey = this.getKeyFromResultSet(0, this.getEntityPersisters()[0], (Serializable)null, resultSet, session);
               if (!keyToRead.equals(checkKey)) {
                  break;
               }
            }

            resultSet.next();
            return this.sequentialLoad(resultSet, session, queryParameters, returnProxies, keyToRead);
         }
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not doAfterTransactionCompletion sequential read of results (forward)", this.getSQLString());
      }
   }

   private static EntityKey getOptionalObjectKey(QueryParameters queryParameters, SessionImplementor session) {
      Object optionalObject = queryParameters.getOptionalObject();
      Serializable optionalId = queryParameters.getOptionalId();
      String optionalEntityName = queryParameters.getOptionalEntityName();
      return optionalObject != null && optionalEntityName != null ? session.generateEntityKey(optionalId, session.getEntityPersister(optionalEntityName, optionalObject)) : null;
   }

   private Object getRowFromResultSet(ResultSet resultSet, SessionImplementor session, QueryParameters queryParameters, LockMode[] lockModesArray, EntityKey optionalObjectKey, List hydratedObjects, EntityKey[] keys, boolean returnProxies) throws SQLException, HibernateException {
      return this.getRowFromResultSet(resultSet, session, queryParameters, lockModesArray, optionalObjectKey, hydratedObjects, keys, returnProxies, (ResultTransformer)null);
   }

   private Object getRowFromResultSet(ResultSet resultSet, SessionImplementor session, QueryParameters queryParameters, LockMode[] lockModesArray, EntityKey optionalObjectKey, List hydratedObjects, EntityKey[] keys, boolean returnProxies, ResultTransformer forcedResultTransformer) throws SQLException, HibernateException {
      Loadable[] persisters = this.getEntityPersisters();
      int entitySpan = persisters.length;
      this.extractKeysFromResultSet(persisters, queryParameters, resultSet, session, keys, lockModesArray, hydratedObjects);
      this.registerNonExists(keys, persisters, session);
      Object[] row = this.getRow(resultSet, persisters, keys, queryParameters.getOptionalObject(), optionalObjectKey, lockModesArray, hydratedObjects, session);
      this.readCollectionElements(row, resultSet, session);
      if (returnProxies) {
         for(int i = 0; i < entitySpan; ++i) {
            Object entity = row[i];
            Object proxy = session.getPersistenceContext().proxyFor(persisters[i], keys[i], entity);
            if (entity != proxy) {
               ((HibernateProxy)proxy).getHibernateLazyInitializer().setImplementation(entity);
               row[i] = proxy;
            }
         }
      }

      this.applyPostLoadLocks(row, lockModesArray, session);
      return forcedResultTransformer == null ? this.getResultColumnOrRow(row, queryParameters.getResultTransformer(), resultSet, session) : forcedResultTransformer.transformTuple(this.getResultRow(row, resultSet, session), this.getResultRowAliases());
   }

   protected void extractKeysFromResultSet(Loadable[] persisters, QueryParameters queryParameters, ResultSet resultSet, SessionImplementor session, EntityKey[] keys, LockMode[] lockModes, List hydratedObjects) throws SQLException {
      int entitySpan = persisters.length;
      Serializable optionalId = queryParameters.getOptionalId();
      int numberOfPersistersToProcess;
      if (this.isSingleRowLoader() && optionalId != null) {
         keys[entitySpan - 1] = session.generateEntityKey(optionalId, persisters[entitySpan - 1]);
         numberOfPersistersToProcess = entitySpan - 1;
      } else {
         numberOfPersistersToProcess = entitySpan;
      }

      Object[] hydratedKeyState = new Object[numberOfPersistersToProcess];

      for(int i = 0; i < numberOfPersistersToProcess; ++i) {
         Type idType = persisters[i].getIdentifierType();
         hydratedKeyState[i] = idType.hydrate(resultSet, this.getEntityAliases()[i].getSuffixedKeyAliases(), session, (Object)null);
      }

      for(int i = 0; i < numberOfPersistersToProcess; ++i) {
         Type idType = persisters[i].getIdentifierType();
         if (idType.isComponentType() && this.getCompositeKeyManyToOneTargetIndices() != null) {
            int[] keyManyToOneTargetIndices = this.getCompositeKeyManyToOneTargetIndices()[i];
            if (keyManyToOneTargetIndices != null) {
               for(int targetIndex : keyManyToOneTargetIndices) {
                  if (targetIndex < numberOfPersistersToProcess) {
                     Type targetIdType = persisters[targetIndex].getIdentifierType();
                     Serializable targetId = (Serializable)targetIdType.resolve(hydratedKeyState[targetIndex], session, (Object)null);
                     keys[targetIndex] = session.generateEntityKey(targetId, persisters[targetIndex]);
                  }

                  Object object = session.getEntityUsingInterceptor(keys[targetIndex]);
                  if (object != null) {
                     this.instanceAlreadyLoaded(resultSet, targetIndex, persisters[targetIndex], keys[targetIndex], object, lockModes[targetIndex], session);
                  } else {
                     this.instanceNotYetLoaded(resultSet, targetIndex, persisters[targetIndex], this.getEntityAliases()[targetIndex].getRowIdAlias(), keys[targetIndex], lockModes[targetIndex], getOptionalObjectKey(queryParameters, session), queryParameters.getOptionalObject(), hydratedObjects, session);
                  }
               }
            }
         }

         Serializable resolvedId = (Serializable)idType.resolve(hydratedKeyState[i], session, (Object)null);
         keys[i] = resolvedId == null ? null : session.generateEntityKey(resolvedId, persisters[i]);
      }

   }

   protected void applyPostLoadLocks(Object[] row, LockMode[] lockModesArray, SessionImplementor session) {
   }

   private void readCollectionElements(Object[] row, ResultSet resultSet, SessionImplementor session) throws SQLException, HibernateException {
      CollectionPersister[] collectionPersisters = this.getCollectionPersisters();
      if (collectionPersisters != null) {
         CollectionAliases[] descriptors = this.getCollectionAliases();
         int[] collectionOwners = this.getCollectionOwners();

         for(int i = 0; i < collectionPersisters.length; ++i) {
            boolean hasCollectionOwners = collectionOwners != null && collectionOwners[i] > -1;
            Object owner = hasCollectionOwners ? row[collectionOwners[i]] : null;
            CollectionPersister collectionPersister = collectionPersisters[i];
            Serializable key;
            if (owner == null) {
               key = null;
            } else {
               key = collectionPersister.getCollectionType().getKeyOfOwner(owner, session);
            }

            this.readCollectionElement(owner, key, collectionPersister, descriptors[i], resultSet, session);
         }
      }

   }

   private List doQuery(SessionImplementor session, QueryParameters queryParameters, boolean returnProxies, ResultTransformer forcedResultTransformer) throws SQLException, HibernateException {
      RowSelection selection = queryParameters.getRowSelection();
      int maxRows = LimitHelper.hasMaxRows(selection) ? selection.getMaxRows() : Integer.MAX_VALUE;
      List<AfterLoadAction> afterLoadActions = new ArrayList();
      ResultSet rs = this.executeQueryStatement(queryParameters, false, afterLoadActions, session);
      Statement st = rs.getStatement();
      int entitySpan = this.getEntityPersisters().length;

      List var11;
      try {
         var11 = this.processResultSet(rs, queryParameters, session, returnProxies, forcedResultTransformer, maxRows, afterLoadActions);
      } finally {
         st.close();
      }

      return var11;
   }

   protected List processResultSet(ResultSet rs, QueryParameters queryParameters, SessionImplementor session, boolean returnProxies, ResultTransformer forcedResultTransformer, int maxRows, List afterLoadActions) throws SQLException {
      int entitySpan = this.getEntityPersisters().length;
      EntityKey optionalObjectKey = getOptionalObjectKey(queryParameters, session);
      LockMode[] lockModesArray = this.getLockModes(queryParameters.getLockOptions());
      boolean createSubselects = this.isSubselectLoadingEnabled();
      List subselectResultKeys = createSubselects ? new ArrayList() : null;
      ArrayList hydratedObjects = entitySpan == 0 ? null : new ArrayList(entitySpan * 10);
      List results = new ArrayList();
      this.handleEmptyCollections(queryParameters.getCollectionKeys(), rs, session);
      EntityKey[] keys = new EntityKey[entitySpan];
      LOG.trace("Processing result set");

      int count;
      for(count = 0; count < maxRows && rs.next(); ++count) {
         LOG.debugf("Result set row: %s", count);
         Object result = this.getRowFromResultSet(rs, session, queryParameters, lockModesArray, optionalObjectKey, hydratedObjects, keys, returnProxies, forcedResultTransformer);
         results.add(result);
         if (createSubselects) {
            subselectResultKeys.add(keys);
            keys = new EntityKey[entitySpan];
         }
      }

      LOG.tracev("Done processing result set ({0} rows)", count);
      this.initializeEntitiesAndCollections(hydratedObjects, rs, session, queryParameters.isReadOnly(session), afterLoadActions);
      if (createSubselects) {
         this.createSubselects(subselectResultKeys, queryParameters, session);
      }

      return results;
   }

   protected boolean isSubselectLoadingEnabled() {
      return false;
   }

   protected boolean hasSubselectLoadableCollections() {
      Loadable[] loadables = this.getEntityPersisters();

      for(int i = 0; i < loadables.length; ++i) {
         if (loadables[i].hasSubselectLoadableCollections()) {
            return true;
         }
      }

      return false;
   }

   private static Set[] transpose(List keys) {
      Set[] result = new Set[((EntityKey[])((EntityKey[])keys.get(0))).length];

      for(int j = 0; j < result.length; ++j) {
         result[j] = new HashSet(keys.size());

         for(int i = 0; i < keys.size(); ++i) {
            result[j].add(((EntityKey[])((EntityKey[])keys.get(i)))[j]);
         }
      }

      return result;
   }

   private void createSubselects(List keys, QueryParameters queryParameters, SessionImplementor session) {
      if (keys.size() > 1) {
         Set[] keySets = transpose(keys);
         Map namedParameterLocMap = this.buildNamedParameterLocMap(queryParameters);
         Loadable[] loadables = this.getEntityPersisters();
         String[] aliases = this.getAliases();

         for(EntityKey[] rowKeys : keys) {
            for(int i = 0; i < rowKeys.length; ++i) {
               if (rowKeys[i] != null && loadables[i].hasSubselectLoadableCollections()) {
                  SubselectFetch subselectFetch = new SubselectFetch(aliases[i], loadables[i], queryParameters, keySets[i], namedParameterLocMap);
                  session.getPersistenceContext().getBatchFetchQueue().addSubselect(rowKeys[i], subselectFetch);
               }
            }
         }
      }

   }

   private Map buildNamedParameterLocMap(QueryParameters queryParameters) {
      if (queryParameters.getNamedParameters() == null) {
         return null;
      } else {
         Map namedParameterLocMap = new HashMap();

         for(String name : queryParameters.getNamedParameters().keySet()) {
            namedParameterLocMap.put(name, this.getNamedParameterLocs(name));
         }

         return namedParameterLocMap;
      }
   }

   private void initializeEntitiesAndCollections(List hydratedObjects, Object resultSetId, SessionImplementor session, boolean readOnly) throws HibernateException {
      this.initializeEntitiesAndCollections(hydratedObjects, resultSetId, session, readOnly, Collections.emptyList());
   }

   private void initializeEntitiesAndCollections(List hydratedObjects, Object resultSetId, SessionImplementor session, boolean readOnly, List afterLoadActions) throws HibernateException {
      CollectionPersister[] collectionPersisters = this.getCollectionPersisters();
      if (collectionPersisters != null) {
         for(int i = 0; i < collectionPersisters.length; ++i) {
            if (collectionPersisters[i].isArray()) {
               this.endCollectionLoad(resultSetId, session, collectionPersisters[i]);
            }
         }
      }

      PostLoadEvent post;
      PreLoadEvent pre;
      if (session.isEventSource()) {
         pre = new PreLoadEvent((EventSource)session);
         post = new PostLoadEvent((EventSource)session);
      } else {
         pre = null;
         post = null;
      }

      if (hydratedObjects != null) {
         int hydratedObjectsSize = hydratedObjects.size();
         LOG.tracev("Total objects hydrated: {0}", hydratedObjectsSize);

         for(int i = 0; i < hydratedObjectsSize; ++i) {
            TwoPhaseLoad.initializeEntity(hydratedObjects.get(i), readOnly, session, pre, post);
         }
      }

      if (collectionPersisters != null) {
         for(int i = 0; i < collectionPersisters.length; ++i) {
            if (!collectionPersisters[i].isArray()) {
               this.endCollectionLoad(resultSetId, session, collectionPersisters[i]);
            }
         }
      }

      if (hydratedObjects != null) {
         for(Object hydratedObject : hydratedObjects) {
            TwoPhaseLoad.postLoad(hydratedObject, session, post);
            if (afterLoadActions != null) {
               for(AfterLoadAction afterLoadAction : afterLoadActions) {
                  EntityEntry entityEntry = session.getPersistenceContext().getEntry(hydratedObject);
                  if (entityEntry == null) {
                     throw new HibernateException("Could not locate EntityEntry immediately after two-phase load");
                  }

                  afterLoadAction.afterLoad(session, hydratedObject, (Loadable)entityEntry.getPersister());
               }
            }
         }
      }

   }

   private void endCollectionLoad(Object resultSetId, SessionImplementor session, CollectionPersister collectionPersister) {
      session.getPersistenceContext().getLoadContexts().getCollectionLoadContext((ResultSet)resultSetId).endLoadingCollections(collectionPersister);
   }

   protected ResultTransformer resolveResultTransformer(ResultTransformer resultTransformer) {
      return resultTransformer;
   }

   protected List getResultList(List results, ResultTransformer resultTransformer) throws QueryException {
      return results;
   }

   protected boolean areResultSetRowsTransformedImmediately() {
      return false;
   }

   protected String[] getResultRowAliases() {
      return null;
   }

   protected Object getResultColumnOrRow(Object[] row, ResultTransformer transformer, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      return row;
   }

   protected boolean[] includeInResultRow() {
      return null;
   }

   protected Object[] getResultRow(Object[] row, ResultSet rs, SessionImplementor session) throws SQLException, HibernateException {
      return row;
   }

   private void registerNonExists(EntityKey[] keys, Loadable[] persisters, SessionImplementor session) {
      int[] owners = this.getOwners();
      if (owners != null) {
         EntityType[] ownerAssociationTypes = this.getOwnerAssociationTypes();

         for(int i = 0; i < keys.length; ++i) {
            int owner = owners[i];
            if (owner > -1) {
               EntityKey ownerKey = keys[owner];
               if (keys[i] == null && ownerKey != null) {
                  PersistenceContext persistenceContext = session.getPersistenceContext();
                  boolean isOneToOneAssociation = ownerAssociationTypes != null && ownerAssociationTypes[i] != null && ownerAssociationTypes[i].isOneToOne();
                  if (isOneToOneAssociation) {
                     persistenceContext.addNullProperty(ownerKey, ownerAssociationTypes[i].getPropertyName());
                  }
               }
            }
         }
      }

   }

   private void readCollectionElement(Object optionalOwner, Serializable optionalKey, CollectionPersister persister, CollectionAliases descriptor, ResultSet rs, SessionImplementor session) throws HibernateException, SQLException {
      PersistenceContext persistenceContext = session.getPersistenceContext();
      Serializable collectionRowKey = (Serializable)persister.readKey(rs, descriptor.getSuffixedKeyAliases(), session);
      if (collectionRowKey != null) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Found row of collection: %s", MessageHelper.collectionInfoString(persister, collectionRowKey, this.getFactory()));
         }

         Object owner = optionalOwner;
         if (optionalOwner == null) {
            owner = persistenceContext.getCollectionOwner(collectionRowKey, persister);
            if (owner == null) {
            }
         }

         PersistentCollection rowCollection = persistenceContext.getLoadContexts().getCollectionLoadContext(rs).getLoadingCollection(persister, collectionRowKey);
         if (rowCollection != null) {
            rowCollection.readFrom(rs, persister, descriptor, owner);
         }
      } else if (optionalKey != null) {
         if (LOG.isDebugEnabled()) {
            LOG.debugf("Result set contains (possibly empty) collection: %s", MessageHelper.collectionInfoString(persister, optionalKey, this.getFactory()));
         }

         persistenceContext.getLoadContexts().getCollectionLoadContext(rs).getLoadingCollection(persister, optionalKey);
      }

   }

   private void handleEmptyCollections(Serializable[] keys, Object resultSetId, SessionImplementor session) {
      if (keys != null) {
         CollectionPersister[] collectionPersisters = this.getCollectionPersisters();

         for(int j = 0; j < collectionPersisters.length; ++j) {
            for(int i = 0; i < keys.length; ++i) {
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("Result set contains (possibly empty) collection: %s", MessageHelper.collectionInfoString(collectionPersisters[j], keys[i], this.getFactory()));
               }

               session.getPersistenceContext().getLoadContexts().getCollectionLoadContext((ResultSet)resultSetId).getLoadingCollection(collectionPersisters[j], keys[i]);
            }
         }
      }

   }

   private EntityKey getKeyFromResultSet(int i, Loadable persister, Serializable id, ResultSet rs, SessionImplementor session) throws HibernateException, SQLException {
      Serializable resultId;
      if (this.isSingleRowLoader() && id != null) {
         resultId = id;
      } else {
         Type idType = persister.getIdentifierType();
         resultId = (Serializable)idType.nullSafeGet(rs, (String[])this.getEntityAliases()[i].getSuffixedKeyAliases(), session, (Object)null);
         boolean idIsResultId = id != null && resultId != null && idType.isEqual(id, resultId, this.factory);
         if (idIsResultId) {
            resultId = id;
         }
      }

      return resultId == null ? null : session.generateEntityKey(resultId, persister);
   }

   private void checkVersion(int i, Loadable persister, Serializable id, Object entity, ResultSet rs, SessionImplementor session) throws HibernateException, SQLException {
      Object version = session.getPersistenceContext().getEntry(entity).getVersion();
      if (version != null) {
         VersionType versionType = persister.getVersionType();
         Object currentVersion = versionType.nullSafeGet(rs, this.getEntityAliases()[i].getSuffixedVersionAliases(), session, (Object)null);
         if (!versionType.isEqual(version, currentVersion)) {
            if (session.getFactory().getStatistics().isStatisticsEnabled()) {
               session.getFactory().getStatisticsImplementor().optimisticFailure(persister.getEntityName());
            }

            throw new StaleObjectStateException(persister.getEntityName(), id);
         }
      }

   }

   private Object[] getRow(ResultSet rs, Loadable[] persisters, EntityKey[] keys, Object optionalObject, EntityKey optionalObjectKey, LockMode[] lockModes, List hydratedObjects, SessionImplementor session) throws HibernateException, SQLException {
      int cols = persisters.length;
      EntityAliases[] descriptors = this.getEntityAliases();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Result row: %s", StringHelper.toString(keys));
      }

      Object[] rowResults = new Object[cols];

      for(int i = 0; i < cols; ++i) {
         Object object = null;
         EntityKey key = keys[i];
         if (keys[i] != null) {
            object = session.getEntityUsingInterceptor(key);
            if (object != null) {
               this.instanceAlreadyLoaded(rs, i, persisters[i], key, object, lockModes[i], session);
            } else {
               object = this.instanceNotYetLoaded(rs, i, persisters[i], descriptors[i].getRowIdAlias(), key, lockModes[i], optionalObjectKey, optionalObject, hydratedObjects, session);
            }
         }

         rowResults[i] = object;
      }

      return rowResults;
   }

   private void instanceAlreadyLoaded(ResultSet rs, int i, Loadable persister, EntityKey key, Object object, LockMode lockMode, SessionImplementor session) throws HibernateException, SQLException {
      if (!persister.isInstance(object)) {
         throw new WrongClassException("loaded object was of wrong class " + object.getClass(), key.getIdentifier(), persister.getEntityName());
      } else {
         if (LockMode.NONE != lockMode && this.upgradeLocks()) {
            boolean isVersionCheckNeeded = persister.isVersioned() && session.getPersistenceContext().getEntry(object).getLockMode().lessThan(lockMode);
            if (isVersionCheckNeeded) {
               this.checkVersion(i, persister, key.getIdentifier(), object, rs, session);
               session.getPersistenceContext().getEntry(object).setLockMode(lockMode);
            }
         }

      }
   }

   private Object instanceNotYetLoaded(ResultSet rs, int i, Loadable persister, String rowIdAlias, EntityKey key, LockMode lockMode, EntityKey optionalObjectKey, Object optionalObject, List hydratedObjects, SessionImplementor session) throws HibernateException, SQLException {
      String instanceClass = this.getInstanceClass(rs, i, persister, key.getIdentifier(), session);
      Object object;
      if (optionalObjectKey != null && key.equals(optionalObjectKey)) {
         object = optionalObject;
      } else {
         object = session.instantiate(instanceClass, key.getIdentifier());
      }

      LockMode acquiredLockMode = lockMode == LockMode.NONE ? LockMode.READ : lockMode;
      this.loadFromResultSet(rs, i, object, instanceClass, key, rowIdAlias, acquiredLockMode, persister, session);
      hydratedObjects.add(object);
      return object;
   }

   private boolean isEagerPropertyFetchEnabled(int i) {
      boolean[] array = this.getEntityEagerPropertyFetches();
      return array != null && array[i];
   }

   private void loadFromResultSet(ResultSet rs, int i, Object object, String instanceEntityName, EntityKey key, String rowIdAlias, LockMode lockMode, Loadable rootPersister, SessionImplementor session) throws SQLException, HibernateException {
      Serializable id = key.getIdentifier();
      Loadable persister = (Loadable)this.getFactory().getEntityPersister(instanceEntityName);
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Initializing object from ResultSet: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)this.getFactory()));
      }

      boolean eagerPropertyFetch = this.isEagerPropertyFetchEnabled(i);
      TwoPhaseLoad.addUninitializedEntity(key, object, persister, lockMode, !eagerPropertyFetch, session);
      String[][] cols = persister == rootPersister ? this.getEntityAliases()[i].getSuffixedPropertyAliases() : this.getEntityAliases()[i].getSuffixedPropertyAliases(persister);
      Object[] values = persister.hydrate(rs, id, object, rootPersister, cols, eagerPropertyFetch, session);
      Object rowId = persister.hasRowId() ? rs.getObject(rowIdAlias) : null;
      AssociationType[] ownerAssociationTypes = this.getOwnerAssociationTypes();
      if (ownerAssociationTypes != null && ownerAssociationTypes[i] != null) {
         String ukName = ownerAssociationTypes[i].getRHSUniqueKeyPropertyName();
         if (ukName != null) {
            int index = ((UniqueKeyLoadable)persister).getPropertyIndex(ukName);
            Type type = persister.getPropertyTypes()[index];
            EntityUniqueKey euk = new EntityUniqueKey(rootPersister.getEntityName(), ukName, type.semiResolve(values[index], session, object), type, persister.getEntityMode(), session.getFactory());
            session.getPersistenceContext().addEntity(euk, object);
         }
      }

      TwoPhaseLoad.postHydrate(persister, id, values, rowId, object, lockMode, !eagerPropertyFetch, session);
   }

   private String getInstanceClass(ResultSet rs, int i, Loadable persister, Serializable id, SessionImplementor session) throws HibernateException, SQLException {
      if (persister.hasSubclasses()) {
         Object discriminatorValue = persister.getDiscriminatorType().nullSafeGet(rs, (String)this.getEntityAliases()[i].getSuffixedDiscriminatorAlias(), session, (Object)null);
         String result = persister.getSubclassForDiscriminatorValue(discriminatorValue);
         if (result == null) {
            throw new WrongClassException("Discriminator: " + discriminatorValue, id, persister.getEntityName());
         } else {
            return result;
         }
      } else {
         return persister.getEntityName();
      }
   }

   private void advance(ResultSet rs, RowSelection selection) throws SQLException {
      int firstRow = LimitHelper.getFirstRow(selection);
      if (firstRow != 0) {
         if (this.getFactory().getSettings().isScrollableResultSetsEnabled()) {
            rs.absolute(firstRow);
         } else {
            for(int m = 0; m < firstRow; ++m) {
               rs.next();
            }
         }
      }

   }

   protected LimitHandler getLimitHandler(String sql, RowSelection selection) {
      LimitHandler limitHandler = this.getFactory().getDialect().buildLimitHandler(sql, selection);
      return (LimitHandler)(LimitHelper.useLimit(limitHandler, selection) ? limitHandler : new NoopLimitHandler(sql, selection));
   }

   private ScrollMode getScrollMode(boolean scroll, boolean hasFirstRow, boolean useLimitOffSet, QueryParameters queryParameters) {
      boolean canScroll = this.getFactory().getSettings().isScrollableResultSetsEnabled();
      if (canScroll) {
         if (scroll) {
            return queryParameters.getScrollMode();
         }

         if (hasFirstRow && !useLimitOffSet) {
            return ScrollMode.SCROLL_INSENSITIVE;
         }
      }

      return null;
   }

   protected ResultSet executeQueryStatement(QueryParameters queryParameters, boolean scroll, List afterLoadActions, SessionImplementor session) throws SQLException {
      return this.executeQueryStatement(this.getSQLString(), queryParameters, scroll, afterLoadActions, session);
   }

   protected ResultSet executeQueryStatement(String sqlStatement, QueryParameters queryParameters, boolean scroll, List afterLoadActions, SessionImplementor session) throws SQLException {
      queryParameters.processFilters(this.getSQLString(), session);
      LimitHandler limitHandler = this.getLimitHandler(queryParameters.getFilteredSQL(), queryParameters.getRowSelection());
      String sql = limitHandler.getProcessedSql();
      sql = this.preprocessSQL(sql, queryParameters, this.getFactory().getDialect(), afterLoadActions);
      PreparedStatement st = this.prepareQueryStatement(sql, queryParameters, limitHandler, scroll, session);
      return this.getResultSet(st, queryParameters.getRowSelection(), limitHandler, queryParameters.hasAutoDiscoverScalarTypes(), session);
   }

   protected final PreparedStatement prepareQueryStatement(String sql, QueryParameters queryParameters, LimitHandler limitHandler, boolean scroll, SessionImplementor session) throws SQLException, HibernateException {
      Dialect dialect = this.getFactory().getDialect();
      RowSelection selection = queryParameters.getRowSelection();
      boolean useLimit = LimitHelper.useLimit(limitHandler, selection);
      boolean hasFirstRow = LimitHelper.hasFirstRow(selection);
      boolean useLimitOffset = hasFirstRow && useLimit && limitHandler.supportsLimitOffset();
      boolean callable = queryParameters.isCallable();
      ScrollMode scrollMode = this.getScrollMode(scroll, hasFirstRow, useLimitOffset, queryParameters);
      PreparedStatement st = session.getTransactionCoordinator().getJdbcCoordinator().getStatementPreparer().prepareQueryStatement(sql, callable, scrollMode);

      try {
         int col = 1;
         col += limitHandler.bindLimitParametersAtStartOfQuery(st, col);
         if (callable) {
            col = dialect.registerResultSetOutParameter((CallableStatement)st, col);
         }

         col += this.bindParameterValues(st, queryParameters, col, session);
         col += limitHandler.bindLimitParametersAtEndOfQuery(st, col);
         limitHandler.setMaxRows(st);
         if (selection != null) {
            if (selection.getTimeout() != null) {
               st.setQueryTimeout(selection.getTimeout());
            }

            if (selection.getFetchSize() != null) {
               st.setFetchSize(selection.getFetchSize());
            }
         }

         LockOptions lockOptions = queryParameters.getLockOptions();
         if (lockOptions != null && lockOptions.getTimeOut() != -1) {
            if (!dialect.supportsLockTimeouts()) {
               if (LOG.isDebugEnabled()) {
                  LOG.debugf("Lock timeout [%s] requested but dialect reported to not support lock timeouts", lockOptions.getTimeOut());
               }
            } else if (dialect.isLockTimeoutParameterized()) {
               st.setInt(col++, lockOptions.getTimeOut());
            }
         }

         LOG.tracev("Bound [{0}] parameters total", col);
         return st;
      } catch (SQLException sqle) {
         st.close();
         throw sqle;
      } catch (HibernateException he) {
         st.close();
         throw he;
      }
   }

   protected int bindParameterValues(PreparedStatement statement, QueryParameters queryParameters, int startIndex, SessionImplementor session) throws SQLException {
      int span = 0;
      span += this.bindPositionalParameters(statement, queryParameters, startIndex, session);
      span += this.bindNamedParameters(statement, queryParameters.getNamedParameters(), startIndex + span, session);
      return span;
   }

   protected int bindPositionalParameters(PreparedStatement statement, QueryParameters queryParameters, int startIndex, SessionImplementor session) throws SQLException, HibernateException {
      Object[] values = queryParameters.getFilteredPositionalParameterValues();
      Type[] types = queryParameters.getFilteredPositionalParameterTypes();
      int span = 0;

      for(int i = 0; i < values.length; ++i) {
         types[i].nullSafeSet(statement, values[i], startIndex + span, session);
         span += types[i].getColumnSpan(this.getFactory());
      }

      return span;
   }

   protected int bindNamedParameters(PreparedStatement statement, Map namedParams, int startIndex, SessionImplementor session) throws SQLException, HibernateException {
      if (namedParams == null) {
         return 0;
      } else {
         Iterator iter = namedParams.entrySet().iterator();
         boolean debugEnabled = LOG.isDebugEnabled();

         int result;
         int[] locs;
         for(result = 0; iter.hasNext(); result += locs.length) {
            Map.Entry e = (Map.Entry)iter.next();
            String name = (String)e.getKey();
            TypedValue typedval = (TypedValue)e.getValue();
            locs = this.getNamedParameterLocs(name);

            for(int i = 0; i < locs.length; ++i) {
               if (debugEnabled) {
                  LOG.debugf("bindNamedParameters() %s -> %s [%s]", typedval.getValue(), name, locs[i] + startIndex);
               }

               typedval.getType().nullSafeSet(statement, typedval.getValue(), locs[i] + startIndex, session);
            }
         }

         return result;
      }
   }

   public int[] getNamedParameterLocs(String name) {
      throw new AssertionFailure("no named parameters");
   }

   protected final ResultSet getResultSet(PreparedStatement st, RowSelection selection, LimitHandler limitHandler, boolean autodiscovertypes, SessionImplementor session) throws SQLException, HibernateException {
      try {
         ResultSet rs = st.executeQuery();
         rs = this.wrapResultSetIfEnabled(rs, session);
         if (!limitHandler.supportsLimitOffset() || !LimitHelper.useLimit(limitHandler, selection)) {
            this.advance(rs, selection);
         }

         if (autodiscovertypes) {
            this.autoDiscoverTypes(rs);
         }

         return rs;
      } catch (SQLException sqle) {
         st.close();
         throw sqle;
      }
   }

   protected void autoDiscoverTypes(ResultSet rs) {
      throw new AssertionFailure("Auto discover types not supported in this loader");
   }

   private synchronized ResultSet wrapResultSetIfEnabled(ResultSet rs, SessionImplementor session) {
      if (session.getFactory().getSettings().isWrapResultSetsEnabled()) {
         try {
            LOG.debugf("Wrapping result set [%s]", rs);
            return session.getFactory().getJdbcServices().getResultSetWrapper().wrap(rs, this.retreiveColumnNameToIndexCache(rs));
         } catch (SQLException e) {
            LOG.unableToWrapResultSet(e);
            return rs;
         }
      } else {
         return rs;
      }
   }

   private ColumnNameCache retreiveColumnNameToIndexCache(ResultSet rs) throws SQLException {
      if (this.columnNameCache == null) {
         LOG.trace("Building columnName->columnIndex cache");
         this.columnNameCache = new ColumnNameCache(rs.getMetaData().getColumnCount());
      }

      return this.columnNameCache;
   }

   protected final List loadEntity(SessionImplementor session, Object id, Type identifierType, Object optionalObject, String optionalEntityName, Serializable optionalIdentifier, EntityPersister persister, LockOptions lockOptions) throws HibernateException {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Loading entity: %s", MessageHelper.infoString(persister, id, identifierType, this.getFactory()));
      }

      List result;
      try {
         QueryParameters qp = new QueryParameters();
         qp.setPositionalParameterTypes(new Type[]{identifierType});
         qp.setPositionalParameterValues(new Object[]{id});
         qp.setOptionalObject(optionalObject);
         qp.setOptionalEntityName(optionalEntityName);
         qp.setOptionalId(optionalIdentifier);
         qp.setLockOptions(lockOptions);
         result = this.doQueryAndInitializeNonLazyCollections(session, qp, false);
      } catch (SQLException sqle) {
         Loadable[] persisters = this.getEntityPersisters();
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not load an entity: " + MessageHelper.infoString(persisters[persisters.length - 1], id, identifierType, this.getFactory()), this.getSQLString());
      }

      LOG.debug("Done entity load");
      return result;
   }

   protected final List loadEntity(SessionImplementor session, Object key, Object index, Type keyType, Type indexType, EntityPersister persister) throws HibernateException {
      LOG.debug("Loading collection element by index");

      List result;
      try {
         result = this.doQueryAndInitializeNonLazyCollections(session, new QueryParameters(new Type[]{keyType, indexType}, new Object[]{key, index}), false);
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not collection element by index", this.getSQLString());
      }

      LOG.debug("Done entity load");
      return result;
   }

   public final List loadEntityBatch(SessionImplementor session, Serializable[] ids, Type idType, Object optionalObject, String optionalEntityName, Serializable optionalId, EntityPersister persister, LockOptions lockOptions) throws HibernateException {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Batch loading entity: %s", MessageHelper.infoString(persister, ids, this.getFactory()));
      }

      Type[] types = new Type[ids.length];
      Arrays.fill(types, idType);

      List result;
      try {
         QueryParameters qp = new QueryParameters();
         qp.setPositionalParameterTypes(types);
         qp.setPositionalParameterValues(ids);
         qp.setOptionalObject(optionalObject);
         qp.setOptionalEntityName(optionalEntityName);
         qp.setOptionalId(optionalId);
         qp.setLockOptions(lockOptions);
         result = this.doQueryAndInitializeNonLazyCollections(session, qp, false);
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not load an entity batch: " + MessageHelper.infoString((EntityPersister)this.getEntityPersisters()[0], (Serializable[])ids, (SessionFactoryImplementor)this.getFactory()), this.getSQLString());
      }

      LOG.debug("Done entity batch load");
      return result;
   }

   public final void loadCollection(SessionImplementor session, Serializable id, Type type) throws HibernateException {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Loading collection: %s", MessageHelper.collectionInfoString(this.getCollectionPersisters()[0], id, this.getFactory()));
      }

      Serializable[] ids = new Serializable[]{id};

      try {
         this.doQueryAndInitializeNonLazyCollections(session, new QueryParameters(new Type[]{type}, ids, ids), true);
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not initialize a collection: " + MessageHelper.collectionInfoString(this.getCollectionPersisters()[0], id, this.getFactory()), this.getSQLString());
      }

      LOG.debug("Done loading collection");
   }

   public final void loadCollectionBatch(SessionImplementor session, Serializable[] ids, Type type) throws HibernateException {
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Batch loading collection: %s", MessageHelper.collectionInfoString(this.getCollectionPersisters()[0], ids, this.getFactory()));
      }

      Type[] idTypes = new Type[ids.length];
      Arrays.fill(idTypes, type);

      try {
         this.doQueryAndInitializeNonLazyCollections(session, new QueryParameters(idTypes, ids, ids), true);
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not initialize a collection batch: " + MessageHelper.collectionInfoString(this.getCollectionPersisters()[0], ids, this.getFactory()), this.getSQLString());
      }

      LOG.debug("Done batch load");
   }

   protected final void loadCollectionSubselect(SessionImplementor session, Serializable[] ids, Object[] parameterValues, Type[] parameterTypes, Map namedParameters, Type type) throws HibernateException {
      Type[] idTypes = new Type[ids.length];
      Arrays.fill(idTypes, type);

      try {
         this.doQueryAndInitializeNonLazyCollections(session, new QueryParameters(parameterTypes, parameterValues, namedParameters, ids), true);
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not load collection by subselect: " + MessageHelper.collectionInfoString(this.getCollectionPersisters()[0], ids, this.getFactory()), this.getSQLString());
      }
   }

   protected List list(SessionImplementor session, QueryParameters queryParameters, Set querySpaces, Type[] resultTypes) throws HibernateException {
      boolean cacheable = this.factory.getSettings().isQueryCacheEnabled() && queryParameters.isCacheable();
      return cacheable ? this.listUsingQueryCache(session, queryParameters, querySpaces, resultTypes) : this.listIgnoreQueryCache(session, queryParameters);
   }

   private List listIgnoreQueryCache(SessionImplementor session, QueryParameters queryParameters) {
      return this.getResultList(this.doList(session, queryParameters), queryParameters.getResultTransformer());
   }

   private List listUsingQueryCache(SessionImplementor session, QueryParameters queryParameters, Set querySpaces, Type[] resultTypes) {
      QueryCache queryCache = this.factory.getQueryCache(queryParameters.getCacheRegion());
      QueryKey key = this.generateQueryKey(session, queryParameters);
      if (querySpaces != null && querySpaces.size() != 0) {
         LOG.tracev("querySpaces is {0}", querySpaces);
      } else {
         LOG.tracev("Unexpected querySpaces is {0}", querySpaces == null ? querySpaces : "empty");
      }

      List result = this.getResultFromQueryCache(session, queryParameters, querySpaces, resultTypes, queryCache, key);
      if (result == null) {
         result = this.doList(session, queryParameters, key.getResultTransformer());
         this.putResultInQueryCache(session, queryParameters, resultTypes, queryCache, key, result);
      }

      ResultTransformer resolvedTransformer = this.resolveResultTransformer(queryParameters.getResultTransformer());
      if (resolvedTransformer != null) {
         result = this.areResultSetRowsTransformedImmediately() ? key.getResultTransformer().retransformResults(result, this.getResultRowAliases(), queryParameters.getResultTransformer(), this.includeInResultRow()) : key.getResultTransformer().untransformToTuples(result);
      }

      return this.getResultList(result, queryParameters.getResultTransformer());
   }

   private QueryKey generateQueryKey(SessionImplementor session, QueryParameters queryParameters) {
      return QueryKey.generateQueryKey(this.getSQLString(), queryParameters, FilterKey.createFilterKeys(session.getLoadQueryInfluencers().getEnabledFilters()), session, this.createCacheableResultTransformer(queryParameters));
   }

   private CacheableResultTransformer createCacheableResultTransformer(QueryParameters queryParameters) {
      return CacheableResultTransformer.create(queryParameters.getResultTransformer(), this.getResultRowAliases(), this.includeInResultRow());
   }

   private List getResultFromQueryCache(SessionImplementor session, QueryParameters queryParameters, Set querySpaces, Type[] resultTypes, QueryCache queryCache, QueryKey key) {
      List result = null;
      if (session.getCacheMode().isGetEnabled()) {
         boolean isImmutableNaturalKeyLookup = queryParameters.isNaturalKeyLookup() && resultTypes.length == 1 && resultTypes[0].isEntityType() && this.getEntityPersister((EntityType)EntityType.class.cast(resultTypes[0])).getEntityMetamodel().hasImmutableNaturalId();
         PersistenceContext persistenceContext = session.getPersistenceContext();
         boolean defaultReadOnlyOrig = persistenceContext.isDefaultReadOnly();
         if (queryParameters.isReadOnlyInitialized()) {
            persistenceContext.setDefaultReadOnly(queryParameters.isReadOnly());
         } else {
            queryParameters.setReadOnly(persistenceContext.isDefaultReadOnly());
         }

         try {
            result = queryCache.get(key, key.getResultTransformer().getCachedResultTypes(resultTypes), isImmutableNaturalKeyLookup, querySpaces, session);
         } finally {
            persistenceContext.setDefaultReadOnly(defaultReadOnlyOrig);
         }

         if (this.factory.getStatistics().isStatisticsEnabled()) {
            if (result == null) {
               this.factory.getStatisticsImplementor().queryCacheMiss(this.getQueryIdentifier(), queryCache.getRegion().getName());
            } else {
               this.factory.getStatisticsImplementor().queryCacheHit(this.getQueryIdentifier(), queryCache.getRegion().getName());
            }
         }
      }

      return result;
   }

   private EntityPersister getEntityPersister(EntityType entityType) {
      return this.factory.getEntityPersister(entityType.getAssociatedEntityName());
   }

   private void putResultInQueryCache(SessionImplementor session, QueryParameters queryParameters, Type[] resultTypes, QueryCache queryCache, QueryKey key, List result) {
      if (session.getCacheMode().isPutEnabled()) {
         boolean put = queryCache.put(key, key.getResultTransformer().getCachedResultTypes(resultTypes), result, queryParameters.isNaturalKeyLookup(), session);
         if (put && this.factory.getStatistics().isStatisticsEnabled()) {
            this.factory.getStatisticsImplementor().queryCachePut(this.getQueryIdentifier(), queryCache.getRegion().getName());
         }
      }

   }

   protected List doList(SessionImplementor session, QueryParameters queryParameters) throws HibernateException {
      return this.doList(session, queryParameters, (ResultTransformer)null);
   }

   private List doList(SessionImplementor session, QueryParameters queryParameters, ResultTransformer forcedResultTransformer) throws HibernateException {
      boolean stats = this.getFactory().getStatistics().isStatisticsEnabled();
      long startTime = 0L;
      if (stats) {
         startTime = System.currentTimeMillis();
      }

      List result;
      try {
         result = this.doQueryAndInitializeNonLazyCollections(session, queryParameters, true, forcedResultTransformer);
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not execute query", this.getSQLString());
      }

      if (stats) {
         this.getFactory().getStatisticsImplementor().queryExecuted(this.getQueryIdentifier(), result.size(), System.currentTimeMillis() - startTime);
      }

      return result;
   }

   protected void checkScrollability() throws HibernateException {
   }

   protected boolean needsFetchingScroll() {
      return false;
   }

   protected ScrollableResults scroll(QueryParameters queryParameters, Type[] returnTypes, HolderInstantiator holderInstantiator, SessionImplementor session) throws HibernateException {
      this.checkScrollability();
      boolean stats = this.getQueryIdentifier() != null && this.getFactory().getStatistics().isStatisticsEnabled();
      long startTime = 0L;
      if (stats) {
         startTime = System.currentTimeMillis();
      }

      try {
         ResultSet rs = this.executeQueryStatement(queryParameters, true, Collections.emptyList(), session);
         PreparedStatement st = (PreparedStatement)rs.getStatement();
         if (stats) {
            this.getFactory().getStatisticsImplementor().queryExecuted(this.getQueryIdentifier(), 0, System.currentTimeMillis() - startTime);
         }

         return (ScrollableResults)(this.needsFetchingScroll() ? new FetchingScrollableResultsImpl(rs, st, session, this, queryParameters, returnTypes, holderInstantiator) : new ScrollableResultsImpl(rs, st, session, this, queryParameters, returnTypes, holderInstantiator));
      } catch (SQLException sqle) {
         throw this.factory.getSQLExceptionHelper().convert(sqle, "could not execute query using scroll", this.getSQLString());
      }
   }

   protected void postInstantiate() {
   }

   protected abstract EntityAliases[] getEntityAliases();

   protected abstract CollectionAliases[] getCollectionAliases();

   protected String getQueryIdentifier() {
      return null;
   }

   public final SessionFactoryImplementor getFactory() {
      return this.factory;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.getSQLString() + ')';
   }

   protected interface AfterLoadAction {
      void afterLoad(SessionImplementor var1, Object var2, Loadable var3);
   }
}
