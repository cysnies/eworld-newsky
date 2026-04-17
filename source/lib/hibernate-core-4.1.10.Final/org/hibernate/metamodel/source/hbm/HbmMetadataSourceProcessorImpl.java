package org.hibernate.metamodel.source.hbm;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.internal.jaxb.JaxbRoot;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.MetadataSourceProcessor;
import org.hibernate.metamodel.source.binder.Binder;

public class HbmMetadataSourceProcessorImpl implements MetadataSourceProcessor {
   private final MetadataImplementor metadata;
   private List processors = new ArrayList();
   private List entityHierarchies;

   public HbmMetadataSourceProcessorImpl(MetadataImplementor metadata) {
      this.metadata = metadata;
   }

   public void prepare(MetadataSources sources) {
      HierarchyBuilder hierarchyBuilder = new HierarchyBuilder();

      for(JaxbRoot jaxbRoot : sources.getJaxbRootList()) {
         if (JaxbHibernateMapping.class.isInstance(jaxbRoot.getRoot())) {
            MappingDocument mappingDocument = new MappingDocument(jaxbRoot, this.metadata);
            this.processors.add(new HibernateMappingProcessor(this.metadata, mappingDocument));
            hierarchyBuilder.processMappingDocument(mappingDocument);
         }
      }

      this.entityHierarchies = hierarchyBuilder.groupEntityHierarchies();
   }

   public void processIndependentMetadata(MetadataSources sources) {
      for(HibernateMappingProcessor processor : this.processors) {
         processor.processIndependentMetadata();
      }

   }

   public void processTypeDependentMetadata(MetadataSources sources) {
      for(HibernateMappingProcessor processor : this.processors) {
         processor.processTypeDependentMetadata();
      }

   }

   public void processMappingMetadata(MetadataSources sources, List processedEntityNames) {
      Binder binder = new Binder(this.metadata, processedEntityNames);

      for(EntityHierarchyImpl entityHierarchy : this.entityHierarchies) {
         binder.processEntityHierarchy(entityHierarchy);
      }

   }

   public void processMappingDependentMetadata(MetadataSources sources) {
      for(HibernateMappingProcessor processor : this.processors) {
         processor.processMappingDependentMetadata();
      }

   }
}
