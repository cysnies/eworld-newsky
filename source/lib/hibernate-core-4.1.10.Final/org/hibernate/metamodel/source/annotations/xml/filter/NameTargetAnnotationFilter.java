package org.hibernate.metamodel.source.annotations.xml.filter;

import java.util.Iterator;
import java.util.List;
import org.hibernate.metamodel.source.annotations.xml.mocker.MockHelper;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationTarget;
import org.jboss.jandex.DotName;

class NameTargetAnnotationFilter extends AbstractAnnotationFilter {
   public static NameTargetAnnotationFilter INSTANCE = new NameTargetAnnotationFilter();

   protected void process(DotName annName, AnnotationInstance annotationInstance, List indexedAnnotationInstanceList) {
      AnnotationTarget target = annotationInstance.target();
      Iterator<AnnotationInstance> iter = indexedAnnotationInstanceList.iterator();

      while(iter.hasNext()) {
         AnnotationInstance ann = (AnnotationInstance)iter.next();
         if (MockHelper.targetEquals(target, ann.target())) {
            iter.remove();
         }
      }

   }

   protected DotName[] targetAnnotation() {
      return new DotName[]{LOB, ID, BASIC, GENERATED_VALUE, VERSION, TRANSIENT, ACCESS, POST_LOAD, POST_PERSIST, POST_REMOVE, POST_UPDATE, PRE_PERSIST, PRE_REMOVE, PRE_UPDATE, EMBEDDED_ID, EMBEDDED, MANY_TO_ONE, MANY_TO_MANY, ONE_TO_ONE, ONE_TO_MANY, ELEMENT_COLLECTION, COLLECTION_TABLE, COLUMN, ENUMERATED, JOIN_TABLE, TEMPORAL, ORDER_BY, ORDER_COLUMN, JOIN_COLUMN, JOIN_COLUMNS, MAPS_ID, MAP_KEY_TEMPORAL, MAP_KEY, MAP_KEY_CLASS, MAP_KEY_COLUMN, MAP_KEY_ENUMERATED};
   }
}
