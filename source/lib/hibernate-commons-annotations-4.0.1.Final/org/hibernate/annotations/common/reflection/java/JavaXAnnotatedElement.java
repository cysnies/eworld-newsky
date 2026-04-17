package org.hibernate.annotations.common.reflection.java;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import org.hibernate.annotations.common.reflection.AnnotationReader;
import org.hibernate.annotations.common.reflection.XAnnotatedElement;

abstract class JavaXAnnotatedElement implements XAnnotatedElement {
   private final JavaReflectionManager factory;
   private final AnnotatedElement annotatedElement;

   public JavaXAnnotatedElement(AnnotatedElement annotatedElement, JavaReflectionManager factory) {
      this.factory = factory;
      this.annotatedElement = annotatedElement;
   }

   protected JavaReflectionManager getFactory() {
      return this.factory;
   }

   private AnnotationReader getAnnotationReader() {
      return this.factory.buildAnnotationReader(this.annotatedElement);
   }

   public Annotation getAnnotation(Class annotationType) {
      return this.getAnnotationReader().getAnnotation(annotationType);
   }

   public boolean isAnnotationPresent(Class annotationType) {
      return this.getAnnotationReader().isAnnotationPresent(annotationType);
   }

   public Annotation[] getAnnotations() {
      return this.getAnnotationReader().getAnnotations();
   }

   AnnotatedElement toAnnotatedElement() {
      return this.annotatedElement;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof JavaXAnnotatedElement)) {
         return false;
      } else {
         JavaXAnnotatedElement other = (JavaXAnnotatedElement)obj;
         return this.annotatedElement.equals(other.toAnnotatedElement());
      }
   }

   public int hashCode() {
      return this.annotatedElement.hashCode();
   }

   public String toString() {
      return this.annotatedElement.toString();
   }
}
