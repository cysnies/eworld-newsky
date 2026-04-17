package org.hibernate.metamodel.source.binder;

import org.hibernate.internal.util.ValueHolder;

public interface CompositePluralAttributeElementSource extends PluralAttributeElementSource, AttributeSourceContainer {
   String getClassName();

   ValueHolder getClassReference();

   String getParentReferenceAttributeName();

   String getExplicitTuplizerClassName();
}
