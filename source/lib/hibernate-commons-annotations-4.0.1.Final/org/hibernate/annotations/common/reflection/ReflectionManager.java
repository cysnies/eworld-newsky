package org.hibernate.annotations.common.reflection;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Map;

public interface ReflectionManager {
   XClass toXClass(Class var1);

   Class toClass(XClass var1);

   Method toMethod(XMethod var1);

   XClass classForName(String var1, Class var2) throws ClassNotFoundException;

   XPackage packageForName(String var1) throws ClassNotFoundException;

   boolean equals(XClass var1, Class var2);

   AnnotationReader buildAnnotationReader(AnnotatedElement var1);

   Map getDefaults();
}
