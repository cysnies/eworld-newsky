package org.hibernate.tuple;

import org.hibernate.property.Getter;

public interface Tuplizer {
   Object[] getPropertyValues(Object var1);

   void setPropertyValues(Object var1, Object[] var2);

   Object getPropertyValue(Object var1, int var2);

   Object instantiate();

   boolean isInstance(Object var1);

   Class getMappedClass();

   Getter getGetter(int var1);
}
