package org.hibernate.metamodel.source.binder;

import org.hibernate.mapping.PropertyGeneration;

public interface SingularAttributeSource extends AttributeSource, RelationalValueSourceContainer {
   boolean isVirtualAttribute();

   SingularAttributeNature getNature();

   boolean isInsertable();

   boolean isUpdatable();

   PropertyGeneration getGeneration();

   boolean isLazy();
}
