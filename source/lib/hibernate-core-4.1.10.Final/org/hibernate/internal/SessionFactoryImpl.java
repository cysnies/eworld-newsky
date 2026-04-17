package org.hibernate.internal;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import org.hibernate.AssertionFailure;
import org.hibernate.Cache;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.CustomEntityDirtinessStrategy;
import org.hibernate.EmptyInterceptor;
import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.MappingException;
import org.hibernate.MultiTenancyStrategy;
import org.hibernate.ObjectNotFoundException;
import org.hibernate.QueryException;
import org.hibernate.Session;
import org.hibernate.SessionBuilder;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.StatelessSession;
import org.hibernate.StatelessSessionBuilder;
import org.hibernate.TypeHelper;
import org.hibernate.cache.internal.CacheDataDescriptionImpl;
import org.hibernate.cache.spi.CollectionRegion;
import org.hibernate.cache.spi.EntityRegion;
import org.hibernate.cache.spi.NaturalIdRegion;
import org.hibernate.cache.spi.QueryCache;
import org.hibernate.cache.spi.Region;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.UpdateTimestampsCache;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cache.spi.access.CollectionRegionAccessStrategy;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.cache.spi.access.RegionAccessStrategy;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.context.internal.JTASessionContext;
import org.hibernate.context.internal.ManagedSessionContext;
import org.hibernate.context.internal.ThreadLocalSessionContext;
import org.hibernate.context.spi.CurrentSessionContext;
import org.hibernate.context.spi.CurrentTenantIdentifierResolver;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.jdbc.spi.JdbcConnectionAccess;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.jdbc.spi.SqlExceptionHelper;
import org.hibernate.engine.profile.Association;
import org.hibernate.engine.profile.Fetch;
import org.hibernate.engine.query.spi.QueryPlanCache;
import org.hibernate.engine.query.spi.sql.NativeSQLQuerySpecification;
import org.hibernate.engine.spi.CacheImplementor;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.engine.spi.SessionBuilderImplementor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.SessionOwner;
import org.hibernate.engine.transaction.internal.TransactionCoordinatorImpl;
import org.hibernate.engine.transaction.spi.TransactionEnvironment;
import org.hibernate.engine.transaction.spi.TransactionFactory;
import org.hibernate.exception.spi.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.id.UUIDGenerator;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.integrator.spi.IntegratorService;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.FetchProfile;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.Queryable;
import org.hibernate.persister.spi.PersisterFactory;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.service.config.spi.ConfigurationService;
import org.hibernate.service.jdbc.connections.spi.ConnectionProvider;
import org.hibernate.service.jdbc.connections.spi.MultiTenantConnectionProvider;
import org.hibernate.service.jndi.spi.JndiService;
import org.hibernate.service.jta.platform.spi.JtaPlatform;
import org.hibernate.service.spi.ServiceRegistryImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.hibernate.service.spi.SessionFactoryServiceRegistryFactory;
import org.hibernate.stat.Statistics;
import org.hibernate.stat.spi.StatisticsImplementor;
import org.hibernate.tool.hbm2ddl.ImportSqlCommandExtractor;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.tool.hbm2ddl.SchemaValidator;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.AssociationType;
import org.hibernate.type.Type;
import org.hibernate.type.TypeResolver;
import org.jboss.logging.Logger;

