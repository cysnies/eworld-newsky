package org.hibernate.annotations.common.reflection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import org.hibernate.annotations.common.reflection.AnnotationReader;

class JavaAnnotationReader implements AnnotationReader {
   protected final AnnotatedElement element;

   public JavaAnnotationReader(AnnotatedElement el) {
      this.element = el;
   }

   public Annotation getAnnotation(Class annotationType) {
      return this.element.getAnnotation(annotationType);
   }

   public boolean isAnnotationPresent(Class annotationType) {
      return this.element.isAnnotationPresent(annotationType);
   }

   public Annotation[] getAnnotations() {
      return this.element.getAnnotations();
   }
}
