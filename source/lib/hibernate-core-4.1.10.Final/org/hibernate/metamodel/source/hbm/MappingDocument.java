package org.hibernate.metamodel.source.hbm;

import java.util.List;
import org.hibernate.cfg.NamingStrategy;
import org.hibernate.internal.jaxb.JaxbRoot;
import org.hibernate.internal.jaxb.Origin;
import org.hibernate.internal.jaxb.mapping.hbm.EntityElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbFetchProfileElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.metamodel.domain.Type;
import org.hibernate.metamodel.source.MappingDefaults;
import org.hibernate.metamodel.source.MetaAttributeContext;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.internal.OverriddenMappingDefaults;
import org.hibernate.service.ServiceRegistry;

public class MappingDocument {
   private final JaxbRoot hbmJaxbRoot;
   private final LocalBindingContextImpl mappingLocalBindingContext;

   public MappingDocument(JaxbRoot hbmJaxbRoot, MetadataImplementor metadata) {
      this.hbmJaxbRoot = hbmJaxbRoot;
      this.mappingLocalBindingContext = new LocalBindingContextImpl(metadata);
   }

   public JaxbHibernateMapping getMappingRoot() {
      return (JaxbHibernateMapping)this.hbmJaxbRoot.getRoot();
   }

   public Origin getOrigin() {
      return this.hbmJaxbRoot.getOrigin();
   }

   public JaxbRoot getJaxbRoot() {
      return this.hbmJaxbRoot;
   }

   public HbmBindingContext getMappingLocalBindingContext() {
      return this.mappingLocalBindingContext;
   }

   private class LocalBindingContextImpl implements HbmBindingContext {
      private final MetadataImplementor metadata;
      private final MappingDefaults localMappingDefaults;
      private final MetaAttributeContext metaAttributeContext;

      private LocalBindingContextImpl(MetadataImplementor metadata) {
         this.metadata = metadata;
         this.localMappingDefaults = new OverriddenMappingDefaults(metadata.getMappingDefaults(), ((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getPackage(), ((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getSchema(), ((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getCatalog(), (String)null, (String)null, ((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getDefaultCascade(), ((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getDefaultAccess(), ((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).isDefaultLazy());
         if (((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getMeta() != null && !((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getMeta().isEmpty()) {
            this.metaAttributeContext = Helper.extractMetaAttributeContext(((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).getMeta(), true, metadata.getGlobalMetaAttributeContext());
         } else {
            this.metaAttributeContext = new MetaAttributeContext(metadata.getGlobalMetaAttributeContext());
         }

      }

      public ServiceRegistry getServiceRegistry() {
         return this.metadata.getServiceRegistry();
      }

      public NamingStrategy getNamingStrategy() {
         return this.metadata.getNamingStrategy();
      }

      public MappingDefaults getMappingDefaults() {
         return this.localMappingDefaults;
      }

      public MetadataImplementor getMetadataImplementor() {
         return this.metadata;
      }

      public Class locateClassByName(String name) {
         return this.metadata.locateClassByName(name);
      }

      public Type makeJavaType(String className) {
         return this.metadata.makeJavaType(className);
      }

      public ValueHolder makeClassReference(String className) {
         return this.metadata.makeClassReference(className);
      }

      public boolean isAutoImport() {
         return ((JaxbHibernateMapping)MappingDocument.this.hbmJaxbRoot.getRoot()).isAutoImport();
      }

      public MetaAttributeContext getMetaAttributeContext() {
         return this.metaAttributeContext;
      }

      public Origin getOrigin() {
         return MappingDocument.this.hbmJaxbRoot.getOrigin();
      }

      public String qualifyClassName(String unqualifiedName) {
         return Helper.qualifyIfNeeded(unqualifiedName, this.getMappingDefaults().getPackageName());
      }

      public String determineEntityName(EntityElement entityElement) {
         return Helper.determineEntityName(entityElement, this.getMappingDefaults().getPackageName());
      }

      public boolean isGloballyQuotedIdentifiers() {
         return this.metadata.isGloballyQuotedIdentifiers();
      }

      public void processFetchProfiles(List fetchProfiles, String containingEntityName) {
      }
   }
}
