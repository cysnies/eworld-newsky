package org.hibernate.metamodel.source.annotations.attribute.type;

import java.util.Map;

public interface AttributeTypeResolver {
   String getExplicitHibernateTypeName();

   Map getExplicitHibernateTypeParameters();
}
