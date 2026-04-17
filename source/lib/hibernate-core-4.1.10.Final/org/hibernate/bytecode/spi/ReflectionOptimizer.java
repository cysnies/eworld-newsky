package org.hibernate.bytecode.spi;

public interface ReflectionOptimizer {
   InstantiationOptimizer getInstantiationOptimizer();

   AccessOptimizer getAccessOptimizer();

   public interface AccessOptimizer {
      String[] getPropertyNames();

      Object[] getPropertyValues(Object var1);

      void setPropertyValues(Object var1, Object[] var2);
   }

   public interface InstantiationOptimizer {
      Object newInstance();
   }
}
