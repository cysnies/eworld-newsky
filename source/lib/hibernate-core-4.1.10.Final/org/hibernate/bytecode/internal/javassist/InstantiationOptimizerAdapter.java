package org.hibernate.bytecode.internal.javassist;

import java.io.Serializable;
import org.hibernate.InstantiationException;
import org.hibernate.bytecode.spi.ReflectionOptimizer;

public class InstantiationOptimizerAdapter implements ReflectionOptimizer.InstantiationOptimizer, Serializable {
   private final FastClass fastClass;

   public InstantiationOptimizerAdapter(FastClass fastClass) {
      this.fastClass = fastClass;
   }

   public Object newInstance() {
      try {
         return this.fastClass.newInstance();
      } catch (Throwable t) {
         throw new InstantiationException("Could not instantiate entity with Javassist optimizer: ", this.fastClass.getJavaClass(), t);
      }
   }
}
