package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEmbeddable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntity;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityMappings;
import org.hibernate.internal.jaxb.mapping.orm.JaxbMappedSuperclass;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPersistenceUnitDefaults;
import org.hibernate.internal.jaxb.mapping.orm.JaxbPersistenceUnitMetadata;
import org.hibernate.service.ServiceRegistry;
import org.jboss.jandex.Index;
import org.jboss.logging.Logger;

public class EntityMappingsMocker {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, EntityMappingsMocker.class.getName());
   private final List entityMappingsList;
   private Default globalDefaults;
   private final IndexBuilder indexBuilder;
   private final GlobalAnnotations globalAnnotations;

   public EntityMappingsMocker(List entityMappingsList, Index index, ServiceRegistry serviceRegistry) {
      this.entityMappingsList = entityMappingsList;
      this.indexBuilder = new IndexBuilder(index, serviceRegistry);
      this.globalAnnotations = new GlobalAnnotations();
   }

   public Index mockNewIndex() {
      this.processPersistenceUnitMetadata(this.entityMappingsList);
      this.processEntityMappings(this.entityMappingsList);
      this.processGlobalAnnotations();
      return this.indexBuilder.build(this.globalDefaults);
   }

   private void processPersistenceUnitMetadata(List entityMappingsList) {
      for(JaxbEntityMappings entityMappings : entityMappingsList) {
         JaxbPersistenceUnitMetadata pum = entityMappings.getPersistenceUnitMetadata();
         if (this.globalDefaults != null) {
            LOG.duplicateMetadata();
            return;
         }

         if (pum != null) {
            this.globalDefaults = new Default();
            if (pum.getXmlMappingMetadataComplete() != null) {
               this.globalDefaults.setMetadataComplete(true);
               this.indexBuilder.mappingMetadataComplete();
            }

            JaxbPersistenceUnitDefaults pud = pum.getPersistenceUnitDefaults();
            if (pud == null) {
               return;
            }

            this.globalDefaults.setSchema(pud.getSchema());
            this.globalDefaults.setCatalog(pud.getCatalog());
            this.globalDefaults.setCascadePersist(pud.getCascadePersist() != null);
            (new PersistenceMetadataMocker(this.indexBuilder, pud)).process();
         }
      }

   }

   private void processEntityMappings(List entityMappingsList) {
      List<AbstractEntityObjectMocker> mockerList = new ArrayList();

      for(JaxbEntityMappings entityMappings : entityMappingsList) {
         Default defaults = this.getEntityMappingsDefaults(entityMappings);
         this.globalAnnotations.collectGlobalMappings(entityMappings, defaults);

         for(JaxbMappedSuperclass mappedSuperclass : entityMappings.getMappedSuperclass()) {
            AbstractEntityObjectMocker mocker = new MappedSuperclassMocker(this.indexBuilder, mappedSuperclass, defaults);
            mockerList.add(mocker);
            mocker.preProcess();
         }

         for(JaxbEmbeddable embeddable : entityMappings.getEmbeddable()) {
            AbstractEntityObjectMocker mocker = new EmbeddableMocker(this.indexBuilder, embeddable, defaults);
            mockerList.add(mocker);
            mocker.preProcess();
         }

         for(JaxbEntity entity : entityMappings.getEntity()) {
            this.globalAnnotations.collectGlobalMappings(entity, defaults);
            AbstractEntityObjectMocker mocker = new EntityMocker(this.indexBuilder, entity, defaults);
            mockerList.add(mocker);
            mocker.preProcess();
         }
      }

      for(AbstractEntityObjectMocker mocker : mockerList) {
         mocker.process();
      }

   }

   private void processGlobalAnnotations() {
      if (this.globalAnnotations.hasGlobalConfiguration()) {
         this.indexBuilder.collectGlobalConfigurationFromIndex(this.globalAnnotations);
         (new GlobalAnnotationMocker(this.indexBuilder, this.globalAnnotations)).process();
      }

   }

   private Default getEntityMappingsDefaults(JaxbEntityMappings entityMappings) {
      Default entityMappingDefault = new Default();
      entityMappingDefault.setPackageName(entityMappings.getPackage());
      entityMappingDefault.setSchema(entityMappings.getSchema());
      entityMappingDefault.setCatalog(entityMappings.getCatalog());
      entityMappingDefault.setAccess(entityMappings.getAccess());
      Default defaults = new Default();
      defaults.override(this.globalDefaults);
      defaults.override(entityMappingDefault);
      return defaults;
   }

   public static class Default implements Serializable {
      private JaxbAccessType access;
      private String packageName;
      private String schema;
      private String catalog;
      private Boolean metadataComplete;
      private Boolean cascadePersist;

      public JaxbAccessType getAccess() {
         return this.access;
      }

      void setAccess(JaxbAccessType access) {
         this.access = access;
      }

      public String getCatalog() {
         return this.catalog;
      }

      void setCatalog(String catalog) {
         this.catalog = catalog;
      }

      public String getPackageName() {
         return this.packageName;
      }

      void setPackageName(String packageName) {
         this.packageName = packageName;
      }

      public String getSchema() {
         return this.schema;
      }

      void setSchema(String schema) {
         this.schema = schema;
      }

      public Boolean isMetadataComplete() {
         return this.metadataComplete;
      }

      void setMetadataComplete(Boolean metadataComplete) {
         this.metadataComplete = metadataComplete;
      }

      public Boolean isCascadePersist() {
         return this.cascadePersist;
      }

      void setCascadePersist(Boolean cascadePersist) {
         this.cascadePersist = cascadePersist;
      }

      void override(Default globalDefault) {
         if (globalDefault != null) {
            if (globalDefault.getAccess() != null) {
               this.access = globalDefault.getAccess();
            }

            if (globalDefault.getPackageName() != null) {
               this.packageName = globalDefault.getPackageName();
            }

            if (globalDefault.getSchema() != null) {
               this.schema = globalDefault.getSchema();
            }

            if (globalDefault.getCatalog() != null) {
               this.catalog = globalDefault.getCatalog();
            }

            if (globalDefault.isCascadePersist() != null) {
               this.cascadePersist = globalDefault.isCascadePersist();
            }

            if (globalDefault.isMetadataComplete() != null) {
               this.metadataComplete = globalDefault.isMetadataComplete();
            }
         }

      }
   }
}
