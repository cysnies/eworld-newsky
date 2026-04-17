package org.hibernate.metamodel.source.binder;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum PluralAttributeNature {
   BAG(Collection.class),
   ID_BAG(Collection.class),
   SET(Set.class),
   LIST(List.class),
   MAP(Map.class);

   private final Class reportedJavaType;

   private PluralAttributeNature(Class reportedJavaType) {
      this.reportedJavaType = reportedJavaType;
   }

   public Class reportedJavaType() {
      return this.reportedJavaType;
   }
}
