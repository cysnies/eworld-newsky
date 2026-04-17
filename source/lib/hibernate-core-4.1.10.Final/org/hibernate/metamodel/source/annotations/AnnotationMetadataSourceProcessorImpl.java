package org.hibernate.metamodel.source.annotations;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.internal.jaxb.JaxbRoot;
import org.hibernate.internal.jaxb.mapping.orm.JaxbEntityMappings;
import org.hibernate.metamodel.MetadataSources;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.metamodel.source.MetadataSourceProcessor;
import org.hibernate.metamodel.source.annotations.global.FetchProfileBinder;
import org.hibernate.metamodel.source.annotations.global.FilterDefBinder;
import org.hibernate.metamodel.source.annotations.global.IdGeneratorBinder;
import org.hibernate.metamodel.source.annotations.global.QueryBinder;
import org.hibernate.metamodel.source.annotations.global.TableBinder;
import org.hibernate.metamodel.source.annotations.global.TypeDefBinder;
import org.hibernate.metamodel.source.annotations.xml.PseudoJpaDotNames;
import org.hibernate.metamodel.source.annotations.xml.mocker.EntityMappingsMocker;
import org.hibernate.metamodel.source.binder.Binder;
import org.hibernate.metamodel.source.binder.EntityHierarchy;
import org.hibernate.metamodel.source.internal.MetadataImpl;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;

public class AnnotationMetadataSourceProcessorImpl implements MetadataSourceProcessor {
   private static final Logger LOG = Logger.getLogger(AnnotationMetadataSourceProcessorImpl.class);
   private final MetadataImplementor metadata;
   private AnnotationBindingContext bindingContext;

   public AnnotationMetadataSourceProcessorImpl(MetadataImpl metadata) {
      this.metadata = metadata;
   }

   public void prepare(MetadataSources sources) {
      Indexer indexer = new Indexer();

      for(Class clazz : sources.getAnnotatedClasses()) {
         this.indexClass(indexer, clazz.getName().replace('.', '/') + ".class");
      }

      for(String packageName : sources.getAnnotatedPackages()) {
         this.indexClass(indexer, packageName.replace('.', '/') + "/package-info.class");
      }

      Index index = indexer.complete();
      List<JaxbRoot<JaxbEntityMappings>> mappings = new ArrayList();

      for(JaxbRoot root : sources.getJaxbRootList()) {
         if (root.getRoot() instanceof JaxbEntityMappings) {
            mappings.add(root);
         }
      }

      if (!mappings.isEmpty()) {
         index = this.parseAndUpdateIndex(mappings, index);
      }

      if (index.getAnnotations(PseudoJpaDotNames.DEFAULT_DELIMITED_IDENTIFIERS) != null) {
         this.metadata.setGloballyQuotedIdentifiers(true);
      }

      this.bindingContext = new AnnotationBindingContextImpl(this.metadata, index);
   }

   public void processIndependentMetadata(MetadataSources sources) {
      this.assertBindingContextExists();
      TypeDefBinder.bind(this.bindingContext);
   }

   private void assertBindingContextExists() {
      if (this.bindingContext == null) {
         throw new AssertionFailure("The binding context should exist. Has prepare been called!?");
      }
   }

   public void processTypeDependentMetadata(MetadataSources sources) {
      this.assertBindingContextExists();
      IdGeneratorBinder.bind(this.bindingContext);
   }

   public void processMappingMetadata(MetadataSources sources, List processedEntityNames) {
      this.assertBindingContextExists();
      Set<EntityHierarchy> hierarchies = EntityHierarchyBuilder.createEntityHierarchies(this.bindingContext);
      Binder binder = new Binder(this.bindingContext.getMetadataImplementor(), new ArrayList());

      for(EntityHierarchy hierarchy : hierarchies) {
         binder.processEntityHierarchy(hierarchy);
      }

   }

   public void processMappingDependentMetadata(MetadataSources sources) {
      TableBinder.bind(this.bindingContext);
      FetchProfileBinder.bind(this.bindingContext);
      QueryBinder.bind(this.bindingContext);
      FilterDefBinder.bind(this.bindingContext);
   }

   private Index parseAndUpdateIndex(List mappings, Index annotationIndex) {
      List<JaxbEntityMappings> list = new ArrayList(mappings.size());

      for(JaxbRoot jaxbRoot : mappings) {
         list.add(jaxbRoot.getRoot());
      }

      return (new EntityMappingsMocker(list, annotationIndex, this.metadata.getServiceRegistry())).mockNewIndex();
   }

   private void indexClass(Indexer indexer, String className) {
      InputStream stream = ((ClassLoaderService)this.metadata.getServiceRegistry().getService(ClassLoaderService.class)).locateResourceStream(className);

      try {
         indexer.index(stream);
      } catch (IOException e) {
         throw new HibernateException("Unable to open input stream for class " + className, e);
      }
   }
}
