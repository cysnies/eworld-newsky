package org.hibernate.bytecode.internal.javassist;

import java.io.Serializable;
import org.hibernate.bytecode.spi.ReflectionOptimizer;

public class ReflectionOptimizerImpl implements ReflectionOptimizer, Serializable {
   private final ReflectionOptimizer.InstantiationOptimizer instantiationOptimizer;
   private final ReflectionOptimizer.AccessOptimizer accessOptimizer;

   public ReflectionOptimizerImpl(ReflectionOptimizer.InstantiationOptimizer instantiationOptimizer, ReflectionOptimizer.AccessOptimizer accessOptimizer) {
      this.instantiationOptimizer = instantiationOptimizer;
      this.accessOptimizer = accessOptimizer;
   }

   public ReflectionOptimizer.InstantiationOptimizer getInstantiationOptimizer() {
      return this.instantiationOptimizer;
   }

   public ReflectionOptimizer.AccessOptimizer getAccessOptimizer() {
      return this.accessOptimizer;
   }
}
