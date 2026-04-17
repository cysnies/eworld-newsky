package org.hibernate.metamodel.source.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.DuplicateMappingException;
import org.hibernate.MappingException;
import org.hibernate.SessionFactory;
import org.hibernate.cache.spi.RegionFactory;
import org.hibernate.cache.spi.access.AccessType;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.NamedSQLQueryDefinition;
import org.hibernate.id.factory.IdentifierGeneratorFactory;
import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.metamodel.Metadata;
import org.hibernate.metamodel.MetadataSourceProcessingOrder;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.metamodel.SessionFactoryBuilder;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.FetchProfile;
import org.hibernate.metamodel.binding.IdGenerator;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.metamodel.binding.TypeDef;
import org.hibernate.metamodel.domain.BasicType;
import org.hibernate.metamodel.domain.Type;
import org.hibernate.metamodel.relational.Database;
import org.hibernate.metamodel.source.MappingDefaults;
import org.hibernate.metamodel.source.MetaAttributeContext;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.MetadataSourceProcessor;
import org.hibernate.metamodel.source.annotations.AnnotationMetadataSourceProcessorImpl;
import org.hibernate.metamodel.source.hbm.HbmMetadataSourceProcessorImpl;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.type.TypeResolver;
import org.jboss.logging.Logger;

