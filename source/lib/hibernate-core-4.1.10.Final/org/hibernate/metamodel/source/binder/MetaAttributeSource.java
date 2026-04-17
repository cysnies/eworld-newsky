package org.hibernate.metamodel.source.binder;

public interface MetaAttributeSource {
   String getName();

   String getValue();

   boolean isInheritable();
}
