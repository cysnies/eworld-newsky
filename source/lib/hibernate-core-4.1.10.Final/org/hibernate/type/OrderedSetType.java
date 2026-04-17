package org.hibernate.type;

import java.util.LinkedHashSet;

public class OrderedSetType extends SetType {
   /** @deprecated */
   @Deprecated
   public OrderedSetType(TypeFactory.TypeScope typeScope, String role, String propertyRef, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
   }

   public OrderedSetType(TypeFactory.TypeScope typeScope, String role, String propertyRef) {
      super(typeScope, role, propertyRef);
   }

   public Object instantiate(int anticipatedSize) {
      return anticipatedSize > 0 ? new LinkedHashSet(anticipatedSize) : new LinkedHashSet();
   }
}
