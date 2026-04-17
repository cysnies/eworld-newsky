package org.hibernate.annotations.common.reflection;

import java.lang.reflect.AnnotatedElement;
import java.util.Map;

public interface MetadataProvider {
   Map getDefaults();

   AnnotationReader getAnnotationReader(AnnotatedElement var1);
}
