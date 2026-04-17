package org.hibernate.metamodel.binding;

import org.hibernate.mapping.PropertyGeneration;
import org.hibernate.metamodel.relational.Value;

public interface SingularAttributeBinding extends AttributeBinding {
   Value getValue();

   int getSimpleValueSpan();

   Iterable getSimpleValueBindings();

   void setSimpleValueBindings(Iterable var1);

   boolean hasDerivedValue();

   boolean isNullable();

   PropertyGeneration getGeneration();
}
