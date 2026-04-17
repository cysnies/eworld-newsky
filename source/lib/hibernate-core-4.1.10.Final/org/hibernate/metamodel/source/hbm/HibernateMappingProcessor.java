package org.hibernate.metamodel.source.hbm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.engine.spi.FilterDefinition;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbFetchProfileElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbParamElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbQueryElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbSqlQueryElement;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.metamodel.binding.FetchProfile;
import org.hibernate.metamodel.binding.TypeDef;
import org.hibernate.metamodel.relational.AuxiliaryDatabaseObject;
import org.hibernate.metamodel.relational.BasicAuxiliaryDatabaseObjectImpl;
import org.hibernate.metamodel.source.MappingException;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.classloading.spi.ClassLoadingException;
import org.hibernate.type.Type;

public class HibernateMappingProcessor {
   private final MetadataImplementor metadata;
   private final MappingDocument mappingDocument;
   private ValueHolder classLoaderService = new ValueHolder(new ValueHolder.DeferredInitializer() {
      public ClassLoaderService initialize() {
         return (ClassLoaderService)HibernateMappingProcessor.this.metadata.getServiceRegistry().getService(ClassLoaderService.class);
      }
   });

   public HibernateMappingProcessor(MetadataImplementor metadata, MappingDocument mappingDocument) {
      this.metadata = metadata;
      this.mappingDocument = mappingDocument;
   }

   private JaxbHibernateMapping mappingRoot() {
      return this.mappingDocument.getMappingRoot();
   }

   private Origin origin() {
      return this.mappingDocument.getOrigin();
   }

   private HbmBindingContext bindingContext() {
      return this.mappingDocument.getMappingLocalBindingContext();
   }

   private Class classForName(String name) {
      return ((ClassLoaderService)this.classLoaderService.getValue()).classForName(this.bindingContext().qualifyClassName(name));
   }

   public void processIndependentMetadata() {
      this.processDatabaseObjectDefinitions();
      this.processTypeDefinitions();
   }

   private void processDatabaseObjectDefinitions() {
      if (this.mappingRoot().getDatabaseObject() != null) {
         for(JaxbHibernateMapping.JaxbDatabaseObject databaseObjectElement : this.mappingRoot().getDatabaseObject()) {
            AuxiliaryDatabaseObject auxiliaryDatabaseObject;
            if (databaseObjectElement.getDefinition() != null) {
               String className = databaseObjectElement.getDefinition().getClazz();

               try {
                  auxiliaryDatabaseObject = (AuxiliaryDatabaseObject)this.classForName(className).newInstance();
               } catch (ClassLoadingException e) {
                  throw e;
               } catch (Exception var8) {
                  throw new MappingException("could not instantiate custom database object class [" + className + "]", this.origin());
               }
            } else {
               Set<String> dialectScopes = new HashSet();
               if (databaseObjectElement.getDialectScope() != null) {
                  for(JaxbHibernateMapping.JaxbDatabaseObject.JaxbDialectScope dialectScope : databaseObjectElement.getDialectScope()) {
                     dialectScopes.add(dialectScope.getName());
                  }
               }

               auxiliaryDatabaseObject = new BasicAuxiliaryDatabaseObjectImpl(this.metadata.getDatabase().getDefaultSchema(), databaseObjectElement.getCreate(), databaseObjectElement.getDrop(), dialectScopes);
            }

            this.metadata.getDatabase().addAuxiliaryDatabaseObject(auxiliaryDatabaseObject);
         }

      }
   }

   private void processTypeDefinitions() {
      if (this.mappingRoot().getTypedef() != null) {
         for(JaxbHibernateMapping.JaxbTypedef typedef : this.mappingRoot().getTypedef()) {
            Map<String, String> parameters = new HashMap();

            for(JaxbParamElement paramElement : typedef.getParam()) {
               parameters.put(paramElement.getName(), paramElement.getValue());
            }

            this.metadata.addTypeDefinition(new TypeDef(typedef.getName(), typedef.getClazz(), parameters));
         }

      }
   }

   public void processTypeDependentMetadata() {
      this.processFilterDefinitions();
      this.processIdentifierGenerators();
   }

