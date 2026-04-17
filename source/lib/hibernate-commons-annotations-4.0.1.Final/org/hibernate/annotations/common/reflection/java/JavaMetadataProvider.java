package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Map;
import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.annotations.common.reflection.MetadataProvider;

public class JavaMetadataProvider implements MetadataProvider {
   public Map getDefaults() {
      return Collections.emptyMap();
   }

   public AnnotationReader getAnnotationReader(AnnotatedElement annotatedElement) {
      return new JavaAnnotationReader(annotatedElement);
   }
}
