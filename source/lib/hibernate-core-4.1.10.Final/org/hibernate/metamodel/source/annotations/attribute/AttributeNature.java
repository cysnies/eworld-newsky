package org.hibernate.metamodel.source.annotations.attribute;

import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.jboss.jandex.DotName;

public enum AttributeNature {
   BASIC(JPADotNames.BASIC),
   ONE_TO_ONE(JPADotNames.ONE_TO_ONE),
   ONE_TO_MANY(JPADotNames.ONE_TO_MANY),
   MANY_TO_ONE(JPADotNames.MANY_TO_ONE),
   MANY_TO_MANY(JPADotNames.MANY_TO_MANY),
   ELEMENT_COLLECTION(JPADotNames.ELEMENT_COLLECTION),
   EMBEDDED_ID(JPADotNames.EMBEDDED_ID),
   EMBEDDED(JPADotNames.EMBEDDED);

   private final DotName annotationDotName;

   private AttributeNature(DotName annotationDotName) {
      this.annotationDotName = annotationDotName;
   }

   public DotName getAnnotationDotName() {
      return this.annotationDotName;
   }
}
