package org.hibernate.cfg;

import java.util.concurrent.atomic.AtomicInteger;
import org.hibernate.mapping.SimpleValue;
import org.hibernate.mapping.Value;

public abstract class FkSecondPass implements SecondPass {
   protected SimpleValue value;
   protected Ejb3JoinColumn[] columns;
   private int uniqueCounter;
   private static AtomicInteger globalCounter = new AtomicInteger();

   public FkSecondPass(SimpleValue value, Ejb3JoinColumn[] columns) {
      this.value = value;
      this.columns = columns;
      this.uniqueCounter = globalCounter.getAndIncrement();
   }

   public int getUniqueCounter() {
      return this.uniqueCounter;
   }

   public Value getValue() {
      return this.value;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (!(o instanceof FkSecondPass)) {
         return false;
      } else {
         FkSecondPass that = (FkSecondPass)o;
         return this.uniqueCounter == that.uniqueCounter;
      }
   }

   public int hashCode() {
      return this.uniqueCounter;
   }

   public abstract String getReferencedEntityName();

   public abstract boolean isInPrimaryKey();
}
