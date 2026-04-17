package org.hibernate.bytecode.spi;

import java.io.InputStream;
import java.security.ProtectionDomain;

public class InstrumentedClassLoader extends ClassLoader {
   private ClassTransformer classTransformer;

   public InstrumentedClassLoader(ClassLoader parent, ClassTransformer classTransformer) {
      super(parent);
      this.classTransformer = classTransformer;
   }

   public Class loadClass(String name) throws ClassNotFoundException {
      if (!name.startsWith("java.") && this.classTransformer != null) {
         Class c = this.findLoadedClass(name);
         if (c != null) {
            return c;
         } else {
            InputStream is = this.getResourceAsStream(name.replace('.', '/') + ".class");
            if (is == null) {
               throw new ClassNotFoundException(name + " not found");
            } else {
               try {
                  byte[] originalBytecode = ByteCodeHelper.readByteCode(is);
                  byte[] transformedBytecode = this.classTransformer.transform(this.getParent(), name, (Class)null, (ProtectionDomain)null, originalBytecode);
                  return originalBytecode == transformedBytecode ? this.getParent().loadClass(name) : this.defineClass(name, transformedBytecode, 0, transformedBytecode.length);
               } catch (Throwable t) {
                  throw new ClassNotFoundException(name + " not found", t);
               }
            }
         }
      } else {
         return this.getParent().loadClass(name);
      }
   }
}