public class MetadataImpl implements MetadataImplementor, Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, MetadataImpl.class.getName());
   private final ServiceRegistry serviceRegistry;
   private final Metadata.Options options;
   private final ValueHolder classLoaderService;
   private final ValueHolder persisterClassResolverService;
   private TypeResolver typeResolver = new TypeResolver();
   private SessionFactoryBuilder sessionFactoryBuilder = new SessionFactoryBuilderImpl(this);
   private final MutableIdentifierGeneratorFactory identifierGeneratorFactory;
   private final Database database;
   private final MappingDefaults mappingDefaults;
   private Map entityBindingMap = new HashMap();
   private Map collectionBindingMap = new HashMap();
   private Map fetchProfiles = new HashMap();
   private Map imports = new HashMap();
   private Map typeDefs = new HashMap();
   private Map idGenerators = new HashMap();
   private Map namedQueryDefs = new HashMap();
   private Map namedNativeQueryDefs = new HashMap();
   private Map resultSetMappings = new HashMap();
   private Map filterDefs = new HashMap();
   private boolean globallyQuotedIdentifiers = false;
   private final MetaAttributeContext globalMetaAttributeContext = new MetaAttributeContext();
   private static final String DEFAULT_IDENTIFIER_COLUMN_NAME = "id";
   private static final String DEFAULT_DISCRIMINATOR_COLUMN_NAME = "class";
   private static final String DEFAULT_CASCADE = "none";
   private static final String DEFAULT_PROPERTY_ACCESS = "property";

   public MetadataImpl(MetadataSources metadataSources, Metadata.Options options) {
      this.serviceRegistry = metadataSources.getServiceRegistry();
      this.options = options;
      this.identifierGeneratorFactory = (MutableIdentifierGeneratorFactory)this.serviceRegistry.getService(MutableIdentifierGeneratorFactory.class);
      this.database = new Database(options);
      this.mappingDefaults = new MappingDefaultsImpl();
      MetadataSourceProcessor[] metadataSourceProcessors;
      if (options.getMetadataSourceProcessingOrder() == MetadataSourceProcessingOrder.HBM_FIRST) {
         metadataSourceProcessors = new MetadataSourceProcessor[]{new HbmMetadataSourceProcessorImpl(this), new AnnotationMetadataSourceProcessorImpl(this)};
      } else {
         metadataSourceProcessors = new MetadataSourceProcessor[]{new AnnotationMetadataSourceProcessorImpl(this), new HbmMetadataSourceProcessorImpl(this)};
      }

      this.classLoaderService = new ValueHolder(new ValueHolder.DeferredInitializer() {
         public ClassLoaderService initialize() {
            return (ClassLoaderService)MetadataImpl.this.serviceRegistry.getService(ClassLoaderService.class);
         }
      });
      this.persisterClassResolverService = new ValueHolder(new ValueHolder.DeferredInitializer() {
         public PersisterClassResolver initialize() {
            return (PersisterClassResolver)MetadataImpl.this.serviceRegistry.getService(PersisterClassResolver.class);
         }
      });
      ArrayList<String> processedEntityNames = new ArrayList();
      this.prepare(metadataSourceProcessors, metadataSources);
      this.bindIndependentMetadata(metadataSourceProcessors, metadataSources);
      this.bindTypeDependentMetadata(metadataSourceProcessors, metadataSources);
      this.bindMappingMetadata(metadataSourceProcessors, metadataSources, processedEntityNames);
      this.bindMappingDependentMetadata(metadataSourceProcessors, metadataSources);
      (new AssociationResolver(this)).resolve();
      (new HibernateTypeResolver(this)).resolve();
      (new IdentifierGeneratorResolver(this)).resolve();
   }

   private void prepare(MetadataSourceProcessor[] metadataSourceProcessors, MetadataSources metadataSources) {
      for(MetadataSourceProcessor metadataSourceProcessor : metadataSourceProcessors) {
         metadataSourceProcessor.prepare(metadataSources);
      }

   }

   private void bindIndependentMetadata(MetadataSourceProcessor[] metadataSourceProcessors, MetadataSources metadataSources) {
      for(MetadataSourceProcessor metadataSourceProcessor : metadataSourceProcessors) {
         metadataSourceProcessor.processIndependentMetadata(metadataSources);
      }

   }

   private void bindTypeDependentMetadata(MetadataSourceProcessor[] metadataSourceProcessors, MetadataSources metadataSources) {
      for(MetadataSourceProcessor metadataSourceProcessor : metadataSourceProcessors) {
         metadataSourceProcessor.processTypeDependentMetadata(metadataSources);
      }

   }

   private void bindMappingMetadata(MetadataSourceProcessor[] metadataSourceProcessors, MetadataSources metadataSources, List processedEntityNames) {
      for(MetadataSourceProcessor metadataSourceProcessor : metadataSourceProcessors) {
         metadataSourceProcessor.processMappingMetadata(metadataSources, processedEntityNames);
      }

   }

   private void bindMappingDependentMetadata(MetadataSourceProcessor[] metadataSourceProcessors, MetadataSources metadataSources) {
      for(MetadataSourceProcessor metadataSourceProcessor : metadataSourceProcessors) {
         metadataSourceProcessor.processMappingDependentMetadata(metadataSources);
      }

   }

   public void addFetchProfile(FetchProfile profile) {
      if (profile != null && profile.getName() != null) {
         this.fetchProfiles.put(profile.getName(), profile);
      } else {
         throw new IllegalArgumentException("Fetch profile object or name is null: " + profile);
      }
   }

   public void addFilterDefinition(FilterDefinition def) {
      if (def != null && def.getFilterName() != null) {
         this.filterDefs.put(def.getFilterName(), def);
      } else {
         throw new IllegalArgumentException("Filter definition object or name is null: " + def);
      }
   }

   public Iterable getFilterDefinitions() {
      return this.filterDefs.values();
   }

   public void addIdGenerator(IdGenerator generator) {
      if (generator != null && generator.getName() != null) {
         this.idGenerators.put(generator.getName(), generator);
      } else {
         throw new IllegalArgumentException("ID generator object or name is null.");
      }
   }

   public IdGenerator getIdGenerator(String name) {
      if (name == null) {
         throw new IllegalArgumentException("null is not a valid generator name");
      } else {
         return (IdGenerator)this.idGenerators.get(name);
      }
   }

   public void registerIdentifierGenerator(String name, String generatorClassName) {
      this.identifierGeneratorFactory.register(name, this.classLoaderService().classForName(generatorClassName));
   }

   public void addNamedNativeQuery(NamedSQLQueryDefinition def) {
      if (def != null && def.getName() != null) {
         this.namedNativeQueryDefs.put(def.getName(), def);
      } else {
         throw new IllegalArgumentException("Named native query definition object or name is null: " + def.getQueryString());
      }
   }

   public NamedSQLQueryDefinition getNamedNativeQuery(String name) {
      if (name == null) {
         throw new IllegalArgumentException("null is not a valid native query name");
      } else {
         return (NamedSQLQueryDefinition)this.namedNativeQueryDefs.get(name);
      }
   }

   public Iterable getNamedNativeQueryDefinitions() {
      return this.namedNativeQueryDefs.values();
   }

   public void addNamedQuery(NamedQueryDefinition def) {
      if (def == null) {
         throw new IllegalArgumentException("Named query definition is null");
      } else if (def.getName() == null) {
         throw new IllegalArgumentException("Named query definition name is null: " + def.getQueryString());
      } else {
         this.namedQueryDefs.put(def.getName(), def);
      }
   }

   public NamedQueryDefinition getNamedQuery(String name) {
      if (name == null) {
         throw new IllegalArgumentException("null is not a valid query name");
      } else {
         return (NamedQueryDefinition)this.namedQueryDefs.get(name);
      }
   }

   public Iterable getNamedQueryDefinitions() {
      return this.namedQueryDefs.values();
   }

   public void addResultSetMapping(ResultSetMappingDefinition resultSetMappingDefinition) {
      if (resultSetMappingDefinition != null && resultSetMappingDefinition.getName() != null) {
         this.resultSetMappings.put(resultSetMappingDefinition.getName(), resultSetMappingDefinition);
      } else {
         throw new IllegalArgumentException("Result-set mapping object or name is null: " + resultSetMappingDefinition);
      }
   }

   public Iterable getResultSetMappingDefinitions() {
      return this.resultSetMappings.values();
   }

   public void addTypeDefinition(TypeDef typeDef) {
      if (typeDef == null) {
         throw new IllegalArgumentException("Type definition is null");
      } else if (typeDef.getName() == null) {
         throw new IllegalArgumentException("Type definition name is null: " + typeDef.getTypeClass());
      } else {
         TypeDef previous = (TypeDef)this.typeDefs.put(typeDef.getName(), typeDef);
         if (previous != null) {
            LOG.debugf("Duplicate typedef name [%s] now -> %s", typeDef.getName(), typeDef.getTypeClass());
         }

      }
   }

   public Iterable getTypeDefinitions() {
      return this.typeDefs.values();
   }

   public TypeDef getTypeDefinition(String name) {
      return (TypeDef)this.typeDefs.get(name);
   }

   private ClassLoaderService classLoaderService() {
      return (ClassLoaderService)this.classLoaderService.getValue();
   }

   private PersisterClassResolver persisterClassResolverService() {
      return (PersisterClassResolver)this.persisterClassResolverService.getValue();
   }

   public Metadata.Options getOptions() {
      return this.options;
   }

   public SessionFactory buildSessionFactory() {
      return this.sessionFactoryBuilder.buildSessionFactory();
   }

   public ServiceRegistry getServiceRegistry() {
      return this.serviceRegistry;
   }

   public Class locateClassByName(String name) {
      return this.classLoaderService().classForName(name);
   }

   public Type makeJavaType(String className) {
      return new BasicType(className, this.makeClassReference(className));
   }

   public ValueHolder makeClassReference(final String className) {
      return new ValueHolder(new ValueHolder.DeferredInitializer() {
         public Class initialize() {
            return ((ClassLoaderService)MetadataImpl.this.classLoaderService.getValue()).classForName(className);
         }
      });
   }

   public String qualifyClassName(String name) {
      return name;
   }

   public Database getDatabase() {
      return this.database;
   }

   public EntityBinding getEntityBinding(String entityName) {
      return (EntityBinding)this.entityBindingMap.get(entityName);
   }

   public EntityBinding getRootEntityBinding(String entityName) {
      EntityBinding binding = (EntityBinding)this.entityBindingMap.get(entityName);
      if (binding == null) {
         throw new IllegalStateException("Unknown entity binding: " + entityName);
      } else {
         while(!binding.isRoot()) {
            binding = binding.getSuperEntityBinding();
            if (binding == null) {
               throw new AssertionFailure("Entity binding has no root: " + entityName);
            }
         }

         return binding;
      }
   }

   public Iterable getEntityBindings() {
      return this.entityBindingMap.values();
   }

   public void addEntity(EntityBinding entityBinding) {
      String entityName = entityBinding.getEntity().getName();
      if (this.entityBindingMap.containsKey(entityName)) {
         throw new DuplicateMappingException(DuplicateMappingException.Type.ENTITY, entityName);
      } else {
         this.entityBindingMap.put(entityName, entityBinding);
      }
   }

   public PluralAttributeBinding getCollection(String collectionRole) {
      return (PluralAttributeBinding)this.collectionBindingMap.get(collectionRole);
   }

   public Iterable getCollectionBindings() {
      return this.collectionBindingMap.values();
   }

   public void addCollection(PluralAttributeBinding pluralAttributeBinding) {
      String owningEntityName = pluralAttributeBinding.getContainer().getPathBase();
      String attributeName = pluralAttributeBinding.getAttribute().getName();
      String collectionRole = owningEntityName + '.' + attributeName;
      if (this.collectionBindingMap.containsKey(collectionRole)) {
         throw new DuplicateMappingException(DuplicateMappingException.Type.ENTITY, collectionRole);
      } else {
         this.collectionBindingMap.put(collectionRole, pluralAttributeBinding);
      }
   }

   public void addImport(String importName, String entityName) {
      if (importName != null && entityName != null) {
         LOG.tracev("Import: {0} -> {1}", importName, entityName);
         String old = (String)this.imports.put(importName, entityName);
         if (old != null) {
            LOG.debug("import name [" + importName + "] overrode previous [{" + old + "}]");
         }

      } else {
         throw new IllegalArgumentException("Import name or entity name is null");
      }
   }

   public Iterable getImports() {
      return this.imports.entrySet();
   }

   public Iterable getFetchProfiles() {
      return this.fetchProfiles.values();
   }

   public TypeResolver getTypeResolver() {
      return this.typeResolver;
   }

   public SessionFactoryBuilder getSessionFactoryBuilder() {
      return this.sessionFactoryBuilder;
   }

   public NamingStrategy getNamingStrategy() {
      return this.options.getNamingStrategy();
   }

   public boolean isGloballyQuotedIdentifiers() {
      return this.globallyQuotedIdentifiers || this.getOptions().isGloballyQuotedIdentifiers();
   }

   public void setGloballyQuotedIdentifiers(boolean globallyQuotedIdentifiers) {
      this.globallyQuotedIdentifiers = globallyQuotedIdentifiers;
   }

   public MappingDefaults getMappingDefaults() {
      return this.mappingDefaults;
   }

   public MetaAttributeContext getGlobalMetaAttributeContext() {
      return this.globalMetaAttributeContext;
   }

   public MetadataImplementor getMetadataImplementor() {
      return this;
   }

   public IdentifierGeneratorFactory getIdentifierGeneratorFactory() {
      return this.identifierGeneratorFactory;
   }

   public org.hibernate.type.Type getIdentifierType(String entityName) throws MappingException {
      EntityBinding entityBinding = this.getEntityBinding(entityName);
      if (entityBinding == null) {
         throw new MappingException("Entity binding not known: " + entityName);
      } else {
         return entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding().getHibernateTypeDescriptor().getResolvedTypeMapping();
      }
   }

   public String getIdentifierPropertyName(String entityName) throws MappingException {
      EntityBinding entityBinding = this.getEntityBinding(entityName);
      if (entityBinding == null) {
         throw new MappingException("Entity binding not known: " + entityName);
      } else {
         AttributeBinding idBinding = entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding();
         return idBinding == null ? null : idBinding.getAttribute().getName();
      }
   }

   public org.hibernate.type.Type getReferencedPropertyType(String entityName, String propertyName) throws MappingException {
      EntityBinding entityBinding = this.getEntityBinding(entityName);
      if (entityBinding == null) {
         throw new MappingException("Entity binding not known: " + entityName);
      } else {
         AttributeBinding attributeBinding = entityBinding.locateAttributeBinding(propertyName);
         if (attributeBinding == null) {
            throw new MappingException("unknown property: " + entityName + '.' + propertyName);
         } else {
            return attributeBinding.getHibernateTypeDescriptor().getResolvedTypeMapping();
         }
      }
   }

   private class MappingDefaultsImpl implements MappingDefaults {
      private final ValueHolder regionFactorySpecifiedDefaultAccessType;

      private MappingDefaultsImpl() {
         this.regionFactorySpecifiedDefaultAccessType = new ValueHolder(new ValueHolder.DeferredInitializer() {
            public AccessType initialize() {
               RegionFactory regionFactory = (RegionFactory)MetadataImpl.this.getServiceRegistry().getService(RegionFactory.class);
               return regionFactory.getDefaultAccessType();
            }
         });
      }

      public String getPackageName() {
         return null;
      }

      public String getSchemaName() {
         return MetadataImpl.this.options.getDefaultSchemaName();
      }

      public String getCatalogName() {
         return MetadataImpl.this.options.getDefaultCatalogName();
      }

      public String getIdColumnName() {
         return "id";
      }

      public String getDiscriminatorColumnName() {
         return "class";
      }

      public String getCascadeStyle() {
         return "none";
      }

      public String getPropertyAccessorName() {
         return "property";
      }

      public boolean areAssociationsLazy() {
         return true;
      }

      public AccessType getCacheAccessType() {
         return MetadataImpl.this.options.getDefaultAccessType() != null ? MetadataImpl.this.options.getDefaultAccessType() : (AccessType)this.regionFactorySpecifiedDefaultAccessType.getValue();
      }
   }
}
