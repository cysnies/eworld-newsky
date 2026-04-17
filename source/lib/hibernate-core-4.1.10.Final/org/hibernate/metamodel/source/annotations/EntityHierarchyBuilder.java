package org.hibernate.metamodel.source.annotations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.AccessType;
import org.hibernate.AnnotationException;
import org.hibernate.internal.util.collections.CollectionHelper;
import org.hibernate.metamodel.binding.InheritanceType;
import org.hibernate.metamodel.source.annotations.entity.EntityClass;
import org.hibernate.metamodel.source.annotations.entity.RootEntitySourceImpl;
import org.hibernate.metamodel.source.annotations.entity.SubclassEntitySourceImpl;
import org.hibernate.metamodel.source.binder.EntityHierarchy;
import org.hibernate.metamodel.source.binder.EntitySource;
import org.hibernate.metamodel.source.binder.SubclassEntitySource;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.FieldInfo;
import org.jboss.jandex.Index;
import org.jboss.jandex.MethodInfo;

public class EntityHierarchyBuilder {
   private static final DotName OBJECT = DotName.createSimple(Object.class.getName());

   public static Set createEntityHierarchies(AnnotationBindingContext bindingContext) {
      Set<EntityHierarchy> hierarchies = new HashSet();
      List<DotName> processedEntities = new ArrayList();
      Map<DotName, List<ClassInfo>> classToDirectSubClassMap = new HashMap();
      Index index = bindingContext.getIndex();

      for(ClassInfo info : index.getKnownClasses()) {
         if (isEntityClass(info) && !processedEntities.contains(info.name())) {
            ClassInfo rootClassInfo = findRootEntityClassInfo(index, info);
            List<ClassInfo> rootClassWithAllSubclasses = new ArrayList();
            addMappedSuperclasses(index, rootClassInfo, rootClassWithAllSubclasses);
            processHierarchy(bindingContext, rootClassInfo, rootClassWithAllSubclasses, processedEntities, classToDirectSubClassMap);
            AccessType defaultAccessType = determineDefaultAccessType(rootClassWithAllSubclasses);
            InheritanceType hierarchyInheritanceType = determineInheritanceType(rootClassInfo, rootClassWithAllSubclasses);
            EntityClass rootEntityClass = new EntityClass(rootClassInfo, (EntityClass)null, defaultAccessType, hierarchyInheritanceType, bindingContext);
            RootEntitySourceImpl rootSource = new RootEntitySourceImpl(rootEntityClass);
            addSubclassEntitySources(bindingContext, classToDirectSubClassMap, defaultAccessType, hierarchyInheritanceType, rootEntityClass, rootSource);
            hierarchies.add(new EntityHierarchyImpl(rootSource, hierarchyInheritanceType));
         }
      }

      return hierarchies;
   }

   private static void addSubclassEntitySources(AnnotationBindingContext bindingContext, Map classToDirectSubClassMap, AccessType defaultAccessType, InheritanceType hierarchyInheritanceType, EntityClass entityClass, EntitySource entitySource) {
      List<ClassInfo> subClassInfoList = (List)classToDirectSubClassMap.get(DotName.createSimple(entitySource.getClassName()));
      if (subClassInfoList != null) {
         for(ClassInfo subClassInfo : subClassInfoList) {
            EntityClass subclassEntityClass = new EntityClass(subClassInfo, entityClass, defaultAccessType, hierarchyInheritanceType, bindingContext);
            SubclassEntitySource subclassEntitySource = new SubclassEntitySourceImpl(subclassEntityClass);
            entitySource.add(subclassEntitySource);
            addSubclassEntitySources(bindingContext, classToDirectSubClassMap, defaultAccessType, hierarchyInheritanceType, subclassEntityClass, subclassEntitySource);
         }

      }
   }

