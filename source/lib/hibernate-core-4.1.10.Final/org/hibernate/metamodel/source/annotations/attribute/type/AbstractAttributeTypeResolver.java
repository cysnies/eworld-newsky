package org.hibernate.metamodel.source.annotations.attribute.type;

import java.util.Collections;
import java.util.Map;
import org.hibernate.internal.util.StringHelper;
import org.jboss.jandex.AnnotationInstance;

public abstract class AbstractAttributeTypeResolver implements AttributeTypeResolver {
   protected abstract AnnotationInstance getTypeDeterminingAnnotationInstance();

   protected abstract String resolveHibernateTypeName(AnnotationInstance var1);

   protected Map resolveHibernateTypeParameters(AnnotationInstance annotationInstance) {
      return Collections.emptyMap();
   }

   public final String getExplicitHibernateTypeName() {
      return this.resolveHibernateTypeName(this.getTypeDeterminingAnnotationInstance());
   }

   public final Map getExplicitHibernateTypeParameters() {
      return StringHelper.isNotEmpty(this.getExplicitHibernateTypeName()) ? this.resolveHibernateTypeParameters(this.getTypeDeterminingAnnotationInstance()) : Collections.emptyMap();
   }
}
