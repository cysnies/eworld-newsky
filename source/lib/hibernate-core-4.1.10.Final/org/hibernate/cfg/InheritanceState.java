package org.hibernate.cfg;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.persistence.Access;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import org.hibernate.AnnotationException;
import org.hibernate.annotations.common.reflection.XAnnotatedElement;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.cfg.annotations.EntityBinder;
import org.hibernate.mapping.PersistentClass;

public class InheritanceState {
   private XClass clazz;
   private boolean hasSiblings = false;
   private boolean hasParents = false;
   private InheritanceType type;
   private boolean isEmbeddableSuperclass = false;
   private Map inheritanceStatePerClass;
   private List classesToProcessForMappedSuperclass = new ArrayList();
   private Mappings mappings;
   private AccessType accessType;
   private ElementsToProcess elementsToProcess;
   private Boolean hasIdClassOrEmbeddedId;

   public InheritanceState(XClass clazz, Map inheritanceStatePerClass, Mappings mappings) {
      this.setClazz(clazz);
      this.mappings = mappings;
      this.inheritanceStatePerClass = inheritanceStatePerClass;
      this.extractInheritanceType();
   }

   private void extractInheritanceType() {
      XAnnotatedElement element = this.getClazz();
      Inheritance inhAnn = (Inheritance)element.getAnnotation(Inheritance.class);
      MappedSuperclass mappedSuperClass = (MappedSuperclass)element.getAnnotation(MappedSuperclass.class);
      if (mappedSuperClass != null) {
         this.setEmbeddableSuperclass(true);
         this.setType(inhAnn == null ? null : inhAnn.strategy());
      } else {
         this.setType(inhAnn == null ? InheritanceType.SINGLE_TABLE : inhAnn.strategy());
      }

   }

   boolean hasTable() {
      return !this.hasParents() || !InheritanceType.SINGLE_TABLE.equals(this.getType());
   }

   boolean hasDenormalizedTable() {
      return this.hasParents() && InheritanceType.TABLE_PER_CLASS.equals(this.getType());
   }

   public static InheritanceState getInheritanceStateOfSuperEntity(XClass clazz, Map states) {
      XClass superclass = clazz;

      do {
         superclass = superclass.getSuperclass();
         InheritanceState currentState = (InheritanceState)states.get(superclass);
         if (currentState != null && !currentState.isEmbeddableSuperclass()) {
            return currentState;
         }
      } while(superclass != null && !Object.class.getName().equals(superclass.getName()));

      return null;
   }

   public static InheritanceState getSuperclassInheritanceState(XClass clazz, Map states) {
      XClass superclass = clazz;

      do {
         superclass = superclass.getSuperclass();
         InheritanceState currentState = (InheritanceState)states.get(superclass);
         if (currentState != null) {
            return currentState;
         }
      } while(superclass != null && !Object.class.getName().equals(superclass.getName()));

      return null;
   }

   public XClass getClazz() {
      return this.clazz;
   }

   public void setClazz(XClass clazz) {
      this.clazz = clazz;
   }

   public boolean hasSiblings() {
      return this.hasSiblings;
   }

   public void setHasSiblings(boolean hasSiblings) {
      this.hasSiblings = hasSiblings;
   }

   public boolean hasParents() {
      return this.hasParents;
   }

   public void setHasParents(boolean hasParents) {
      this.hasParents = hasParents;
   }

   public InheritanceType getType() {
      return this.type;
   }

   public void setType(InheritanceType type) {
      this.type = type;
   }

   public boolean isEmbeddableSuperclass() {
      return this.isEmbeddableSuperclass;
   }

   public void setEmbeddableSuperclass(boolean embeddableSuperclass) {
      this.isEmbeddableSuperclass = embeddableSuperclass;
   }

   void postProcess(PersistentClass persistenceClass, EntityBinder entityBinder) {
      this.getElementsToProcess();
      this.addMappedSuperClassInMetadata(persistenceClass);
      entityBinder.setPropertyAccessType(this.accessType);
   }

   public XClass getClassWithIdClass(boolean evenIfSubclass) {
      if (!evenIfSubclass && this.hasParents()) {
         return null;
      } else if (this.clazz.isAnnotationPresent(IdClass.class)) {
         return this.clazz;
      } else {
         InheritanceState state = getSuperclassInheritanceState(this.clazz, this.inheritanceStatePerClass);
         return state != null ? state.getClassWithIdClass(true) : null;
      }
   }

   public Boolean hasIdClassOrEmbeddedId() {
      if (this.hasIdClassOrEmbeddedId == null) {
         this.hasIdClassOrEmbeddedId = false;
         if (this.getClassWithIdClass(true) != null) {
            this.hasIdClassOrEmbeddedId = true;
         } else {
            ElementsToProcess process = this.getElementsToProcess();

            for(PropertyData property : process.getElements()) {
               if (property.getProperty().isAnnotationPresent(EmbeddedId.class)) {
                  this.hasIdClassOrEmbeddedId = true;
                  break;
               }
            }
         }
      }

      return this.hasIdClassOrEmbeddedId;
   }

