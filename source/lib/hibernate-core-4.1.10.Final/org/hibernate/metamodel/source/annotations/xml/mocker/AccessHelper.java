package org.hibernate.metamodel.source.annotations.xml.mocker;

import java.util.List;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.MappingException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.jaxb.mapping.orm.JaxbAccessType;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.hibernate.metamodel.source.annotations.xml.PseudoJpaDotNames;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;
import org.jboss.jandex.MethodInfo;
import org.jboss.logging.Logger;

class AccessHelper implements JPADotNames {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AccessHelper.class.getName());

   static JaxbAccessType getAccessFromDefault(IndexBuilder indexBuilder) {
      AnnotationInstance annotationInstance = JandexHelper.getSingleAnnotation(indexBuilder.getAnnotations(), PseudoJpaDotNames.DEFAULT_ACCESS);
      return annotationInstance == null ? null : (JaxbAccessType)JandexHelper.getEnumValue(annotationInstance, "value", JaxbAccessType.class);
   }

   static JaxbAccessType getAccessFromIdPosition(DotName className, IndexBuilder indexBuilder) {
      Map<DotName, List<AnnotationInstance>> indexedAnnotations = indexBuilder.getIndexedAnnotations(className);
      Map<DotName, List<AnnotationInstance>> ormAnnotations = indexBuilder.getClassInfoAnnotationsMap(className);
      JaxbAccessType accessType = getAccessFromIdPosition(ormAnnotations);
      if (accessType == null) {
         accessType = getAccessFromIdPosition(indexedAnnotations);
      }

      if (accessType == null) {
         ClassInfo parent = indexBuilder.getClassInfo(className);
         if (parent == null) {
            parent = indexBuilder.getIndexedClassInfo(className);
         }

         if (parent != null) {
            DotName parentClassName = parent.superName();
            accessType = getAccessFromIdPosition(parentClassName, indexBuilder);
         }
      }

      return accessType;
   }

   private static JaxbAccessType getAccessFromIdPosition(Map annotations) {
      if (annotations != null && !annotations.isEmpty() && annotations.containsKey(ID)) {
         List<AnnotationInstance> idAnnotationInstances = (List)annotations.get(ID);
         return MockHelper.isNotEmpty(idAnnotationInstances) ? processIdAnnotations(idAnnotationInstances) : null;
      } else {
         return null;
      }
   }

   private static JaxbAccessType processIdAnnotations(List idAnnotations) {
      JaxbAccessType accessType = null;

      for(AnnotationInstance annotation : idAnnotations) {
         AnnotationTarget tmpTarget = annotation.target();
         if (tmpTarget == null) {
            throw new AssertionFailure("@Id has no AnnotationTarget, this is mostly a internal error.");
         }

         if (accessType == null) {
            accessType = annotationTargetToAccessType(tmpTarget);
         } else if (!accessType.equals(annotationTargetToAccessType(tmpTarget))) {
            throw new MappingException("Inconsistent placement of @Id annotation within hierarchy ");
         }
      }

      return accessType;
   }

   static JaxbAccessType annotationTargetToAccessType(AnnotationTarget target) {
      return target instanceof MethodInfo ? JaxbAccessType.PROPERTY : JaxbAccessType.FIELD;
   }

   static JaxbAccessType getEntityAccess(DotName className, IndexBuilder indexBuilder) {
      Map<DotName, List<AnnotationInstance>> indexedAnnotations = indexBuilder.getIndexedAnnotations(className);
      Map<DotName, List<AnnotationInstance>> ormAnnotations = indexBuilder.getClassInfoAnnotationsMap(className);
      JaxbAccessType accessType = getAccess(ormAnnotations);
      if (accessType == null) {
         accessType = getAccess(indexedAnnotations);
      }

      if (accessType == null) {
         ClassInfo parent = indexBuilder.getClassInfo(className);
         if (parent == null) {
            parent = indexBuilder.getIndexedClassInfo(className);
         }

         if (parent != null) {
            DotName parentClassName = parent.superName();
            accessType = getEntityAccess(parentClassName, indexBuilder);
         }
      }

      return accessType;
   }

   private static JaxbAccessType getAccess(Map annotations) {
      if (annotations != null && !annotations.isEmpty() && isEntityObject(annotations)) {
         List<AnnotationInstance> accessAnnotationInstances = (List)annotations.get(JPADotNames.ACCESS);
         if (MockHelper.isNotEmpty(accessAnnotationInstances)) {
            for(AnnotationInstance annotationInstance : accessAnnotationInstances) {
               if (annotationInstance.target() != null && annotationInstance.target() instanceof ClassInfo) {
                  return (JaxbAccessType)JandexHelper.getEnumValue(annotationInstance, "value", JaxbAccessType.class);
               }
            }
         }

         return null;
      } else {
         return null;
      }
   }

   private static boolean isEntityObject(Map annotations) {
      return annotations.containsKey(ENTITY) || annotations.containsKey(MAPPED_SUPERCLASS) || annotations.containsKey(EMBEDDABLE);
   }

   static JaxbAccessType getAccessFromAttributeAnnotation(DotName className, String attributeName, IndexBuilder indexBuilder) {
      Map<DotName, List<AnnotationInstance>> indexedAnnotations = indexBuilder.getIndexedAnnotations(className);
      if (indexedAnnotations != null && indexedAnnotations.containsKey(ACCESS)) {
         List<AnnotationInstance> annotationInstances = (List)indexedAnnotations.get(ACCESS);
         if (MockHelper.isNotEmpty(annotationInstances)) {
            for(AnnotationInstance annotationInstance : annotationInstances) {
               AnnotationTarget indexedPropertyTarget = annotationInstance.target();
               if (indexedPropertyTarget != null && JandexHelper.getPropertyName(indexedPropertyTarget).equals(attributeName)) {
                  JaxbAccessType accessType = (JaxbAccessType)JandexHelper.getEnumValue(annotationInstance, "value", JaxbAccessType.class);
                  JaxbAccessType targetAccessType = annotationTargetToAccessType(indexedPropertyTarget);
                  if (accessType.equals(targetAccessType)) {
                     return targetAccessType;
                  }

                  LOG.warn(String.format("%s.%s has @Access on %s, but it tries to assign the access type to %s, this is not allowed by JPA spec, and will be ignored.", className, attributeName, targetAccessType, accessType));
               }
            }
         }
      }

      return null;
   }
}
