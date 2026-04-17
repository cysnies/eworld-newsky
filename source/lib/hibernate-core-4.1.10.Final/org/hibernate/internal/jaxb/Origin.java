package org.hibernate.internal.jaxb;

import java.io.Serializable;

public class Origin implements Serializable {
   private final SourceType type;
   private final String name;

   public Origin(SourceType type, String name) {
      this.type = type;
      this.name = name;
   }

   public SourceType getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }
}
