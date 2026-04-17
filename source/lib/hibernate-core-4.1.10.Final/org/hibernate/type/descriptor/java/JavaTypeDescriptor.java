package org.hibernate.type.descriptor.java;

import java.io.Serializable;
import java.util.Comparator;
import org.hibernate.type.descriptor.WrapperOptions;

public interface JavaTypeDescriptor extends Serializable {
   Class getJavaTypeClass();

   MutabilityPlan getMutabilityPlan();

   Comparator getComparator();

   int extractHashCode(Object var1);

   boolean areEqual(Object var1, Object var2);

   String extractLoggableRepresentation(Object var1);

   String toString(Object var1);

   Object fromString(String var1);

   Object unwrap(Object var1, Class var2, WrapperOptions var3);

   Object wrap(Object var1, WrapperOptions var2);
}
