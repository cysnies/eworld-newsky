package org.hibernate.metamodel.source.hbm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.internal.jaxb.mapping.hbm.EntityElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbHibernateMapping;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbJoinedSubclassElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbSubclassElement;
import org.hibernate.internal.jaxb.mapping.hbm.JaxbUnionSubclassElement;
import org.hibernate.internal.jaxb.mapping.hbm.SubEntityElement;
import org.hibernate.metamodel.source.binder.SubclassEntityContainer;
import org.hibernate.metamodel.source.binder.SubclassEntitySource;

public class HierarchyBuilder {
   private final List entityHierarchies = new ArrayList();
   private final Map subEntityContainerMap = new HashMap();
   private final List extendsQueue = new ArrayList();
   private MappingDocument currentMappingDocument;

   public void processMappingDocument(MappingDocument mappingDocument) {
      this.currentMappingDocument = mappingDocument;

      try {
         this.processCurrentMappingDocument();
      } finally {
         this.currentMappingDocument = null;
      }

   }

   private void processCurrentMappingDocument() {
      for(Object entityElementO : this.currentMappingDocument.getMappingRoot().getClazzOrSubclassOrJoinedSubclass()) {
         EntityElement entityElement = (EntityElement)entityElementO;
         if (JaxbHibernateMapping.JaxbClass.class.isInstance(entityElement)) {
            JaxbHibernateMapping.JaxbClass jaxbClass = (JaxbHibernateMapping.JaxbClass)entityElement;
            RootEntitySourceImpl rootEntitySource = new RootEntitySourceImpl(this.currentMappingDocument, jaxbClass);
            EntityHierarchyImpl hierarchy = new EntityHierarchyImpl(rootEntitySource);
            this.entityHierarchies.add(hierarchy);
            this.subEntityContainerMap.put(rootEntitySource.getEntityName(), rootEntitySource);
            this.processSubElements(entityElement, rootEntitySource);
         } else {
            SubclassEntitySourceImpl subClassEntitySource = new SubclassEntitySourceImpl(this.currentMappingDocument, entityElement);
            String entityName = subClassEntitySource.getEntityName();
            this.subEntityContainerMap.put(entityName, subClassEntitySource);
            String entityItExtends = this.currentMappingDocument.getMappingLocalBindingContext().qualifyClassName(((SubEntityElement)entityElement).getExtends());
            this.processSubElements(entityElement, subClassEntitySource);
            SubclassEntityContainer container = (SubclassEntityContainer)this.subEntityContainerMap.get(entityItExtends);
            if (container != null) {
               container.add(subClassEntitySource);
            } else {
               this.extendsQueue.add(new ExtendsQueueEntry(subClassEntitySource, entityItExtends));
            }
         }
      }

   }

   public List groupEntityHierarchies() {
      while(true) {
         if (!this.extendsQueue.isEmpty()) {
            int numberOfMappingsProcessed = 0;
            Iterator<ExtendsQueueEntry> iterator = this.extendsQueue.iterator();

            while(iterator.hasNext()) {
               ExtendsQueueEntry entry = (ExtendsQueueEntry)iterator.next();
               SubclassEntityContainer container = (SubclassEntityContainer)this.subEntityContainerMap.get(entry.entityItExtends);
               if (container != null) {
                  container.add(entry.subClassEntitySource);
                  iterator.remove();
                  ++numberOfMappingsProcessed;
               }
            }

            if (numberOfMappingsProcessed != 0) {
               continue;
            }

            throw new MappingException("Unable to process extends dependencies in hbm files");
         }

         return this.entityHierarchies;
      }
   }

   private void processSubElements(EntityElement entityElement, SubclassEntityContainer container) {
      if (JaxbHibernateMapping.JaxbClass.class.isInstance(entityElement)) {
         JaxbHibernateMapping.JaxbClass jaxbClass = (JaxbHibernateMapping.JaxbClass)entityElement;
         this.processElements(jaxbClass.getJoinedSubclass(), container);
         this.processElements(jaxbClass.getSubclass(), container);
         this.processElements(jaxbClass.getUnionSubclass(), container);
      } else if (JaxbSubclassElement.class.isInstance(entityElement)) {
         JaxbSubclassElement jaxbSubclass = (JaxbSubclassElement)entityElement;
         this.processElements(jaxbSubclass.getSubclass(), container);
      } else if (JaxbJoinedSubclassElement.class.isInstance(entityElement)) {
         JaxbJoinedSubclassElement jaxbJoinedSubclass = (JaxbJoinedSubclassElement)entityElement;
         this.processElements(jaxbJoinedSubclass.getJoinedSubclass(), container);
      } else if (JaxbUnionSubclassElement.class.isInstance(entityElement)) {
         JaxbUnionSubclassElement jaxbUnionSubclass = (JaxbUnionSubclassElement)entityElement;
         this.processElements(jaxbUnionSubclass.getUnionSubclass(), container);
      }

   }

   private void processElements(List subElements, SubclassEntityContainer container) {
      for(Object subElementO : subElements) {
         SubEntityElement subElement = (SubEntityElement)subElementO;
         SubclassEntitySourceImpl subclassEntitySource = new SubclassEntitySourceImpl(this.currentMappingDocument, subElement);
         container.add(subclassEntitySource);
         String subEntityName = subclassEntitySource.getEntityName();
         this.subEntityContainerMap.put(subEntityName, subclassEntitySource);
      }

   }

   private static class ExtendsQueueEntry {
      private final SubclassEntitySource subClassEntitySource;
      private final String entityItExtends;

      private ExtendsQueueEntry(SubclassEntitySource subClassEntitySource, String entityItExtends) {
         this.subClassEntitySource = subClassEntitySource;
         this.entityItExtends = entityItExtends;
      }
   }
}
