package org.hibernate.annotations.common.reflection;

import java.util.List;

public interface XClass extends XAnnotatedElement {
   String ACCESS_PROPERTY = "property";
   String ACCESS_FIELD = "field";
   Filter DEFAULT_FILTER = new Filter() {
      public boolean returnStatic() {
         return false;
      }

      public boolean returnTransient() {
         return false;
      }
   };

   String getName();

   XClass getSuperclass();

   XClass[] getInterfaces();

   boolean isInterface();

   boolean isAbstract();

   boolean isPrimitive();

   boolean isEnum();

   boolean isAssignableFrom(XClass var1);

   List getDeclaredProperties(String var1);

   List getDeclaredProperties(String var1, Filter var2);

   List getDeclaredMethods();
}
