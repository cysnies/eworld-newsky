package org.hibernate.dialect;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ResultColumnReferenceStrategy implements Serializable {
   private static final Map INSTANCES = new HashMap();
   public static final ResultColumnReferenceStrategy SOURCE = new ResultColumnReferenceStrategy("source");
   public static final ResultColumnReferenceStrategy ALIAS = new ResultColumnReferenceStrategy("alias");
   public static final ResultColumnReferenceStrategy ORDINAL = new ResultColumnReferenceStrategy("ordinal");
   private final String name;

   public ResultColumnReferenceStrategy(String name) {
      this.name = name;
   }

   public String toString() {
      return this.name;
   }

   private Object readResolve() throws ObjectStreamException {
      return parse(this.name);
   }

   public static ResultColumnReferenceStrategy parse(String name) {
      return (ResultColumnReferenceStrategy)INSTANCES.get(name);
   }

   static {
      INSTANCES.put(SOURCE.name, SOURCE);
      INSTANCES.put(ALIAS.name, ALIAS);
      INSTANCES.put(ORDINAL.name, ORDINAL);
   }
}
