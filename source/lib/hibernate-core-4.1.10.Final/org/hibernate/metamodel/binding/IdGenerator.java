package org.hibernate.metamodel.binding;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import org.hibernate.internal.util.collections.CollectionHelper;

public class IdGenerator implements Serializable {
   private final String name;
   private final String strategy;
   private final Map parameters;

   public IdGenerator(String name, String strategy, Map parameters) {
      this.name = name;
      this.strategy = strategy;
      if (CollectionHelper.isEmpty(parameters)) {
         this.parameters = Collections.emptyMap();
      } else {
         this.parameters = Collections.unmodifiableMap(parameters);
      }

   }

   public String getStrategy() {
      return this.strategy;
   }

   public String getName() {
      return this.name;
   }

   public Map getParameters() {
      return this.parameters;
   }
}
