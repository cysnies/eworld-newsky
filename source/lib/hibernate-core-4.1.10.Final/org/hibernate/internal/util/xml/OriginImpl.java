package org.hibernate.internal.util.xml;

import java.io.Serializable;

public class OriginImpl implements Origin, Serializable {
   private final String type;
   private final String name;

   public OriginImpl(String type, String name) {
      this.type = type;
      this.name = name;
   }

   public String getType() {
      return this.type;
   }

   public String getName() {
      return this.name;
   }
}
