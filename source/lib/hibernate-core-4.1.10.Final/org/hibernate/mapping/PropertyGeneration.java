package org.hibernate.mapping;

import java.io.Serializable;

public class PropertyGeneration implements Serializable {
   public static final PropertyGeneration NEVER = new PropertyGeneration("never");
   public static final PropertyGeneration INSERT = new PropertyGeneration("insert");
   public static final PropertyGeneration ALWAYS = new PropertyGeneration("always");
   private final String name;

   private PropertyGeneration(String name) {
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public static PropertyGeneration parse(String name) {
      if ("insert".equalsIgnoreCase(name)) {
         return INSERT;
      } else {
         return "always".equalsIgnoreCase(name) ? ALWAYS : NEVER;
      }
   }

   private Object readResolve() {
      return parse(this.name);
   }

   public String toString() {
      return this.getClass().getName() + "(" + this.getName() + ")";
   }
}
