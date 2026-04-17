package org.hibernate.metamodel.source.annotations.attribute.type;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.hibernate.AssertionFailure;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.CollectionHelper;

public class CompositeAttributeTypeResolver implements AttributeTypeResolver {
   private List resolvers = new ArrayList();
   private final AttributeTypeResolverImpl explicitHibernateTypeResolver;

   public CompositeAttributeTypeResolver(AttributeTypeResolverImpl explicitHibernateTypeResolver) {
      if (explicitHibernateTypeResolver == null) {
         throw new AssertionFailure("The Given AttributeTypeResolver is null.");
      } else {
         this.explicitHibernateTypeResolver = explicitHibernateTypeResolver;
      }
   }

   public void addHibernateTypeResolver(AttributeTypeResolver resolver) {
      if (resolver == null) {
         throw new AssertionFailure("The Given AttributeTypeResolver is null.");
      } else {
         this.resolvers.add(resolver);
      }
   }

   public String getExplicitHibernateTypeName() {
      String type = this.explicitHibernateTypeResolver.getExplicitHibernateTypeName();
      if (StringHelper.isEmpty(type)) {
         for(AttributeTypeResolver resolver : this.resolvers) {
            type = resolver.getExplicitHibernateTypeName();
            if (StringHelper.isNotEmpty(type)) {
               break;
            }
         }
      }

      return type;
   }

   public Map getExplicitHibernateTypeParameters() {
      Map<String, String> parameters = this.explicitHibernateTypeResolver.getExplicitHibernateTypeParameters();
      if (CollectionHelper.isEmpty(parameters)) {
         for(AttributeTypeResolver resolver : this.resolvers) {
            parameters = resolver.getExplicitHibernateTypeParameters();
            if (CollectionHelper.isNotEmpty(parameters)) {
               break;
            }
         }
      }

      return parameters;
   }
}
