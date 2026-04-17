package org.hibernate.type;

import java.util.LinkedHashMap;

public class OrderedMapType extends MapType {
   /** @deprecated */
   @Deprecated
   public OrderedMapType(TypeFactory.TypeScope typeScope, String role, String propertyRef, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
   }

   public OrderedMapType(TypeFactory.TypeScope typeScope, String role, String propertyRef) {
      super(typeScope, role, propertyRef);
   }

   public Object instantiate(int anticipatedSize) {
      return anticipatedSize > 0 ? new LinkedHashMap(anticipatedSize) : new LinkedHashMap();
   }
}
