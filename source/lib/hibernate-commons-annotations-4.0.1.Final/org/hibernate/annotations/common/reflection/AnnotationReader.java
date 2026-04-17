package org.hibernate.annotations.common.reflection;

import java.lang.annotation.Annotation;

public interface AnnotationReader {
   Annotation getAnnotation(Class var1);

   boolean isAnnotationPresent(Class var1);

   Annotation[] getAnnotations();
}
