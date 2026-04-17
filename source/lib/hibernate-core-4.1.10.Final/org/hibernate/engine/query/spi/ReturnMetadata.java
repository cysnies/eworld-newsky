package org.hibernate.engine.query.spi;

import java.io.Serializable;
import org.hibernate.type.Type;

public class ReturnMetadata implements Serializable {
   private final String[] returnAliases;
   private final Type[] returnTypes;

   public ReturnMetadata(String[] returnAliases, Type[] returnTypes) {
      this.returnAliases = returnAliases;
      this.returnTypes = returnTypes;
   }

   public String[] getReturnAliases() {
      return this.returnAliases;
   }

   public Type[] getReturnTypes() {
      return this.returnTypes;
   }
}
