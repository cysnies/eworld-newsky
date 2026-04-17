package org.hibernate.metamodel.source.annotations.xml.filter;

import java.util.List;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

class NameAnnotationFilter extends AbstractAnnotationFilter {
   public static NameTargetAnnotationFilter INSTANCE = new NameTargetAnnotationFilter();

   protected void process(DotName annName, AnnotationInstance annotationInstance, List indexedAnnotationInstanceList) {
      indexedAnnotationInstanceList.clear();
   }

   protected DotName[] targetAnnotation() {
      return new DotName[]{CACHEABLE, TABLE, EXCLUDE_DEFAULT_LISTENERS, EXCLUDE_SUPERCLASS_LISTENERS, ID_CLASS, INHERITANCE, DISCRIMINATOR_VALUE, DISCRIMINATOR_COLUMN, ENTITY_LISTENERS};
   }
}
