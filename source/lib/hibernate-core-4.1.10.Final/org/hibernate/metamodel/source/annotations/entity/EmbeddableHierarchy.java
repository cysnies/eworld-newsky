package org.hibernate.metamodel.source.annotations.entity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.persistence.AccessType;
import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.ClassInfo;
import org.jboss.jandex.DotName;

public class EmbeddableHierarchy implements Iterable {
   private final AccessType defaultAccessType;
   private final List embeddables;

   public static EmbeddableHierarchy createEmbeddableHierarchy(Class embeddableClass, String propertyName, AccessType accessType, AnnotationBindingContext context) {
      ClassInfo embeddableClassInfo = context.getClassInfo(embeddableClass.getName());
      if (embeddableClassInfo == null) {
         throw new AssertionFailure(String.format("The specified class %s cannot be found in the annotation index", embeddableClass.getName()));
      } else if (JandexHelper.getSingleAnnotation(embeddableClassInfo, JPADotNames.EMBEDDABLE) == null) {
         throw new AssertionFailure(String.format("The specified class %s is not annotated with @Embeddable even though it is as embeddable", embeddableClass.getName()));
      } else {
         List<ClassInfo> classInfoList = new ArrayList();
         Class<?> clazz = embeddableClass;

         while(clazz != null && !clazz.equals(Object.class)) {
            ClassInfo tmpClassInfo = context.getIndex().getClassByName(DotName.createSimple(clazz.getName()));
            clazz = clazz.getSuperclass();
            if (tmpClassInfo != null) {
               classInfoList.add(0, tmpClassInfo);
            }
         }

         return new EmbeddableHierarchy(classInfoList, propertyName, context, accessType);
      }
   }

   private EmbeddableHierarchy(List classInfoList, String propertyName, AnnotationBindingContext context, AccessType defaultAccessType) {
      this.defaultAccessType = defaultAccessType;
      context.resolveAllTypes(((ClassInfo)classInfoList.get(classInfoList.size() - 1)).name().toString());
      this.embeddables = new ArrayList();
      ConfiguredClass parent = null;

      for(ClassInfo info : classInfoList) {
         EmbeddableClass embeddable = new EmbeddableClass(info, propertyName, parent, defaultAccessType, context);
         this.embeddables.add(embeddable);
         parent = embeddable;
      }

   }

   public AccessType getDefaultAccessType() {
      return this.defaultAccessType;
   }

   public Iterator iterator() {
      return this.embeddables.iterator();
   }

   public EmbeddableClass getLeaf() {
      return (EmbeddableClass)this.embeddables.get(this.embeddables.size() - 1);
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("EmbeddableHierarchy");
      sb.append("{defaultAccessType=").append(this.defaultAccessType);
      sb.append(", embeddables=").append(this.embeddables);
      sb.append('}');
      return sb.toString();
   }
}
