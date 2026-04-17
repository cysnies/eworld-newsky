package org.hibernate.annotations.common.reflection;

import java.lang.annotation.Annotation;

public interface XAnnotatedElement {
   Annotation getAnnotation(Class var1);

   boolean isAnnotationPresent(Class var1);

   Annotation[] getAnnotations();

   boolean equals(Object var1);
}
