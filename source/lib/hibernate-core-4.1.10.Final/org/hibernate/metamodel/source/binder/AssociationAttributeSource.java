package org.hibernate.metamodel.source.binder;

import org.hibernate.FetchMode;
import org.hibernate.engine.FetchStyle;
import org.hibernate.engine.FetchTiming;

public interface AssociationAttributeSource extends AttributeSource {
   Iterable getCascadeStyles();

   FetchMode getFetchMode();

   FetchTiming getFetchTiming();

   FetchStyle getFetchStyle();
}