   private void processFilterDefinitions() {
      if (this.mappingRoot().getFilterDef() != null) {
         for(JaxbHibernateMapping.JaxbFilterDef filterDefinition : this.mappingRoot().getFilterDef()) {
            String name = filterDefinition.getName();
            Map<String, Type> parameters = new HashMap();
            String condition = null;

            for(Object o : filterDefinition.getContent()) {
               if (o instanceof String) {
                  if (condition != null) {
                  }

                  condition = (String)o;
               } else {
                  if (!(o instanceof JaxbHibernateMapping.JaxbFilterDef.JaxbFilterParam)) {
                     throw new MappingException("Unrecognized nested filter content", this.origin());
                  }

                  JaxbHibernateMapping.JaxbFilterDef.JaxbFilterParam paramElement = (JaxbHibernateMapping.JaxbFilterDef.JaxbFilterParam)JaxbHibernateMapping.JaxbFilterDef.JaxbFilterParam.class.cast(o);
                  parameters.put(paramElement.getName(), this.metadata.getTypeResolver().heuristicType(paramElement.getType()));
               }
            }

            if (condition == null) {
               condition = filterDefinition.getCondition();
            }

            this.metadata.addFilterDefinition(new FilterDefinition(name, condition, parameters));
         }

      }
   }

   private void processIdentifierGenerators() {
      if (this.mappingRoot().getIdentifierGenerator() != null) {
         for(JaxbHibernateMapping.JaxbIdentifierGenerator identifierGeneratorElement : this.mappingRoot().getIdentifierGenerator()) {
            this.metadata.registerIdentifierGenerator(identifierGeneratorElement.getName(), identifierGeneratorElement.getClazz());
         }

      }
   }

   public void processMappingDependentMetadata() {
      this.processFetchProfiles();
      this.processImports();
      this.processResultSetMappings();
      this.processNamedQueries();
   }

   private void processFetchProfiles() {
      if (this.mappingRoot().getFetchProfile() != null) {
         this.processFetchProfiles(this.mappingRoot().getFetchProfile(), (String)null);
      }
   }

   public void processFetchProfiles(List fetchProfiles, String containingEntityName) {
      for(JaxbFetchProfileElement fetchProfile : fetchProfiles) {
         String profileName = fetchProfile.getName();
         Set<FetchProfile.Fetch> fetches = new HashSet();

         for(JaxbFetchProfileElement.JaxbFetch fetch : fetchProfile.getFetch()) {
            String entityName = fetch.getEntity() == null ? containingEntityName : fetch.getEntity();
            if (entityName == null) {
               throw new MappingException("could not determine entity for fetch-profile fetch [" + profileName + "]:[" + fetch.getAssociation() + "]", this.origin());
            }

            fetches.add(new FetchProfile.Fetch(entityName, fetch.getAssociation(), fetch.getStyle()));
         }

         this.metadata.addFetchProfile(new FetchProfile(profileName, fetches));
      }

   }

   private void processImports() {
      if (this.mappingRoot().getImport() != null) {
         for(JaxbHibernateMapping.JaxbImport importValue : this.mappingRoot().getImport()) {
            String className = this.mappingDocument.getMappingLocalBindingContext().qualifyClassName(importValue.getClazz());
            String rename = importValue.getRename();
            rename = rename == null ? StringHelper.unqualify(className) : rename;
            this.metadata.addImport(className, rename);
         }

      }
   }

   private void processResultSetMappings() {
      if (this.mappingRoot().getResultset() != null) {
         ;
      }
   }

   private void processNamedQueries() {
      if (this.mappingRoot().getQueryOrSqlQuery() != null) {
         for(Object queryOrSqlQuery : this.mappingRoot().getQueryOrSqlQuery()) {
            if (!JaxbQueryElement.class.isInstance(queryOrSqlQuery) && !JaxbSqlQueryElement.class.isInstance(queryOrSqlQuery)) {
               throw new MappingException("unknown type of query: " + queryOrSqlQuery.getClass().getName(), this.origin());
            }
         }

      }
   }
}
