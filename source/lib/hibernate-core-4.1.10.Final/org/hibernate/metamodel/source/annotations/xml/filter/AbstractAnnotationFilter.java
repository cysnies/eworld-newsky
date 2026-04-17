package org.hibernate.metamodel.source.annotations.xml.filter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.metamodel.source.annotations.xml.mocker.IndexBuilder;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;

abstract class AbstractAnnotationFilter implements IndexedAnnotationFilter {
   protected static final DotName[] EMPTY_DOTNAME_ARRAY = new DotName[0];
   private Set candidates;

   private boolean match(DotName annName) {
      if (this.candidates == null) {
         this.candidates = new HashSet();
         this.candidates.addAll(Arrays.asList(this.targetAnnotation()));
      }

      return this.candidates.contains(annName);
   }

   public void beforePush(IndexBuilder indexBuilder, DotName classDotName, AnnotationInstance annotationInstance) {
      DotName annName = annotationInstance.name();
      if (this.match(annName)) {
         Map<DotName, List<AnnotationInstance>> map = indexBuilder.getIndexedAnnotations(classDotName);
         this.overrideIndexedAnnotationMap(annName, annotationInstance, map);
      }
   }

   protected void overrideIndexedAnnotationMap(DotName annName, AnnotationInstance annotationInstance, Map map) {
      if (map.containsKey(annName)) {
         List<AnnotationInstance> indexedAnnotationInstanceList = (List)map.get(annName);
         if (!indexedAnnotationInstanceList.isEmpty()) {
            this.process(annName, annotationInstance, indexedAnnotationInstanceList);
         }
      }
   }

   protected void process(DotName annName, AnnotationInstance annotationInstance, List indexedAnnotationInstanceList) {
   }

   protected DotName[] targetAnnotation() {
      return EMPTY_DOTNAME_ARRAY;
   }
}
