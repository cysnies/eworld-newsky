package org.hibernate.bytecode.spi;

import org.hibernate.bytecode.buildtime.spi.ClassFilter;
import org.hibernate.bytecode.buildtime.spi.FieldFilter;

public interface BytecodeProvider {
   ProxyFactoryFactory getProxyFactoryFactory();

   ReflectionOptimizer getReflectionOptimizer(Class var1, String[] var2, String[] var3, Class[] var4);

   ClassTransformer getTransformer(ClassFilter var1, FieldFilter var2);

   EntityInstrumentationMetadata getEntityInstrumentationMetadata(Class var1);
}
