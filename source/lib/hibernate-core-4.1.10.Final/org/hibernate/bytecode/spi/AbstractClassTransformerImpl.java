package org.hibernate.bytecode.spi;

import java.security.ProtectionDomain;
import org.hibernate.bytecode.buildtime.spi.ClassFilter;
import org.hibernate.bytecode.buildtime.spi.FieldFilter;

public abstract class AbstractClassTransformerImpl implements ClassTransformer {
   protected final ClassFilter classFilter;
   protected final FieldFilter fieldFilter;

   protected AbstractClassTransformerImpl(ClassFilter classFilter, FieldFilter fieldFilter) {
      this.classFilter = classFilter;
      this.fieldFilter = fieldFilter;
   }

   public byte[] transform(ClassLoader loader, String className, Class classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
      className = className.replace('/', '.');
      return this.classFilter.shouldInstrumentClass(className) ? this.doTransform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer) : classfileBuffer;
   }

   protected abstract byte[] doTransform(ClassLoader var1, String var2, Class var3, ProtectionDomain var4, byte[] var5);
}
