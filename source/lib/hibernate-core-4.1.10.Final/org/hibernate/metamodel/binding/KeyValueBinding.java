package org.hibernate.metamodel.binding;

public interface KeyValueBinding extends AttributeBinding {
   boolean isKeyCascadeDeleteEnabled();

   String getUnsavedValue();
}