public final class SessionFactoryImpl implements SessionFactoryImplementor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SessionFactoryImpl.class.getName());
   private static final IdentifierGenerator UUID_GENERATOR = UUIDGenerator.buildSessionFactoryUniqueIdentifierGenerator();
   private final String name;
   private final String uuid;
   private final transient Map entityPersisters;
   private final transient Map classMetadata;
   private final transient Map collectionPersisters;
   private final transient Map collectionMetadata;
   private final transient Map collectionRolesByEntityParticipant;
   private final transient Map identifierGenerators;
   private final transient Map namedQueries;
   private final transient Map namedSqlQueries;
   private final transient Map sqlResultSetMappings;
   private final transient Map filters;
   private final transient Map fetchProfiles;
   private final transient Map imports;
   private final transient SessionFactoryServiceRegistry serviceRegistry;
   private final transient JdbcServices jdbcServices;
   private final transient Dialect dialect;
   private final transient Settings settings;
   private final transient Properties properties;
   private transient SchemaExport schemaExport;
   private final transient CurrentSessionContext currentSessionContext;
   private final transient SQLFunctionRegistry sqlFunctionRegistry;
   private final transient SessionFactoryObserverChain observer = new SessionFactoryObserverChain();
   private final transient ConcurrentHashMap entityNameResolvers = new ConcurrentHashMap();
   private final transient QueryPlanCache queryPlanCache;
   private final transient CacheImplementor cacheAccess;
   private transient boolean isClosed = false;
   private final transient TypeResolver typeResolver;
   private final transient TypeHelper typeHelper;
   private final transient TransactionEnvironment transactionEnvironment;
   private final transient SessionFactory.SessionFactoryOptions sessionFactoryOptions;
   private final transient CustomEntityDirtinessStrategy customEntityDirtinessStrategy;
   private final transient CurrentTenantIdentifierResolver currentTenantIdentifierResolver;
   private static final Object ENTITY_NAME_RESOLVER_MAP_VALUE = new Object();

   public SessionFactoryImpl(final Configuration cfg, Mapping mapping, ServiceRegistry serviceRegistry, Settings settings, SessionFactoryObserver observer) throws HibernateException {
      LOG.debug("Building session factory");
      this.sessionFactoryOptions = new SessionFactory.SessionFactoryOptions() {
         private EntityNotFoundDelegate entityNotFoundDelegate;

         public Interceptor getInterceptor() {
            return cfg.getInterceptor();
         }

         public EntityNotFoundDelegate getEntityNotFoundDelegate() {
            if (this.entityNotFoundDelegate == null) {
               if (cfg.getEntityNotFoundDelegate() != null) {
                  this.entityNotFoundDelegate = cfg.getEntityNotFoundDelegate();
               } else {
                  this.entityNotFoundDelegate = new EntityNotFoundDelegate() {
                     public void handleEntityNotFound(String entityName, Serializable id) {
                        throw new ObjectNotFoundException(id, entityName);
                     }
                  };
               }
            }

            return this.entityNotFoundDelegate;
         }
      };
      this.settings = settings;
      this.properties = new Properties();
      this.properties.putAll(cfg.getProperties());
      this.serviceRegistry = ((SessionFactoryServiceRegistryFactory)serviceRegistry.getService(SessionFactoryServiceRegistryFactory.class)).buildServiceRegistry(this, (Configuration)cfg);
      this.jdbcServices = (JdbcServices)this.serviceRegistry.getService(JdbcServices.class);
      this.dialect = this.jdbcServices.getDialect();
      this.cacheAccess = (CacheImplementor)this.serviceRegistry.getService(CacheImplementor.class);
      RegionFactory regionFactory = this.cacheAccess.getRegionFactory();
      this.sqlFunctionRegistry = new SQLFunctionRegistry(this.getDialect(), cfg.getSqlFunctions());
      if (observer != null) {
         this.observer.addObserver(observer);
      }

      this.typeResolver = cfg.getTypeResolver().scope(this);
      this.typeHelper = new TypeLocatorImpl(this.typeResolver);
      this.filters = new HashMap();
      this.filters.putAll(cfg.getFilterDefinitions());
      LOG.debugf("Session factory constructed with filter configurations : %s", this.filters);
      LOG.debugf("Instantiating session factory with properties: %s", this.properties);
      this.queryPlanCache = new QueryPlanCache(this);

      class IntegratorObserver implements SessionFactoryObserver {
         private ArrayList integrators = new ArrayList();

         public void sessionFactoryCreated(SessionFactory factory) {
         }

         public void sessionFactoryClosed(SessionFactory factory) {
            for(Integrator integrator : this.integrators) {
               integrator.disintegrate(SessionFactoryImpl.this, SessionFactoryImpl.this.serviceRegistry);
            }

         }
      }

      IntegratorObserver integratorObserver = new IntegratorObserver();
      this.observer.addObserver(integratorObserver);

      for(Integrator integrator : ((IntegratorService)serviceRegistry.getService(IntegratorService.class)).getIntegrators()) {
         integrator.integrate((Configuration)cfg, this, this.serviceRegistry);
         integratorObserver.integrators.add(integrator);
      }

      this.identifierGenerators = new HashMap();
      Iterator classes = cfg.getClassMappings();

      while(classes.hasNext()) {
         PersistentClass model = (PersistentClass)classes.next();
         if (!model.isInherited()) {
            IdentifierGenerator generator = model.getIdentifier().createIdentifierGenerator(cfg.getIdentifierGeneratorFactory(), this.getDialect(), settings.getDefaultCatalogName(), settings.getDefaultSchemaName(), (RootClass)model);
            this.identifierGenerators.put(model.getEntityName(), generator);
         }
      }

      String cacheRegionPrefix = settings.getCacheRegionPrefix() == null ? "" : settings.getCacheRegionPrefix() + ".";
      this.entityPersisters = new HashMap();
      Map entityAccessStrategies = new HashMap();
      Map<String, ClassMetadata> classMeta = new HashMap();
      classes = cfg.getClassMappings();

      while(classes.hasNext()) {
         PersistentClass model = (PersistentClass)classes.next();
         model.prepareTemporaryTables(mapping, this.getDialect());
         String cacheRegionName = cacheRegionPrefix + model.getRootClass().getCacheRegionName();
         EntityRegionAccessStrategy accessStrategy = (EntityRegionAccessStrategy)entityAccessStrategies.get(cacheRegionName);
         if (accessStrategy == null && settings.isSecondLevelCacheEnabled()) {
            AccessType accessType = AccessType.fromExternalName(model.getCacheConcurrencyStrategy());
            if (accessType != null) {
               LOG.tracef("Building shared cache region for entity data [%s]", model.getEntityName());
               EntityRegion entityRegion = regionFactory.buildEntityRegion(cacheRegionName, this.properties, CacheDataDescriptionImpl.decode(model));
               accessStrategy = entityRegion.buildAccessStrategy(accessType);
               entityAccessStrategies.put(cacheRegionName, accessStrategy);
               this.cacheAccess.addCacheRegion(cacheRegionName, entityRegion);
            }
         }

         NaturalIdRegionAccessStrategy naturalIdAccessStrategy = null;
         if (model.hasNaturalId() && model.getNaturalIdCacheRegionName() != null) {
            String naturalIdCacheRegionName = cacheRegionPrefix + model.getNaturalIdCacheRegionName();
            naturalIdAccessStrategy = (NaturalIdRegionAccessStrategy)entityAccessStrategies.get(naturalIdCacheRegionName);
            if (naturalIdAccessStrategy == null && settings.isSecondLevelCacheEnabled()) {
               CacheDataDescriptionImpl cacheDataDescription = CacheDataDescriptionImpl.decode(model);
               NaturalIdRegion naturalIdRegion = null;

               try {
                  naturalIdRegion = regionFactory.buildNaturalIdRegion(naturalIdCacheRegionName, this.properties, cacheDataDescription);
               } catch (UnsupportedOperationException var26) {
                  LOG.warnf("Shared cache region factory [%s] does not support natural id caching; shared NaturalId caching will be disabled for not be enabled for %s", regionFactory.getClass().getName(), model.getEntityName());
               }

               if (naturalIdRegion != null) {
                  naturalIdAccessStrategy = naturalIdRegion.buildAccessStrategy(regionFactory.getDefaultAccessType());
                  entityAccessStrategies.put(naturalIdCacheRegionName, naturalIdAccessStrategy);
                  this.cacheAccess.addCacheRegion(naturalIdCacheRegionName, naturalIdRegion);
               }
            }
         }

         EntityPersister cp = ((PersisterFactory)serviceRegistry.getService(PersisterFactory.class)).createEntityPersister(model, accessStrategy, naturalIdAccessStrategy, this, mapping);
         this.entityPersisters.put(model.getEntityName(), cp);
         classMeta.put(model.getEntityName(), cp.getClassMetadata());
      }

      this.classMetadata = Collections.unmodifiableMap(classMeta);
      Map<String, Set<String>> tmpEntityToCollectionRoleMap = new HashMap();
      this.collectionPersisters = new HashMap();
      Map<String, CollectionMetadata> tmpCollectionMetadata = new HashMap();
      Iterator collections = cfg.getCollectionMappings();

      while(collections.hasNext()) {
         Collection model = (Collection)collections.next();
         String cacheRegionName = cacheRegionPrefix + model.getCacheRegionName();
         AccessType accessType = AccessType.fromExternalName(model.getCacheConcurrencyStrategy());
         CollectionRegionAccessStrategy accessStrategy = null;
         if (accessType != null && settings.isSecondLevelCacheEnabled()) {
            LOG.tracev("Building shared cache region for collection data [{0}]", model.getRole());
            CollectionRegion collectionRegion = regionFactory.buildCollectionRegion(cacheRegionName, this.properties, CacheDataDescriptionImpl.decode(model));
            accessStrategy = collectionRegion.buildAccessStrategy(accessType);
            entityAccessStrategies.put(cacheRegionName, accessStrategy);
            this.cacheAccess.addCacheRegion(cacheRegionName, collectionRegion);
         }

         CollectionPersister persister = ((PersisterFactory)serviceRegistry.getService(PersisterFactory.class)).createCollectionPersister((Configuration)cfg, (Collection)model, accessStrategy, this);
         this.collectionPersisters.put(model.getRole(), persister);
         tmpCollectionMetadata.put(model.getRole(), persister.getCollectionMetadata());
         Type indexType = persister.getIndexType();
         if (indexType != null && indexType.isAssociationType() && !indexType.isAnyType()) {
            String entityName = ((AssociationType)indexType).getAssociatedEntityName(this);
            Set roles = (Set)tmpEntityToCollectionRoleMap.get(entityName);
            if (roles == null) {
               roles = new HashSet();
               tmpEntityToCollectionRoleMap.put(entityName, roles);
            }

            roles.add(persister.getRole());
         }

         Type elementType = persister.getElementType();
         if (elementType.isAssociationType() && !elementType.isAnyType()) {
            String entityName = ((AssociationType)elementType).getAssociatedEntityName(this);
            Set roles = (Set)tmpEntityToCollectionRoleMap.get(entityName);
            if (roles == null) {
               roles = new HashSet();
               tmpEntityToCollectionRoleMap.put(entityName, roles);
            }

            roles.add(persister.getRole());
         }
      }

      this.collectionMetadata = Collections.unmodifiableMap(tmpCollectionMetadata);

      for(Map.Entry entry : tmpEntityToCollectionRoleMap.entrySet()) {
         entry.setValue(Collections.unmodifiableSet((Set)entry.getValue()));
      }

      this.collectionRolesByEntityParticipant = Collections.unmodifiableMap(tmpEntityToCollectionRoleMap);
      this.namedQueries = new HashMap(cfg.getNamedQueries());
      this.namedSqlQueries = new HashMap(cfg.getNamedSQLQueries());
      this.sqlResultSetMappings = new HashMap(cfg.getSqlResultSetMappings());
      this.imports = new HashMap(cfg.getImports());

      for(EntityPersister persister : this.entityPersisters.values()) {
         persister.postInstantiate();
         this.registerEntityNameResolvers(persister);
      }

      for(CollectionPersister persister : this.collectionPersisters.values()) {
         persister.postInstantiate();
      }

      this.name = settings.getSessionFactoryName();

      try {
         this.uuid = (String)UUID_GENERATOR.generate((SessionImplementor)null, (Object)null);
      } catch (Exception var25) {
         throw new AssertionFailure("Could not generate UUID");
      }

      SessionFactoryRegistry.INSTANCE.addSessionFactory(this.uuid, this.name, settings.isSessionFactoryNameAlsoJndiName(), this, (JndiService)serviceRegistry.getService(JndiService.class));
      LOG.debug("Instantiated session factory");
      settings.getMultiTableBulkIdStrategy().prepare(this.jdbcServices, this.buildLocalConnectionAccess(), cfg.createMappings(), cfg.buildMapping(), this.properties);
      if (settings.isAutoCreateSchema()) {
         (new SchemaExport(serviceRegistry, cfg)).setImportSqlCommandExtractor((ImportSqlCommandExtractor)serviceRegistry.getService(ImportSqlCommandExtractor.class)).create(false, true);
      }

      if (settings.isAutoUpdateSchema()) {
         (new SchemaUpdate(serviceRegistry, cfg)).execute(false, true);
      }

      if (settings.isAutoValidateSchema()) {
         (new SchemaValidator(serviceRegistry, cfg)).validate();
      }

      if (settings.isAutoDropSchema()) {
         this.schemaExport = (new SchemaExport(serviceRegistry, cfg)).setImportSqlCommandExtractor((ImportSqlCommandExtractor)serviceRegistry.getService(ImportSqlCommandExtractor.class));
      }

      this.currentSessionContext = this.buildCurrentSessionContext();
      if (settings.isNamedQueryStartupCheckingEnabled()) {
         Map<String, HibernateException> errors = this.checkNamedQueries();
         if (!errors.isEmpty()) {
            StringBuilder failingQueries = new StringBuilder("Errors in named queries: ");
            String sep = "";

            for(Map.Entry entry : errors.entrySet()) {
               LOG.namedQueryError((String)entry.getKey(), (HibernateException)entry.getValue());
               failingQueries.append(sep).append((String)entry.getKey());
               sep = ", ";
            }

            throw new HibernateException(failingQueries.toString());
         }
      }

      this.fetchProfiles = new HashMap();
      Iterator var38 = cfg.iterateFetchProfiles();

      while(var38.hasNext()) {
         FetchProfile mappingProfile = (FetchProfile)var38.next();
         org.hibernate.engine.profile.FetchProfile fetchProfile = new org.hibernate.engine.profile.FetchProfile(mappingProfile.getName());

         for(FetchProfile.Fetch mappingFetch : mappingProfile.getFetches()) {
            String entityName = this.getImportedClassName(mappingFetch.getEntity());
            EntityPersister owner = entityName == null ? null : (EntityPersister)this.entityPersisters.get(entityName);
            if (owner == null) {
               throw new HibernateException("Unable to resolve entity reference [" + mappingFetch.getEntity() + "] in fetch profile [" + fetchProfile.getName() + "]");
            }

            Type associationType = owner.getPropertyType(mappingFetch.getAssociation());
            if (associationType == null || !associationType.isAssociationType()) {
               throw new HibernateException("Fetch profile [" + fetchProfile.getName() + "] specified an invalid association");
            }

            Fetch.Style fetchStyle = Fetch.Style.parse(mappingFetch.getStyle());
            fetchProfile.addFetch(new Association(owner, mappingFetch.getAssociation()), fetchStyle);
            ((Loadable)owner).registerAffectingFetchProfile(fetchProfile.getName());
         }

         this.fetchProfiles.put(fetchProfile.getName(), fetchProfile);
      }

      this.customEntityDirtinessStrategy = this.determineCustomEntityDirtinessStrategy();
      this.currentTenantIdentifierResolver = this.determineCurrentTenantIdentifierResolver(cfg.getCurrentTenantIdentifierResolver());
      this.transactionEnvironment = new TransactionEnvironmentImpl(this);
      this.observer.sessionFactoryCreated(this);
   }

   private JdbcConnectionAccess buildLocalConnectionAccess() {
      return new JdbcConnectionAccess() {
         public Connection obtainConnection() throws SQLException {
            return SessionFactoryImpl.this.settings.getMultiTenancyStrategy() == MultiTenancyStrategy.NONE ? ((ConnectionProvider)SessionFactoryImpl.this.serviceRegistry.getService(ConnectionProvider.class)).getConnection() : ((MultiTenantConnectionProvider)SessionFactoryImpl.this.serviceRegistry.getService(MultiTenantConnectionProvider.class)).getAnyConnection();
         }

         public void releaseConnection(Connection connection) throws SQLException {
            if (SessionFactoryImpl.this.settings.getMultiTenancyStrategy() == MultiTenancyStrategy.NONE) {
               ((ConnectionProvider)SessionFactoryImpl.this.serviceRegistry.getService(ConnectionProvider.class)).closeConnection(connection);
            } else {
               ((MultiTenantConnectionProvider)SessionFactoryImpl.this.serviceRegistry.getService(MultiTenantConnectionProvider.class)).releaseAnyConnection(connection);
            }

         }

         public boolean supportsAggressiveRelease() {
            return false;
         }
      };
   }

   private CustomEntityDirtinessStrategy determineCustomEntityDirtinessStrategy() {
      CustomEntityDirtinessStrategy defaultValue = new CustomEntityDirtinessStrategy() {
         public boolean canDirtyCheck(Object entity, EntityPersister persister, Session session) {
            return false;
         }

         public boolean isDirty(Object entity, EntityPersister persister, Session session) {
            return false;
         }

         public void resetDirty(Object entity, EntityPersister persister, Session session) {
         }

         public void findDirty(Object entity, EntityPersister persister, Session session, CustomEntityDirtinessStrategy.DirtyCheckContext dirtyCheckContext) {
         }
      };
      return (CustomEntityDirtinessStrategy)((ConfigurationService)this.serviceRegistry.getService(ConfigurationService.class)).getSetting("hibernate.entity_dirtiness_strategy", (Class)CustomEntityDirtinessStrategy.class, defaultValue);
   }

   private CurrentTenantIdentifierResolver determineCurrentTenantIdentifierResolver(CurrentTenantIdentifierResolver explicitResolver) {
      return explicitResolver != null ? explicitResolver : (CurrentTenantIdentifierResolver)((ConfigurationService)this.serviceRegistry.getService(ConfigurationService.class)).getSetting("hibernate.tenant_identifier_resolver", (Class)CurrentTenantIdentifierResolver.class, (Object)null);
   }

   public SessionFactoryImpl(MetadataImplementor metadata, SessionFactory.SessionFactoryOptions sessionFactoryOptions, SessionFactoryObserver observer) throws HibernateException {
      LOG.debug("Building session factory");
      this.sessionFactoryOptions = sessionFactoryOptions;
      this.properties = createPropertiesFromMap(((ConfigurationService)metadata.getServiceRegistry().getService(ConfigurationService.class)).getSettings());
      this.settings = (new SettingsFactory()).buildSettings(this.properties, metadata.getServiceRegistry());
      this.serviceRegistry = ((SessionFactoryServiceRegistryFactory)metadata.getServiceRegistry().getService(SessionFactoryServiceRegistryFactory.class)).buildServiceRegistry(this, (MetadataImplementor)metadata);
      this.jdbcServices = (JdbcServices)this.serviceRegistry.getService(JdbcServices.class);
      this.dialect = this.jdbcServices.getDialect();
      this.cacheAccess = (CacheImplementor)this.serviceRegistry.getService(CacheImplementor.class);
      this.sqlFunctionRegistry = new SQLFunctionRegistry(this.dialect, new HashMap());
      if (observer != null) {
         this.observer.addObserver(observer);
      }

      this.typeResolver = metadata.getTypeResolver().scope(this);
      this.typeHelper = new TypeLocatorImpl(this.typeResolver);
      this.filters = new HashMap();

      for(FilterDefinition filterDefinition : metadata.getFilterDefinitions()) {
         this.filters.put(filterDefinition.getFilterName(), filterDefinition);
      }

      LOG.debugf("Session factory constructed with filter configurations : %s", this.filters);
      LOG.debugf("Instantiating session factory with properties: %s", this.properties);
      this.queryPlanCache = new QueryPlanCache(this);

      class IntegratorObserver implements SessionFactoryObserver {
         private ArrayList integrators = new ArrayList();

         public void sessionFactoryCreated(SessionFactory factory) {
         }

         public void sessionFactoryClosed(SessionFactory factory) {
            for(Integrator integrator : this.integrators) {
               integrator.disintegrate(SessionFactoryImpl.this, SessionFactoryImpl.this.serviceRegistry);
            }

         }
      }

      IntegratorObserver integratorObserver = new IntegratorObserver();
      this.observer.addObserver(integratorObserver);

      for(Integrator integrator : ((IntegratorService)this.serviceRegistry.getService(IntegratorService.class)).getIntegrators()) {
         integrator.integrate((MetadataImplementor)metadata, this, this.serviceRegistry);
         integratorObserver.integrators.add(integrator);
      }

      this.identifierGenerators = new HashMap();

      for(EntityBinding entityBinding : metadata.getEntityBindings()) {
         if (entityBinding.isRoot()) {
            this.identifierGenerators.put(entityBinding.getEntity().getName(), entityBinding.getHierarchyDetails().getEntityIdentifier().getIdentifierGenerator());
         }
      }

      StringBuilder stringBuilder = new StringBuilder();
      if (this.settings.getCacheRegionPrefix() != null) {
         stringBuilder.append(this.settings.getCacheRegionPrefix()).append('.');
      }

      String cacheRegionPrefix = stringBuilder.toString();
      this.entityPersisters = new HashMap();
      Map<String, RegionAccessStrategy> entityAccessStrategies = new HashMap();
      Map<String, ClassMetadata> classMeta = new HashMap();

      for(EntityBinding model : metadata.getEntityBindings()) {
         EntityBinding rootEntityBinding = metadata.getRootEntityBinding(model.getEntity().getName());
         EntityRegionAccessStrategy accessStrategy = null;
         if (this.settings.isSecondLevelCacheEnabled() && rootEntityBinding.getHierarchyDetails().getCaching() != null && model.getHierarchyDetails().getCaching() != null && model.getHierarchyDetails().getCaching().getAccessType() != null) {
            String cacheRegionName = cacheRegionPrefix + rootEntityBinding.getHierarchyDetails().getCaching().getRegion();
            accessStrategy = (EntityRegionAccessStrategy)EntityRegionAccessStrategy.class.cast(entityAccessStrategies.get(cacheRegionName));
            if (accessStrategy == null) {
               AccessType accessType = model.getHierarchyDetails().getCaching().getAccessType();
               if (LOG.isTraceEnabled()) {
                  LOG.tracev("Building cache for entity data [{0}]", model.getEntity().getName());
               }

               EntityRegion entityRegion = this.settings.getRegionFactory().buildEntityRegion(cacheRegionName, this.properties, CacheDataDescriptionImpl.decode(model));
               accessStrategy = entityRegion.buildAccessStrategy(accessType);
               entityAccessStrategies.put(cacheRegionName, accessStrategy);
               this.cacheAccess.addCacheRegion(cacheRegionName, entityRegion);
            }
         }

         EntityPersister cp = ((PersisterFactory)this.serviceRegistry.getService(PersisterFactory.class)).createEntityPersister(model, accessStrategy, this, metadata);
         this.entityPersisters.put(model.getEntity().getName(), cp);
         classMeta.put(model.getEntity().getName(), cp.getClassMetadata());
      }

      this.classMetadata = Collections.unmodifiableMap(classMeta);
      Map<String, Set<String>> tmpEntityToCollectionRoleMap = new HashMap();
      this.collectionPersisters = new HashMap();
      Map<String, CollectionMetadata> tmpCollectionMetadata = new HashMap();

      for(PluralAttributeBinding model : metadata.getCollectionBindings()) {
         if (model.getAttribute() == null) {
            throw new IllegalStateException("No attribute defined for a AbstractPluralAttributeBinding: " + model);
         }

         if (model.getAttribute().isSingular()) {
            throw new IllegalStateException("AbstractPluralAttributeBinding has a Singular attribute defined: " + model.getAttribute().getName());
         }

         String cacheRegionName = cacheRegionPrefix + model.getCaching().getRegion();
         AccessType accessType = model.getCaching().getAccessType();
         CollectionRegionAccessStrategy accessStrategy = null;
         if (accessType != null && this.settings.isSecondLevelCacheEnabled()) {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Building cache for collection data [{0}]", model.getAttribute().getRole());
            }

            CollectionRegion collectionRegion = this.settings.getRegionFactory().buildCollectionRegion(cacheRegionName, this.properties, CacheDataDescriptionImpl.decode(model));
            accessStrategy = collectionRegion.buildAccessStrategy(accessType);
            entityAccessStrategies.put(cacheRegionName, accessStrategy);
            this.cacheAccess.addCacheRegion(cacheRegionName, collectionRegion);
         }

         CollectionPersister persister = ((PersisterFactory)this.serviceRegistry.getService(PersisterFactory.class)).createCollectionPersister((MetadataImplementor)metadata, (PluralAttributeBinding)model, accessStrategy, this);
         this.collectionPersisters.put(model.getAttribute().getRole(), persister);
         tmpCollectionMetadata.put(model.getAttribute().getRole(), persister.getCollectionMetadata());
         Type indexType = persister.getIndexType();
         if (indexType != null && indexType.isAssociationType() && !indexType.isAnyType()) {
            String entityName = ((AssociationType)indexType).getAssociatedEntityName(this);
            Set<String> roles = (Set)tmpEntityToCollectionRoleMap.get(entityName);
            if (roles == null) {
               roles = new HashSet();
               tmpEntityToCollectionRoleMap.put(entityName, roles);
            }

            roles.add(persister.getRole());
         }

         Type elementType = persister.getElementType();
         if (elementType.isAssociationType() && !elementType.isAnyType()) {
            String entityName = ((AssociationType)elementType).getAssociatedEntityName(this);
            Set<String> roles = (Set)tmpEntityToCollectionRoleMap.get(entityName);
            if (roles == null) {
               roles = new HashSet();
               tmpEntityToCollectionRoleMap.put(entityName, roles);
            }

            roles.add(persister.getRole());
         }
      }

      this.collectionMetadata = Collections.unmodifiableMap(tmpCollectionMetadata);

      for(Map.Entry entry : tmpEntityToCollectionRoleMap.entrySet()) {
         entry.setValue(Collections.unmodifiableSet((Set)entry.getValue()));
      }

      this.collectionRolesByEntityParticipant = Collections.unmodifiableMap(tmpEntityToCollectionRoleMap);
      this.namedQueries = new HashMap();

      for(NamedQueryDefinition namedQueryDefinition : metadata.getNamedQueryDefinitions()) {
         this.namedQueries.put(namedQueryDefinition.getName(), namedQueryDefinition);
      }

      this.namedSqlQueries = new HashMap();

      for(NamedSQLQueryDefinition namedNativeQueryDefinition : metadata.getNamedNativeQueryDefinitions()) {
         this.namedSqlQueries.put(namedNativeQueryDefinition.getName(), namedNativeQueryDefinition);
      }

      this.sqlResultSetMappings = new HashMap();

      for(ResultSetMappingDefinition resultSetMappingDefinition : metadata.getResultSetMappingDefinitions()) {
         this.sqlResultSetMappings.put(resultSetMappingDefinition.getName(), resultSetMappingDefinition);
      }

      this.imports = new HashMap();

      for(Map.Entry importEntry : metadata.getImports()) {
         this.imports.put(importEntry.getKey(), importEntry.getValue());
      }

      for(EntityPersister persister : this.entityPersisters.values()) {
         persister.postInstantiate();
         this.registerEntityNameResolvers(persister);
      }

      for(CollectionPersister persister : this.collectionPersisters.values()) {
         persister.postInstantiate();
      }

      this.name = this.settings.getSessionFactoryName();

      try {
         this.uuid = (String)UUID_GENERATOR.generate((SessionImplementor)null, (Object)null);
      } catch (Exception var21) {
         throw new AssertionFailure("Could not generate UUID");
      }

      SessionFactoryRegistry.INSTANCE.addSessionFactory(this.uuid, this.name, this.settings.isSessionFactoryNameAlsoJndiName(), this, (JndiService)this.serviceRegistry.getService(JndiService.class));
      LOG.debug("Instantiated session factory");
      if (this.settings.isAutoCreateSchema()) {
         (new SchemaExport(metadata)).setImportSqlCommandExtractor((ImportSqlCommandExtractor)this.serviceRegistry.getService(ImportSqlCommandExtractor.class)).create(false, true);
      }

      if (this.settings.isAutoDropSchema()) {
         this.schemaExport = (new SchemaExport(metadata)).setImportSqlCommandExtractor((ImportSqlCommandExtractor)this.serviceRegistry.getService(ImportSqlCommandExtractor.class));
      }

      this.currentSessionContext = this.buildCurrentSessionContext();
      if (this.settings.isNamedQueryStartupCheckingEnabled()) {
         Map<String, HibernateException> errors = this.checkNamedQueries();
         if (!errors.isEmpty()) {
            StringBuilder failingQueries = new StringBuilder("Errors in named queries: ");
            String sep = "";

            for(Map.Entry entry : errors.entrySet()) {
               LOG.namedQueryError((String)entry.getKey(), (HibernateException)entry.getValue());
               failingQueries.append((String)entry.getKey()).append(sep);
               sep = ", ";
            }

            throw new HibernateException(failingQueries.toString());
         }
      }

      this.fetchProfiles = new HashMap();

      for(org.hibernate.metamodel.binding.FetchProfile mappingProfile : metadata.getFetchProfiles()) {
         org.hibernate.engine.profile.FetchProfile fetchProfile = new org.hibernate.engine.profile.FetchProfile(mappingProfile.getName());

         for(org.hibernate.metamodel.binding.FetchProfile.Fetch mappingFetch : mappingProfile.getFetches()) {
            String entityName = this.getImportedClassName(mappingFetch.getEntity());
            EntityPersister owner = entityName == null ? null : (EntityPersister)this.entityPersisters.get(entityName);
            if (owner == null) {
               throw new HibernateException("Unable to resolve entity reference [" + mappingFetch.getEntity() + "] in fetch profile [" + fetchProfile.getName() + "]");
            }

            Type associationType = owner.getPropertyType(mappingFetch.getAssociation());
            if (associationType == null || !associationType.isAssociationType()) {
               throw new HibernateException("Fetch profile [" + fetchProfile.getName() + "] specified an invalid association");
            }

            Fetch.Style fetchStyle = Fetch.Style.parse(mappingFetch.getStyle());
            fetchProfile.addFetch(new Association(owner, mappingFetch.getAssociation()), fetchStyle);
            ((Loadable)owner).registerAffectingFetchProfile(fetchProfile.getName());
         }

         this.fetchProfiles.put(fetchProfile.getName(), fetchProfile);
      }

      this.customEntityDirtinessStrategy = this.determineCustomEntityDirtinessStrategy();
      this.currentTenantIdentifierResolver = this.determineCurrentTenantIdentifierResolver((CurrentTenantIdentifierResolver)null);
      this.transactionEnvironment = new TransactionEnvironmentImpl(this);
      this.observer.sessionFactoryCreated(this);
   }

   private static Properties createPropertiesFromMap(Map map) {
      Properties properties = new Properties();
      properties.putAll(map);
      return properties;
   }

   public Session openSession() throws HibernateException {
      return this.withOptions().openSession();
   }

   public Session openTemporarySession() throws HibernateException {
      return this.withOptions().autoClose(false).flushBeforeCompletion(false).connectionReleaseMode(ConnectionReleaseMode.AFTER_STATEMENT).openSession();
   }

   public Session getCurrentSession() throws HibernateException {
      if (this.currentSessionContext == null) {
         throw new HibernateException("No CurrentSessionContext configured!");
      } else {
         return this.currentSessionContext.currentSession();
      }
   }

   public SessionBuilderImplementor withOptions() {
      return new SessionBuilderImpl(this);
   }

   public StatelessSessionBuilder withStatelessOptions() {
      return new StatelessSessionBuilderImpl(this);
   }

   public StatelessSession openStatelessSession() {
      return this.withStatelessOptions().openStatelessSession();
   }

   public StatelessSession openStatelessSession(Connection connection) {
      return this.withStatelessOptions().connection(connection).openStatelessSession();
   }

   public void addObserver(SessionFactoryObserver observer) {
      this.observer.addObserver(observer);
   }

   public TransactionEnvironment getTransactionEnvironment() {
      return this.transactionEnvironment;
   }

   public Properties getProperties() {
      return this.properties;
   }

   public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
      return null;
   }

   public TypeResolver getTypeResolver() {
      return this.typeResolver;
   }

   private void registerEntityNameResolvers(EntityPersister persister) {
      if (persister.getEntityMetamodel() != null && persister.getEntityMetamodel().getTuplizer() != null) {
         this.registerEntityNameResolvers(persister.getEntityMetamodel().getTuplizer());
      }
   }

   private void registerEntityNameResolvers(EntityTuplizer tuplizer) {
      EntityNameResolver[] resolvers = tuplizer.getEntityNameResolvers();
      if (resolvers != null) {
         for(EntityNameResolver resolver : resolvers) {
            this.registerEntityNameResolver(resolver);
         }

      }
   }

   public void registerEntityNameResolver(EntityNameResolver resolver) {
      this.entityNameResolvers.put(resolver, ENTITY_NAME_RESOLVER_MAP_VALUE);
   }

   public Iterable iterateEntityNameResolvers() {
      return this.entityNameResolvers.keySet();
   }

   public QueryPlanCache getQueryPlanCache() {
      return this.queryPlanCache;
   }

   private Map checkNamedQueries() throws HibernateException {
      Map<String, HibernateException> errors = new HashMap();
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Checking %s named HQL queries", this.namedQueries.size());
      }

      for(Map.Entry entry : this.namedQueries.entrySet()) {
         String queryName = (String)entry.getKey();
         NamedQueryDefinition qd = (NamedQueryDefinition)entry.getValue();

         try {
            LOG.debugf("Checking named query: %s", queryName);
            this.queryPlanCache.getHQLQueryPlan(qd.getQueryString(), false, Collections.EMPTY_MAP);
         } catch (QueryException e) {
            errors.put(queryName, e);
         } catch (MappingException e) {
            errors.put(queryName, e);
         }
      }

      if (LOG.isDebugEnabled()) {
         LOG.debugf("Checking %s named SQL queries", this.namedSqlQueries.size());
      }

      for(Map.Entry entry : this.namedSqlQueries.entrySet()) {
         String queryName = (String)entry.getKey();
         NamedSQLQueryDefinition qd = (NamedSQLQueryDefinition)entry.getValue();

         try {
            LOG.debugf("Checking named SQL query: %s", queryName);
            NativeSQLQuerySpecification spec;
            if (qd.getResultSetRef() != null) {
               ResultSetMappingDefinition definition = (ResultSetMappingDefinition)this.sqlResultSetMappings.get(qd.getResultSetRef());
               if (definition == null) {
                  throw new MappingException("Unable to find resultset-ref definition: " + qd.getResultSetRef());
               }

               spec = new NativeSQLQuerySpecification(qd.getQueryString(), definition.getQueryReturns(), qd.getQuerySpaces());
            } else {
               spec = new NativeSQLQuerySpecification(qd.getQueryString(), qd.getQueryReturns(), qd.getQuerySpaces());
            }

            this.queryPlanCache.getNativeSQLQueryPlan(spec);
         } catch (QueryException e) {
            errors.put(queryName, e);
         } catch (MappingException e) {
            errors.put(queryName, e);
         }
      }

      return errors;
   }

   public EntityPersister getEntityPersister(String entityName) throws MappingException {
      EntityPersister result = (EntityPersister)this.entityPersisters.get(entityName);
      if (result == null) {
         throw new MappingException("Unknown entity: " + entityName);
      } else {
         return result;
      }
   }

   public Map getCollectionPersisters() {
      return this.collectionPersisters;
   }

   public Map getEntityPersisters() {
      return this.entityPersisters;
   }

   public CollectionPersister getCollectionPersister(String role) throws MappingException {
      CollectionPersister result = (CollectionPersister)this.collectionPersisters.get(role);
      if (result == null) {
         throw new MappingException("Unknown collection role: " + role);
      } else {
         return result;
      }
   }

   public Settings getSettings() {
      return this.settings;
   }

   public SessionFactory.SessionFactoryOptions getSessionFactoryOptions() {
      return this.sessionFactoryOptions;
   }

   public JdbcServices getJdbcServices() {
      return this.jdbcServices;
   }

   public Dialect getDialect() {
      if (this.serviceRegistry == null) {
         throw new IllegalStateException("Cannot determine dialect because serviceRegistry is null.");
      } else {
         return this.dialect;
      }
   }

   public Interceptor getInterceptor() {
      return this.sessionFactoryOptions.getInterceptor();
   }

   public SQLExceptionConverter getSQLExceptionConverter() {
      return this.getSQLExceptionHelper().getSqlExceptionConverter();
   }

   public SqlExceptionHelper getSQLExceptionHelper() {
      return this.getJdbcServices().getSqlExceptionHelper();
   }

   public Set getCollectionRolesByEntityParticipant(String entityName) {
      return (Set)this.collectionRolesByEntityParticipant.get(entityName);
   }

   public Reference getReference() {
      LOG.debug("Returning a Reference to the SessionFactory");
      return new Reference(SessionFactoryImpl.class.getName(), new StringRefAddr("uuid", this.uuid), SessionFactoryRegistry.ObjectFactoryImpl.class.getName(), (String)null);
   }

   public NamedQueryDefinition getNamedQuery(String queryName) {
      return (NamedQueryDefinition)this.namedQueries.get(queryName);
   }

   public NamedSQLQueryDefinition getNamedSQLQuery(String queryName) {
      return (NamedSQLQueryDefinition)this.namedSqlQueries.get(queryName);
   }

   public ResultSetMappingDefinition getResultSetMapping(String resultSetName) {
      return (ResultSetMappingDefinition)this.sqlResultSetMappings.get(resultSetName);
   }

   public Type getIdentifierType(String className) throws MappingException {
      return this.getEntityPersister(className).getIdentifierType();
   }

   public String getIdentifierPropertyName(String className) throws MappingException {
      return this.getEntityPersister(className).getIdentifierPropertyName();
   }

   public Type[] getReturnTypes(String queryString) throws HibernateException {
      return this.queryPlanCache.getHQLQueryPlan(queryString, false, Collections.EMPTY_MAP).getReturnMetadata().getReturnTypes();
   }

   public String[] getReturnAliases(String queryString) throws HibernateException {
      return this.queryPlanCache.getHQLQueryPlan(queryString, false, Collections.EMPTY_MAP).getReturnMetadata().getReturnAliases();
   }

   public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
      return this.getClassMetadata(persistentClass.getName());
   }

   public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
      return (CollectionMetadata)this.collectionMetadata.get(roleName);
   }

   public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
      return (ClassMetadata)this.classMetadata.get(entityName);
   }

   public String[] getImplementors(String className) throws MappingException {
      Class clazz;
      try {
         clazz = ((ClassLoaderService)this.serviceRegistry.getService(ClassLoaderService.class)).classForName(className);
      } catch (ClassLoadingException var12) {
         return new String[]{className};
      }

      ArrayList<String> results = new ArrayList();

      for(EntityPersister checkPersister : this.entityPersisters.values()) {
         if (Queryable.class.isInstance(checkPersister)) {
            Queryable checkQueryable = (Queryable)Queryable.class.cast(checkPersister);
            String checkQueryableEntityName = checkQueryable.getEntityName();
            boolean isMappedClass = className.equals(checkQueryableEntityName);
            if (checkQueryable.isExplicitPolymorphism()) {
               if (isMappedClass) {
                  return new String[]{className};
               }
            } else if (isMappedClass) {
               results.add(checkQueryableEntityName);
            } else {
               Class mappedClass = checkQueryable.getMappedClass();
               if (mappedClass != null && clazz.isAssignableFrom(mappedClass)) {
                  boolean assignableSuperclass;
                  if (checkQueryable.isInherited()) {
                     Class mappedSuperclass = this.getEntityPersister(checkQueryable.getMappedSuperclass()).getMappedClass();
                     assignableSuperclass = clazz.isAssignableFrom(mappedSuperclass);
                  } else {
                     assignableSuperclass = false;
                  }

                  if (!assignableSuperclass) {
                     results.add(checkQueryableEntityName);
                  }
               }
            }
         }
      }

      return (String[])results.toArray(new String[results.size()]);
   }

   public String getImportedClassName(String className) {
      String result = (String)this.imports.get(className);
      if (result == null) {
         try {
            ((ClassLoaderService)this.serviceRegistry.getService(ClassLoaderService.class)).classForName(className);
            return className;
         } catch (ClassLoadingException var4) {
            return null;
         }
      } else {
         return result;
      }
   }

   public Map getAllClassMetadata() throws HibernateException {
      return this.classMetadata;
   }

   public Map getAllCollectionMetadata() throws HibernateException {
      return this.collectionMetadata;
   }

   public Type getReferencedPropertyType(String className, String propertyName) throws MappingException {
      return this.getEntityPersister(className).getPropertyType(propertyName);
   }

   public ConnectionProvider getConnectionProvider() {
      return this.jdbcServices.getConnectionProvider();
   }

   public void close() throws HibernateException {
      if (this.isClosed) {
         LOG.trace("Already closed");
      } else {
         LOG.closing();
         this.isClosed = true;
         this.settings.getMultiTableBulkIdStrategy().release(this.jdbcServices, this.buildLocalConnectionAccess());

         for(EntityPersister p : this.entityPersisters.values()) {
            if (p.hasCache()) {
               p.getCacheAccessStrategy().getRegion().destroy();
            }
         }

         for(CollectionPersister p : this.collectionPersisters.values()) {
            if (p.hasCache()) {
               p.getCacheAccessStrategy().getRegion().destroy();
            }
         }

         this.cacheAccess.close();
         this.queryPlanCache.cleanup();
         if (this.settings.isAutoDropSchema()) {
            this.schemaExport.drop(false, true);
         }

         SessionFactoryRegistry.INSTANCE.removeSessionFactory(this.uuid, this.name, this.settings.isSessionFactoryNameAlsoJndiName(), (JndiService)this.serviceRegistry.getService(JndiService.class));
         this.observer.sessionFactoryClosed(this);
         this.serviceRegistry.destroy();
      }
   }

   public Cache getCache() {
      return this.cacheAccess;
   }

   public void evictEntity(String entityName, Serializable id) throws HibernateException {
      this.getCache().evictEntity(entityName, id);
   }

   public void evictEntity(String entityName) throws HibernateException {
      this.getCache().evictEntityRegion(entityName);
   }

   public void evict(Class persistentClass, Serializable id) throws HibernateException {
      this.getCache().evictEntity(persistentClass, id);
   }

   public void evict(Class persistentClass) throws HibernateException {
      this.getCache().evictEntityRegion(persistentClass);
   }

   public void evictCollection(String roleName, Serializable id) throws HibernateException {
      this.getCache().evictCollection(roleName, id);
   }

   public void evictCollection(String roleName) throws HibernateException {
      this.getCache().evictCollectionRegion(roleName);
   }

   public void evictQueries() throws HibernateException {
      this.cacheAccess.evictQueries();
   }

   public void evictQueries(String regionName) throws HibernateException {
      this.getCache().evictQueryRegion(regionName);
   }

   public UpdateTimestampsCache getUpdateTimestampsCache() {
      return this.cacheAccess.getUpdateTimestampsCache();
   }

   public QueryCache getQueryCache() {
      return this.cacheAccess.getQueryCache();
   }

   public QueryCache getQueryCache(String regionName) throws HibernateException {
      return this.cacheAccess.getQueryCache(regionName);
   }

   public Region getSecondLevelCacheRegion(String regionName) {
      return this.cacheAccess.getSecondLevelCacheRegion(regionName);
   }

   public Region getNaturalIdCacheRegion(String regionName) {
      return this.cacheAccess.getNaturalIdCacheRegion(regionName);
   }

   public Map getAllSecondLevelCacheRegions() {
      return this.cacheAccess.getAllSecondLevelCacheRegions();
   }

   public boolean isClosed() {
      return this.isClosed;
   }

   public Statistics getStatistics() {
      return this.getStatisticsImplementor();
   }

   public StatisticsImplementor getStatisticsImplementor() {
      return (StatisticsImplementor)this.serviceRegistry.getService(StatisticsImplementor.class);
   }

   public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
      FilterDefinition def = (FilterDefinition)this.filters.get(filterName);
      if (def == null) {
         throw new HibernateException("No such filter configured [" + filterName + "]");
      } else {
         return def;
      }
   }

   public boolean containsFetchProfileDefinition(String name) {
      return this.fetchProfiles.containsKey(name);
   }

   public Set getDefinedFilterNames() {
      return this.filters.keySet();
   }

   public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
      return (IdentifierGenerator)this.identifierGenerators.get(rootEntityName);
   }

   private TransactionFactory transactionFactory() {
      return (TransactionFactory)this.serviceRegistry.getService(TransactionFactory.class);
   }

   private boolean canAccessTransactionManager() {
      try {
         return ((JtaPlatform)this.serviceRegistry.getService(JtaPlatform.class)).retrieveTransactionManager() != null;
      } catch (Exception var2) {
         return false;
      }
   }

   private CurrentSessionContext buildCurrentSessionContext() {
      String impl = this.properties.getProperty("hibernate.current_session_context_class");
      if (impl == null) {
         if (!this.canAccessTransactionManager()) {
            return null;
         }

         impl = "jta";
      }

      if ("jta".equals(impl)) {
         if (!this.transactionFactory().compatibleWithJtaSynchronization()) {
            LOG.autoFlushWillNotWork();
         }

         return new JTASessionContext(this);
      } else if ("thread".equals(impl)) {
         return new ThreadLocalSessionContext(this);
      } else if ("managed".equals(impl)) {
         return new ManagedSessionContext(this);
      } else {
         try {
            Class implClass = ((ClassLoaderService)this.serviceRegistry.getService(ClassLoaderService.class)).classForName(impl);
            return (CurrentSessionContext)implClass.getConstructor(SessionFactoryImplementor.class).newInstance(this);
         } catch (Throwable t) {
            LOG.unableToConstructCurrentSessionContext(impl, t);
            return null;
         }
      }
   }

   public ServiceRegistryImplementor getServiceRegistry() {
      return this.serviceRegistry;
   }

   public EntityNotFoundDelegate getEntityNotFoundDelegate() {
      return this.sessionFactoryOptions.getEntityNotFoundDelegate();
   }

   public SQLFunctionRegistry getSqlFunctionRegistry() {
      return this.sqlFunctionRegistry;
   }

   public org.hibernate.engine.profile.FetchProfile getFetchProfile(String name) {
      return (org.hibernate.engine.profile.FetchProfile)this.fetchProfiles.get(name);
   }

   public TypeHelper getTypeHelper() {
      return this.typeHelper;
   }

   public CustomEntityDirtinessStrategy getCustomEntityDirtinessStrategy() {
      return this.customEntityDirtinessStrategy;
   }

   public CurrentTenantIdentifierResolver getCurrentTenantIdentifierResolver() {
      return this.currentTenantIdentifierResolver;
   }

   private void writeObject(ObjectOutputStream out) throws IOException {
      LOG.debugf("Serializing: %s", this.uuid);
      out.defaultWriteObject();
      LOG.trace("Serialized");
   }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
      LOG.trace("Deserializing");
      in.defaultReadObject();
      LOG.debugf("Deserialized: %s", this.uuid);
   }

   private Object readResolve() throws InvalidObjectException {
      LOG.trace("Resolving serialized SessionFactory");
      return locateSessionFactoryOnDeserialization(this.uuid, this.name);
   }

   private static SessionFactory locateSessionFactoryOnDeserialization(String uuid, String name) throws InvalidObjectException {
      SessionFactory uuidResult = SessionFactoryRegistry.INSTANCE.getSessionFactory(uuid);
      if (uuidResult != null) {
         LOG.debugf("Resolved SessionFactory by UUID [%s]", uuid);
         return uuidResult;
      } else {
         if (name != null) {
            SessionFactory namedResult = SessionFactoryRegistry.INSTANCE.getNamedSessionFactory(name);
            if (namedResult != null) {
               LOG.debugf("Resolved SessionFactory by name [%s]", name);
               return namedResult;
            }
         }

         throw new InvalidObjectException("Could not find a SessionFactory [uuid=" + uuid + ",name=" + name + "]");
      }
   }

   void serialize(ObjectOutputStream oos) throws IOException {
      oos.writeUTF(this.uuid);
      oos.writeBoolean(this.name != null);
      if (this.name != null) {
         oos.writeUTF(this.name);
      }

   }

   static SessionFactoryImpl deserialize(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      LOG.trace("Deserializing SessionFactory from Session");
      String uuid = ois.readUTF();
      boolean isNamed = ois.readBoolean();
      String name = isNamed ? ois.readUTF() : null;
      return (SessionFactoryImpl)locateSessionFactoryOnDeserialization(uuid, name);
   }

   static class SessionBuilderImpl implements SessionBuilderImplementor {
      private final SessionFactoryImpl sessionFactory;
      private SessionOwner sessionOwner;
      private Interceptor interceptor;
      private Connection connection;
      private ConnectionReleaseMode connectionReleaseMode;
      private boolean autoClose;
      private boolean autoJoinTransactions = true;
      private boolean flushBeforeCompletion;
      private String tenantIdentifier;

      SessionBuilderImpl(SessionFactoryImpl sessionFactory) {
         this.sessionFactory = sessionFactory;
         this.sessionOwner = null;
         Settings settings = sessionFactory.settings;
         this.interceptor = sessionFactory.getInterceptor();
         this.connectionReleaseMode = settings.getConnectionReleaseMode();
         this.autoClose = settings.isAutoCloseSessionEnabled();
         this.flushBeforeCompletion = settings.isFlushBeforeCompletionEnabled();
         if (sessionFactory.getCurrentTenantIdentifierResolver() != null) {
            this.tenantIdentifier = sessionFactory.getCurrentTenantIdentifierResolver().resolveCurrentTenantIdentifier();
         }

      }

      protected TransactionCoordinatorImpl getTransactionCoordinator() {
         return null;
      }

      public Session openSession() {
         return new SessionImpl(this.connection, this.sessionFactory, this.sessionOwner, this.getTransactionCoordinator(), this.autoJoinTransactions, this.sessionFactory.settings.getRegionFactory().nextTimestamp(), this.interceptor, this.flushBeforeCompletion, this.autoClose, this.connectionReleaseMode, this.tenantIdentifier);
      }

      public SessionBuilder owner(SessionOwner sessionOwner) {
         this.sessionOwner = sessionOwner;
         return this;
      }

      public SessionBuilder interceptor(Interceptor interceptor) {
         this.interceptor = interceptor;
         return this;
      }

      public SessionBuilder noInterceptor() {
         this.interceptor = EmptyInterceptor.INSTANCE;
         return this;
      }

      public SessionBuilder connection(Connection connection) {
         this.connection = connection;
         return this;
      }

      public SessionBuilder connectionReleaseMode(ConnectionReleaseMode connectionReleaseMode) {
         this.connectionReleaseMode = connectionReleaseMode;
         return this;
      }

      public SessionBuilder autoJoinTransactions(boolean autoJoinTransactions) {
         this.autoJoinTransactions = autoJoinTransactions;
         return this;
      }

      public SessionBuilder autoClose(boolean autoClose) {
         this.autoClose = autoClose;
         return this;
      }

      public SessionBuilder flushBeforeCompletion(boolean flushBeforeCompletion) {
         this.flushBeforeCompletion = flushBeforeCompletion;
         return this;
      }

      public SessionBuilder tenantIdentifier(String tenantIdentifier) {
         this.tenantIdentifier = tenantIdentifier;
         return this;
      }
   }

   public static class StatelessSessionBuilderImpl implements StatelessSessionBuilder {
      private final SessionFactoryImpl sessionFactory;
      private Connection connection;
      private String tenantIdentifier;

      public StatelessSessionBuilderImpl(SessionFactoryImpl sessionFactory) {
         this.sessionFactory = sessionFactory;
      }

      public StatelessSession openStatelessSession() {
         return new StatelessSessionImpl(this.connection, this.tenantIdentifier, this.sessionFactory);
      }

      public StatelessSessionBuilder connection(Connection connection) {
         this.connection = connection;
         return this;
      }

      public StatelessSessionBuilder tenantIdentifier(String tenantIdentifier) {
         this.tenantIdentifier = tenantIdentifier;
         return this;
      }
   }
}
