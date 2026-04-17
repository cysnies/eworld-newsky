package org.hibernate.metamodel.source.binder;

import java.util.List;

public interface BasicPluralAttributeElementSource extends PluralAttributeElementSource {
   List getValueSources();

   ExplicitHibernateTypeSource getExplicitHibernateTypeSource();
}