   public ElementsToProcess getElementsToProcess() {
      if (this.elementsToProcess == null) {
         InheritanceState inheritanceState = (InheritanceState)this.inheritanceStatePerClass.get(this.clazz);

         assert !inheritanceState.isEmbeddableSuperclass();

         this.getMappedSuperclassesTillNextEntityOrdered();
         this.accessType = this.determineDefaultAccessType();
         ArrayList<PropertyData> elements = new ArrayList();
         int deep = this.classesToProcessForMappedSuperclass.size();
         int idPropertyCount = 0;

         for(int index = 0; index < deep; ++index) {
            PropertyContainer propertyContainer = new PropertyContainer((XClass)this.classesToProcessForMappedSuperclass.get(index), this.clazz);
            int currentIdPropertyCount = AnnotationBinder.addElementsOfClass(elements, this.accessType, propertyContainer, this.mappings);
            idPropertyCount += currentIdPropertyCount;
         }

         if (idPropertyCount == 0 && !inheritanceState.hasParents()) {
            throw new AnnotationException("No identifier specified for entity: " + this.clazz.getName());
         }

         elements.trimToSize();
         this.elementsToProcess = new ElementsToProcess(elements, idPropertyCount);
      }

      return this.elementsToProcess;
   }

   private AccessType determineDefaultAccessType() {
      for(XClass xclass = this.clazz; xclass != null; xclass = xclass.getSuperclass()) {
         if ((xclass.getSuperclass() == null || Object.class.getName().equals(xclass.getSuperclass().getName())) && (xclass.isAnnotationPresent(Entity.class) || xclass.isAnnotationPresent(MappedSuperclass.class)) && xclass.isAnnotationPresent(Access.class)) {
            return AccessType.getAccessStrategy(((Access)xclass.getAnnotation(Access.class)).value());
         }
      }

      for(XClass xclass = this.clazz; xclass != null && !Object.class.getName().equals(xclass.getName()); xclass = xclass.getSuperclass()) {
         if (xclass.isAnnotationPresent(Entity.class) || xclass.isAnnotationPresent(MappedSuperclass.class)) {
            for(XProperty prop : xclass.getDeclaredProperties(AccessType.PROPERTY.getType())) {
               boolean isEmbeddedId = prop.isAnnotationPresent(EmbeddedId.class);
               if (prop.isAnnotationPresent(Id.class) || isEmbeddedId) {
                  return AccessType.PROPERTY;
               }
            }

            for(XProperty prop : xclass.getDeclaredProperties(AccessType.FIELD.getType())) {
               boolean isEmbeddedId = prop.isAnnotationPresent(EmbeddedId.class);
               if (prop.isAnnotationPresent(Id.class) || isEmbeddedId) {
                  return AccessType.FIELD;
               }
            }
         }
      }

      throw new AnnotationException("No identifier specified for entity: " + this.clazz);
   }

   private void getMappedSuperclassesTillNextEntityOrdered() {
      XClass currentClassInHierarchy = this.clazz;

      InheritanceState superclassState;
      do {
         this.classesToProcessForMappedSuperclass.add(0, currentClassInHierarchy);
         XClass superClass = currentClassInHierarchy;

         do {
            superClass = superClass.getSuperclass();
            superclassState = (InheritanceState)this.inheritanceStatePerClass.get(superClass);
         } while(superClass != null && !this.mappings.getReflectionManager().equals(superClass, Object.class) && superclassState == null);

         currentClassInHierarchy = superClass;
      } while(superclassState != null && superclassState.isEmbeddableSuperclass());

   }

   private void addMappedSuperClassInMetadata(PersistentClass persistentClass) {
      org.hibernate.mapping.MappedSuperclass mappedSuperclass = null;
      InheritanceState superEntityState = getInheritanceStateOfSuperEntity(this.clazz, this.inheritanceStatePerClass);
      PersistentClass superEntity = superEntityState != null ? this.mappings.getClass(superEntityState.getClazz().getName()) : null;
      int lastMappedSuperclass = this.classesToProcessForMappedSuperclass.size() - 1;

      for(int index = 0; index < lastMappedSuperclass; ++index) {
         org.hibernate.mapping.MappedSuperclass parentSuperclass = mappedSuperclass;
         Class<?> type = this.mappings.getReflectionManager().toClass((XClass)this.classesToProcessForMappedSuperclass.get(index));
         mappedSuperclass = this.mappings.getMappedSuperclass(type);
         if (mappedSuperclass == null) {
            mappedSuperclass = new org.hibernate.mapping.MappedSuperclass(parentSuperclass, superEntity);
            mappedSuperclass.setMappedClass(type);
            this.mappings.addMappedSuperclass(type, mappedSuperclass);
         }
      }

      if (mappedSuperclass != null) {
         persistentClass.setSuperMappedSuperclass(mappedSuperclass);
      }

   }

   static final class ElementsToProcess {
      private final List properties;
      private final int idPropertyCount;

      public List getElements() {
         return this.properties;
      }

      public int getIdPropertyCount() {
         return this.idPropertyCount;
      }

      private ElementsToProcess(List properties, int idPropertyCount) {
         this.properties = properties;
         this.idPropertyCount = idPropertyCount;
      }
   }
}
