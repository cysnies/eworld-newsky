package org.hibernate.metamodel.source;

import org.hibernate.internal.jaxb.Origin;

public interface LocalBindingContext extends BindingContext {
   Origin getOrigin();
}
