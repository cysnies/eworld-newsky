package org.hibernate.metamodel.domain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public enum PluralAttributeNature {
   BAG("bag", Collection.class),
   IDBAG("idbag", Collection.class),
   SET("set", Set.class),
   LIST("list", List.class),
   MAP("map", Map.class);

   private final String name;
   private final Class javaContract;
   private final boolean indexed;

   private PluralAttributeNature(String name, Class javaContract) {
      this.name = name;
      this.javaContract = javaContract;
      this.indexed = Map.class.isAssignableFrom(javaContract) || List.class.isAssignableFrom(javaContract);
   }

   public String getName() {
      return this.name;
   }

   public Class getJavaContract() {
      return this.javaContract;
   }

   public boolean isIndexed() {
      return this.indexed;
   }
}
