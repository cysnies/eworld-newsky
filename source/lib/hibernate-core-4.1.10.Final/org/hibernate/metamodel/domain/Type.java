package org.hibernate.metamodel.domain;

import org.hibernate.internal.util.ValueHolder;

public interface Type {
   String getName();

   String getClassName();

   Class getClassReference();

   ValueHolder getClassReferenceUnresolved();

   boolean isAssociation();

   boolean isComponent();
}