   private static ClassInfo findRootEntityClassInfo(Index index, ClassInfo info) {
      ClassInfo rootEntity = info;

      ClassInfo tmpInfo;
      for(DotName superName = info.superName(); !OBJECT.equals(superName); superName = tmpInfo.superName()) {
         tmpInfo = index.getClassByName(superName);
         if (isEntityClass(tmpInfo)) {
            rootEntity = tmpInfo;
         }
      }

      return rootEntity;
   }

   private static void addMappedSuperclasses(Index index, ClassInfo info, List classInfoList) {
      ClassInfo tmpInfo;
      for(DotName superName = info.superName(); !OBJECT.equals(superName); superName = tmpInfo.superName()) {
         tmpInfo = index.getClassByName(superName);
         if (isMappedSuperclass(tmpInfo)) {
            classInfoList.add(tmpInfo);
         }
      }

   }

   private static void processHierarchy(AnnotationBindingContext bindingContext, ClassInfo classInfo, List rootClassWithAllSubclasses, List processedEntities, Map classToDirectSubclassMap) {
      processedEntities.add(classInfo.name());
      rootClassWithAllSubclasses.add(classInfo);
      List<ClassInfo> subClasses = bindingContext.getIndex().getKnownDirectSubclasses(classInfo.name());
      if (subClasses.isEmpty()) {
         bindingContext.resolveAllTypes(classInfo.name().toString());
      }

      for(ClassInfo subClassInfo : subClasses) {
         addSubClassToSubclassMap(classInfo.name(), subClassInfo, classToDirectSubclassMap);
         processHierarchy(bindingContext, subClassInfo, rootClassWithAllSubclasses, processedEntities, classToDirectSubclassMap);
      }

   }

   private static void addSubClassToSubclassMap(DotName name, ClassInfo subClassInfo, Map classToDirectSubclassMap) {
      if (classToDirectSubclassMap.containsKey(name)) {
         ((List)classToDirectSubclassMap.get(name)).add(subClassInfo);
      } else {
         List<ClassInfo> subclassList = new ArrayList();
         subclassList.add(subClassInfo);
         classToDirectSubclassMap.put(name, subclassList);
      }

   }

   private static boolean isEntityClass(ClassInfo info) {
      if (info == null) {
         return false;
      } else {
         AnnotationInstance jpaEntityAnnotation = JandexHelper.getSingleAnnotation(info, JPADotNames.ENTITY);
         if (jpaEntityAnnotation == null) {
            return false;
         } else {
            AnnotationInstance mappedSuperClassAnnotation = JandexHelper.getSingleAnnotation(info, JPADotNames.MAPPED_SUPERCLASS);
            String className = info.toString();
            assertNotEntityAndMappedSuperClass(jpaEntityAnnotation, mappedSuperClassAnnotation, className);
            AnnotationInstance embeddableAnnotation = JandexHelper.getSingleAnnotation(info, JPADotNames.EMBEDDABLE);
            assertNotEntityAndEmbeddable(jpaEntityAnnotation, embeddableAnnotation, className);
            return true;
         }
      }
   }

   private static boolean isMappedSuperclass(ClassInfo info) {
      if (info == null) {
         return false;
      } else {
         AnnotationInstance mappedSuperclassAnnotation = JandexHelper.getSingleAnnotation(info, JPADotNames.MAPPED_SUPERCLASS);
         return mappedSuperclassAnnotation != null;
      }
   }

   private static void assertNotEntityAndMappedSuperClass(AnnotationInstance jpaEntityAnnotation, AnnotationInstance mappedSuperClassAnnotation, String className) {
      if (jpaEntityAnnotation != null && mappedSuperClassAnnotation != null) {
         throw new AnnotationException("An entity cannot be annotated with both @Entity and @MappedSuperclass. " + className + " has both annotations.");
      }
   }

   private static void assertNotEntityAndEmbeddable(AnnotationInstance jpaEntityAnnotation, AnnotationInstance embeddableAnnotation, String className) {
      if (jpaEntityAnnotation != null && embeddableAnnotation != null) {
         throw new AnnotationException("An entity cannot be annotated with both @Entity and @Embeddable. " + className + " has both annotations.");
      }
   }

