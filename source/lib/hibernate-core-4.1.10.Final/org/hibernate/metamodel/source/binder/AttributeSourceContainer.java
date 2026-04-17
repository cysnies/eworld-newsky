package org.hibernate.metamodel.source.binder;

import org.hibernate.metamodel.source.LocalBindingContext;

public interface AttributeSourceContainer {
   String getPath();

   Iterable attributeSources();

   LocalBindingContext getLocalBindingContext();
}
