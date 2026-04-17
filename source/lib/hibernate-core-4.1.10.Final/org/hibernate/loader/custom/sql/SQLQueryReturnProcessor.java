package org.hibernate.loader.custom.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryCollectionReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryJoinReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryNonScalarReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryRootReturn;
import org.hibernate.engine.query.spi.sql.NativeSQLQueryScalarReturn;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.loader.BasicLoader;
import org.hibernate.loader.CollectionAliases;
import org.hibernate.loader.ColumnEntityAliases;
import org.hibernate.loader.DefaultEntityAliases;
import org.hibernate.loader.EntityAliases;
import org.hibernate.loader.GeneratedCollectionAliases;
import org.hibernate.loader.custom.CollectionFetchReturn;
import org.hibernate.loader.custom.CollectionReturn;
import org.hibernate.loader.custom.ColumnCollectionAliases;
import org.hibernate.loader.custom.EntityFetchReturn;
import org.hibernate.loader.custom.FetchReturn;
import org.hibernate.loader.custom.NonScalarReturn;
import org.hibernate.loader.custom.RootReturn;
import org.hibernate.loader.custom.ScalarReturn;
import org.hibernate.persister.collection.SQLLoadableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.SQLLoadable;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class SQLQueryReturnProcessor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SQLQueryReturnProcessor.class.getName());
   private NativeSQLQueryReturn[] queryReturns;
   private final Map alias2Return = new HashMap();
   private final Map alias2OwnerAlias = new HashMap();
   private final Map alias2Persister = new HashMap();
   private final Map alias2Suffix = new HashMap();
   private final Map alias2CollectionPersister = new HashMap();
   private final Map alias2CollectionSuffix = new HashMap();
   private final Map entityPropertyResultMaps = new HashMap();
   private final Map collectionPropertyResultMaps = new HashMap();
   private final SessionFactoryImplementor factory;
   private int entitySuffixSeed = 0;
   private int collectionSuffixSeed = 0;

   public SQLQueryReturnProcessor(NativeSQLQueryReturn[] queryReturns, SessionFactoryImplementor factory) {
      this.queryReturns = queryReturns;
      this.factory = factory;
   }

   private Map internalGetPropertyResultsMap(String alias) {
      NativeSQLQueryReturn rtn = (NativeSQLQueryReturn)this.alias2Return.get(alias);
      return rtn instanceof NativeSQLQueryNonScalarReturn ? ((NativeSQLQueryNonScalarReturn)rtn).getPropertyResultsMap() : null;
   }

   private boolean hasPropertyResultMap(String alias) {
      Map propertyMaps = this.internalGetPropertyResultsMap(alias);
      return propertyMaps != null && !propertyMaps.isEmpty();
   }

   public ResultAliasContext process() {
      for(int i = 0; i < this.queryReturns.length; ++i) {
         if (this.queryReturns[i] instanceof NativeSQLQueryNonScalarReturn) {
            NativeSQLQueryNonScalarReturn rtn = (NativeSQLQueryNonScalarReturn)this.queryReturns[i];
            this.alias2Return.put(rtn.getAlias(), rtn);
            if (rtn instanceof NativeSQLQueryJoinReturn) {
               NativeSQLQueryJoinReturn fetchReturn = (NativeSQLQueryJoinReturn)rtn;
               this.alias2OwnerAlias.put(fetchReturn.getAlias(), fetchReturn.getOwnerAlias());
            }
         }
      }

      for(int i = 0; i < this.queryReturns.length; ++i) {
         this.processReturn(this.queryReturns[i]);
      }

      return new ResultAliasContext();
   }

   public List generateCustomReturns(boolean queryHadAliases) {
      List customReturns = new ArrayList();
      Map customReturnsByAlias = new HashMap();

      for(int i = 0; i < this.queryReturns.length; ++i) {
         if (this.queryReturns[i] instanceof NativeSQLQueryScalarReturn) {
            NativeSQLQueryScalarReturn rtn = (NativeSQLQueryScalarReturn)this.queryReturns[i];
            customReturns.add(new ScalarReturn(rtn.getType(), rtn.getColumnAlias()));
         } else if (this.queryReturns[i] instanceof NativeSQLQueryRootReturn) {
            NativeSQLQueryRootReturn rtn = (NativeSQLQueryRootReturn)this.queryReturns[i];
            String alias = rtn.getAlias();
            EntityAliases entityAliases;
            if (!queryHadAliases && !this.hasPropertyResultMap(alias)) {
               entityAliases = new ColumnEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
            } else {
               entityAliases = new DefaultEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
            }

            RootReturn customReturn = new RootReturn(alias, rtn.getReturnEntityName(), entityAliases, rtn.getLockMode());
            customReturns.add(customReturn);
            customReturnsByAlias.put(rtn.getAlias(), customReturn);
         } else if (this.queryReturns[i] instanceof NativeSQLQueryCollectionReturn) {
            NativeSQLQueryCollectionReturn rtn = (NativeSQLQueryCollectionReturn)this.queryReturns[i];
            String alias = rtn.getAlias();
            SQLLoadableCollection persister = (SQLLoadableCollection)this.alias2CollectionPersister.get(alias);
            boolean isEntityElements = persister.getElementType().isEntityType();
            EntityAliases elementEntityAliases = null;
            CollectionAliases collectionAliases;
            if (!queryHadAliases && !this.hasPropertyResultMap(alias)) {
               collectionAliases = new ColumnCollectionAliases((Map)this.collectionPropertyResultMaps.get(alias), (SQLLoadableCollection)this.alias2CollectionPersister.get(alias));
               if (isEntityElements) {
                  elementEntityAliases = new ColumnEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
               }
            } else {
               collectionAliases = new GeneratedCollectionAliases((Map)this.collectionPropertyResultMaps.get(alias), (SQLLoadableCollection)this.alias2CollectionPersister.get(alias), (String)this.alias2CollectionSuffix.get(alias));
               if (isEntityElements) {
                  elementEntityAliases = new DefaultEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
               }
            }

            CollectionReturn customReturn = new CollectionReturn(alias, rtn.getOwnerEntityName(), rtn.getOwnerProperty(), collectionAliases, elementEntityAliases, rtn.getLockMode());
            customReturns.add(customReturn);
            customReturnsByAlias.put(rtn.getAlias(), customReturn);
         } else if (this.queryReturns[i] instanceof NativeSQLQueryJoinReturn) {
            NativeSQLQueryJoinReturn rtn = (NativeSQLQueryJoinReturn)this.queryReturns[i];
            String alias = rtn.getAlias();
            NonScalarReturn ownerCustomReturn = (NonScalarReturn)customReturnsByAlias.get(rtn.getOwnerAlias());
            FetchReturn customReturn;
            if (this.alias2CollectionPersister.containsKey(alias)) {
               SQLLoadableCollection persister = (SQLLoadableCollection)this.alias2CollectionPersister.get(alias);
               boolean isEntityElements = persister.getElementType().isEntityType();
               EntityAliases elementEntityAliases = null;
               CollectionAliases collectionAliases;
               if (!queryHadAliases && !this.hasPropertyResultMap(alias)) {
                  collectionAliases = new ColumnCollectionAliases((Map)this.collectionPropertyResultMaps.get(alias), persister);
                  if (isEntityElements) {
                     elementEntityAliases = new ColumnEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
                  }
               } else {
                  collectionAliases = new GeneratedCollectionAliases((Map)this.collectionPropertyResultMaps.get(alias), persister, (String)this.alias2CollectionSuffix.get(alias));
                  if (isEntityElements) {
                     elementEntityAliases = new DefaultEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
                  }
               }

               customReturn = new CollectionFetchReturn(alias, ownerCustomReturn, rtn.getOwnerProperty(), collectionAliases, elementEntityAliases, rtn.getLockMode());
            } else {
               EntityAliases entityAliases;
               if (!queryHadAliases && !this.hasPropertyResultMap(alias)) {
                  entityAliases = new ColumnEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
               } else {
                  entityAliases = new DefaultEntityAliases((Map)this.entityPropertyResultMaps.get(alias), (SQLLoadable)this.alias2Persister.get(alias), (String)this.alias2Suffix.get(alias));
               }

               customReturn = new EntityFetchReturn(alias, entityAliases, ownerCustomReturn, rtn.getOwnerProperty(), rtn.getLockMode());
            }

            customReturns.add(customReturn);
            customReturnsByAlias.put(alias, customReturn);
         }
      }

      return customReturns;
   }

   private SQLLoadable getSQLLoadable(String entityName) throws MappingException {
      EntityPersister persister = this.factory.getEntityPersister(entityName);
      if (!(persister instanceof SQLLoadable)) {
         throw new MappingException("class persister is not SQLLoadable: " + entityName);
      } else {
         return (SQLLoadable)persister;
      }
   }

   private String generateEntitySuffix() {
      return BasicLoader.generateSuffixes(this.entitySuffixSeed++, 1)[0];
   }

   private String generateCollectionSuffix() {
      return this.collectionSuffixSeed++ + "__";
   }

   private void processReturn(NativeSQLQueryReturn rtn) {
      if (rtn instanceof NativeSQLQueryScalarReturn) {
         this.processScalarReturn((NativeSQLQueryScalarReturn)rtn);
      } else if (rtn instanceof NativeSQLQueryRootReturn) {
         this.processRootReturn((NativeSQLQueryRootReturn)rtn);
      } else if (rtn instanceof NativeSQLQueryCollectionReturn) {
         this.processCollectionReturn((NativeSQLQueryCollectionReturn)rtn);
      } else {
         this.processJoinReturn((NativeSQLQueryJoinReturn)rtn);
      }

   }

   private void processScalarReturn(NativeSQLQueryScalarReturn typeReturn) {
   }

   private void processRootReturn(NativeSQLQueryRootReturn rootReturn) {
      if (!this.alias2Persister.containsKey(rootReturn.getAlias())) {
         SQLLoadable persister = this.getSQLLoadable(rootReturn.getReturnEntityName());
         this.addPersister(rootReturn.getAlias(), rootReturn.getPropertyResultsMap(), persister);
      }
   }

   private void addPersister(String alias, Map propertyResult, SQLLoadable persister) {
      this.alias2Persister.put(alias, persister);
      String suffix = this.generateEntitySuffix();
      LOG.tracev("Mapping alias [{0}] to entity-suffix [{1}]", alias, suffix);
      this.alias2Suffix.put(alias, suffix);
      this.entityPropertyResultMaps.put(alias, propertyResult);
   }

   private void addCollection(String role, String alias, Map propertyResults) {
      SQLLoadableCollection collectionPersister = (SQLLoadableCollection)this.factory.getCollectionPersister(role);
      this.alias2CollectionPersister.put(alias, collectionPersister);
      String suffix = this.generateCollectionSuffix();
      LOG.tracev("Mapping alias [{0}] to collection-suffix [{1}]", alias, suffix);
      this.alias2CollectionSuffix.put(alias, suffix);
      this.collectionPropertyResultMaps.put(alias, propertyResults);
      if (collectionPersister.isOneToMany() || collectionPersister.isManyToMany()) {
         SQLLoadable persister = (SQLLoadable)collectionPersister.getElementPersister();
         this.addPersister(alias, this.filter(propertyResults), persister);
      }

   }

   private Map filter(Map propertyResults) {
      Map result = new HashMap(propertyResults.size());
      String keyPrefix = "element.";

      for(Map.Entry element : propertyResults.entrySet()) {
         String path = (String)element.getKey();
         if (path.startsWith(keyPrefix)) {
            result.put(path.substring(keyPrefix.length()), element.getValue());
         }
      }

      return result;
   }

   private void processCollectionReturn(NativeSQLQueryCollectionReturn collectionReturn) {
      String role = collectionReturn.getOwnerEntityName() + '.' + collectionReturn.getOwnerProperty();
      this.addCollection(role, collectionReturn.getAlias(), collectionReturn.getPropertyResultsMap());
   }

   private void processJoinReturn(NativeSQLQueryJoinReturn fetchReturn) {
      String alias = fetchReturn.getAlias();
      if (!this.alias2Persister.containsKey(alias) && !this.alias2CollectionPersister.containsKey(alias)) {
         String ownerAlias = fetchReturn.getOwnerAlias();
         if (!this.alias2Return.containsKey(ownerAlias)) {
            throw new HibernateException("Owner alias [" + ownerAlias + "] is unknown for alias [" + alias + "]");
         } else {
            if (!this.alias2Persister.containsKey(ownerAlias)) {
               NativeSQLQueryNonScalarReturn ownerReturn = (NativeSQLQueryNonScalarReturn)this.alias2Return.get(ownerAlias);
               this.processReturn(ownerReturn);
            }

            SQLLoadable ownerPersister = (SQLLoadable)this.alias2Persister.get(ownerAlias);
            Type returnType = ownerPersister.getPropertyType(fetchReturn.getOwnerProperty());
            if (returnType.isCollectionType()) {
               String role = ownerPersister.getEntityName() + '.' + fetchReturn.getOwnerProperty();
               this.addCollection(role, alias, fetchReturn.getPropertyResultsMap());
            } else if (returnType.isEntityType()) {
               EntityType eType = (EntityType)returnType;
               String returnEntityName = eType.getAssociatedEntityName();
               SQLLoadable persister = this.getSQLLoadable(returnEntityName);
               this.addPersister(alias, fetchReturn.getPropertyResultsMap(), persister);
            }

         }
      }
   }

   class ResultAliasContext {
      public SQLLoadable getEntityPersister(String alias) {
         return (SQLLoadable)SQLQueryReturnProcessor.this.alias2Persister.get(alias);
      }

      public SQLLoadableCollection getCollectionPersister(String alias) {
         return (SQLLoadableCollection)SQLQueryReturnProcessor.this.alias2CollectionPersister.get(alias);
      }

      public String getEntitySuffix(String alias) {
         return (String)SQLQueryReturnProcessor.this.alias2Suffix.get(alias);
      }

      public String getCollectionSuffix(String alias) {
         return (String)SQLQueryReturnProcessor.this.alias2CollectionSuffix.get(alias);
      }

      public String getOwnerAlias(String alias) {
         return (String)SQLQueryReturnProcessor.this.alias2OwnerAlias.get(alias);
      }

      public Map getPropertyResultsMap(String alias) {
         return SQLQueryReturnProcessor.this.internalGetPropertyResultsMap(alias);
      }
   }
}