   private static AccessType determineDefaultAccessType(List classes) {
      AccessType accessTypeByEmbeddedIdPlacement = null;
      AccessType accessTypeByIdPlacement = null;

      for(ClassInfo info : classes) {
         List<AnnotationInstance> idAnnotations = (List)info.annotations().get(JPADotNames.ID);
         List<AnnotationInstance> embeddedIdAnnotations = (List)info.annotations().get(JPADotNames.EMBEDDED_ID);
         if (CollectionHelper.isNotEmpty((Collection)embeddedIdAnnotations)) {
            accessTypeByEmbeddedIdPlacement = determineAccessTypeByIdPlacement(embeddedIdAnnotations);
         }

         if (CollectionHelper.isNotEmpty((Collection)idAnnotations)) {
            accessTypeByIdPlacement = determineAccessTypeByIdPlacement(idAnnotations);
         }
      }

      if (accessTypeByEmbeddedIdPlacement != null) {
         return accessTypeByEmbeddedIdPlacement;
      } else if (accessTypeByIdPlacement != null) {
         return accessTypeByIdPlacement;
      } else {
         return throwIdNotFoundAnnotationException(classes);
      }
   }

   private static AccessType determineAccessTypeByIdPlacement(List idAnnotations) {
      AccessType accessType = null;

      for(AnnotationInstance annotation : idAnnotations) {
         AccessType tmpAccessType;
         if (annotation.target() instanceof FieldInfo) {
            tmpAccessType = AccessType.FIELD;
         } else {
            if (!(annotation.target() instanceof MethodInfo)) {
               throw new AnnotationException("Invalid placement of @Id annotation");
            }

            tmpAccessType = AccessType.PROPERTY;
         }

         if (accessType == null) {
            accessType = tmpAccessType;
         } else if (!accessType.equals(tmpAccessType)) {
            throw new AnnotationException("Inconsistent placement of @Id annotation within hierarchy ");
         }
      }

      return accessType;
   }

   private static InheritanceType determineInheritanceType(ClassInfo rootClassInfo, List classes) {
      if (classes.size() == 1) {
         return InheritanceType.NO_INHERITANCE;
      } else {
         InheritanceType inheritanceType = InheritanceType.SINGLE_TABLE;
         AnnotationInstance inheritanceAnnotation = JandexHelper.getSingleAnnotation(rootClassInfo, JPADotNames.INHERITANCE);
         if (inheritanceAnnotation != null) {
            String enumName = inheritanceAnnotation.value("strategy").asEnum();
            javax.persistence.InheritanceType jpaInheritanceType = (javax.persistence.InheritanceType)Enum.valueOf(javax.persistence.InheritanceType.class, enumName);
            inheritanceType = InheritanceType.get(jpaInheritanceType);
         }

         for(ClassInfo info : classes) {
            if (!rootClassInfo.equals(info)) {
               inheritanceAnnotation = JandexHelper.getSingleAnnotation(info, JPADotNames.INHERITANCE);
               if (inheritanceAnnotation != null) {
                  throw new AnnotationException(String.format("The inheritance type for %s must be specified on the root entity %s", hierarchyListString(classes), rootClassInfo.name().toString()));
               }
            }
         }

         return inheritanceType;
      }
   }

   private static AccessType throwIdNotFoundAnnotationException(List classes) {
      StringBuilder builder = new StringBuilder();
      builder.append("Unable to determine identifier attribute for class hierarchy consisting of the classe(s) ");
      builder.append(hierarchyListString(classes));
      throw new AnnotationException(builder.toString());
   }

   private static String hierarchyListString(List classes) {
      StringBuilder builder = new StringBuilder();
      builder.append("[");
      int count = 0;

      for(ClassInfo info : classes) {
         builder.append(info.name().toString());
         if (count < classes.size() - 1) {
            builder.append(", ");
         }

         ++count;
      }

      builder.append("]");
      return builder.toString();
   }
}
