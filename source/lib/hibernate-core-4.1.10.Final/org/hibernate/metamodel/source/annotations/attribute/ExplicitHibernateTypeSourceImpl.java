package org.hibernate.metamodel.source.annotations.attribute;

import java.util.Map;
import org.hibernate.metamodel.source.annotations.attribute.type.AttributeTypeResolver;
import org.hibernate.metamodel.source.binder.ExplicitHibernateTypeSource;

public class ExplicitHibernateTypeSourceImpl implements ExplicitHibernateTypeSource {
   private final AttributeTypeResolver typeResolver;

   public ExplicitHibernateTypeSourceImpl(AttributeTypeResolver typeResolver) {
      this.typeResolver = typeResolver;
   }

   public String getName() {
      return this.typeResolver.getExplicitHibernateTypeName();
   }

   public Map getParameters() {
      return this.typeResolver.getExplicitHibernateTypeParameters();
   }
}
