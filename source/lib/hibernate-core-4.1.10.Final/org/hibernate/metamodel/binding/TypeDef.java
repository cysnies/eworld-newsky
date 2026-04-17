package org.hibernate.metamodel.binding;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

public class TypeDef implements Serializable {
   private final String name;
   private final String typeClass;
   private final Map parameters;

   public TypeDef(String name, String typeClass, Map parameters) {
      this.name = name;
      this.typeClass = typeClass;
      this.parameters = parameters;
   }

   public String getName() {
      return this.name;
   }

   public String getTypeClass() {
      return this.typeClass;
   }

   public Map getParameters() {
      return Collections.unmodifiableMap(this.parameters);
   }
}
