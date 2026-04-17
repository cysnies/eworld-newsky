package org.hibernate.bytecode.buildtime.internal;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.Set;
import javassist.bytecode.ClassFile;
import org.hibernate.bytecode.buildtime.spi.AbstractInstrumenter;
import org.hibernate.bytecode.buildtime.spi.BasicClassFilter;
import org.hibernate.bytecode.buildtime.spi.ClassDescriptor;
import org.hibernate.bytecode.buildtime.spi.Instrumenter;
import org.hibernate.bytecode.buildtime.spi.Logger;
import org.hibernate.bytecode.internal.javassist.BytecodeProviderImpl;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.bytecode.spi.ClassTransformer;

public class JavassistInstrumenter extends AbstractInstrumenter {
   private static final BasicClassFilter CLASS_FILTER = new BasicClassFilter();
   private final BytecodeProviderImpl provider = new BytecodeProviderImpl();

   public JavassistInstrumenter(Logger logger, Instrumenter.Options options) {
      super(logger, options);
   }

   protected ClassDescriptor getClassDescriptor(byte[] bytecode) throws IOException {
      return new CustomClassDescriptor(bytecode);
   }

   protected ClassTransformer getClassTransformer(ClassDescriptor descriptor, Set classNames) {
      if (descriptor.isInstrumented()) {
         this.logger.debug("class [" + descriptor.getName() + "] already instrumented");
         return null;
      } else {
         return this.provider.getTransformer(CLASS_FILTER, new AbstractInstrumenter.CustomFieldFilter(descriptor, classNames));
      }
   }

   private static class CustomClassDescriptor implements ClassDescriptor {
      private final byte[] bytes;
      private final ClassFile classFile;

      public CustomClassDescriptor(byte[] bytes) throws IOException {
         this.bytes = bytes;
         this.classFile = new ClassFile(new DataInputStream(new ByteArrayInputStream(bytes)));
      }

      public String getName() {
         return this.classFile.getName();
      }

      public boolean isInstrumented() {
         String[] interfaceNames = this.classFile.getInterfaces();

         for(String interfaceName : interfaceNames) {
            if (FieldHandled.class.getName().equals(interfaceName)) {
               return true;
            }
         }

         return false;
      }

      public byte[] getBytes() {
         return this.bytes;
      }
   }
}
