package org.hibernate.metamodel.source.annotations.entity;

import javax.persistence.AccessType;
import org.hibernate.metamodel.source.annotations.AnnotationBindingContext;
import org.hibernate.metamodel.source.annotations.HibernateDotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.ClassInfo;

public class EmbeddableClass extends ConfiguredClass {
   private final String embeddedAttributeName;
   private final String parentReferencingAttributeName;

   public EmbeddableClass(ClassInfo classInfo, String embeddedAttributeName, ConfiguredClass parent, AccessType defaultAccessType, AnnotationBindingContext context) {
      super(classInfo, defaultAccessType, parent, context);
      this.embeddedAttributeName = embeddedAttributeName;
      this.parentReferencingAttributeName = this.checkParentAnnotation();
   }

   private String checkParentAnnotation() {
      AnnotationInstance parentAnnotation = JandexHelper.getSingleAnnotation(this.getClassInfo(), HibernateDotNames.PARENT);
      return parentAnnotation == null ? null : JandexHelper.getPropertyName(parentAnnotation.target());
   }

   public String getEmbeddedAttributeName() {
      return this.embeddedAttributeName;
   }

   public String getParentReferencingAttributeName() {
      return this.parentReferencingAttributeName;
   }
}
