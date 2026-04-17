package org.hibernate.tuple;

import java.io.Serializable;
import org.hibernate.type.Type;

public abstract class Property implements Serializable {
   private String name;
   private String node;
   private Type type;

   protected Property(String name, String node, Type type) {
      this.name = name;
      this.node = node;
      this.type = type;
   }

   public String getName() {
      return this.name;
   }

   public String getNode() {
      return this.node;
   }

   public Type getType() {
      return this.type;
   }

   public String toString() {
      return "Property(" + this.name + ':' + this.type.getName() + ')';
   }
}
