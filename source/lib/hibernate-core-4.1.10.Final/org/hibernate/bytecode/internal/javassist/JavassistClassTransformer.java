package org.hibernate.bytecode.internal.javassist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.ProtectionDomain;
import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.bytecode.ClassFile;
import org.hibernate.HibernateException;
import org.hibernate.bytecode.buildtime.spi.ClassFilter;
import org.hibernate.bytecode.spi.AbstractClassTransformerImpl;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class JavassistClassTransformer extends AbstractClassTransformerImpl {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JavassistClassTransformer.class.getName());

   public JavassistClassTransformer(ClassFilter classFilter, org.hibernate.bytecode.buildtime.spi.FieldFilter fieldFilter) {
      super(classFilter, fieldFilter);
   }

   protected byte[] doTransform(ClassLoader loader, String className, Class classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
      ClassFile classfile;
      try {
         classfile = new ClassFile(new DataInputStream(new ByteArrayInputStream(classfileBuffer)));
      } catch (IOException var25) {
         LOG.unableToBuildEnhancementMetamodel(className);
         return classfileBuffer;
      }

      ClassPool cp = new ClassPool();
      cp.appendSystemPath();
      cp.appendClassPath(new ClassClassPath(this.getClass()));
      cp.appendClassPath(new ClassClassPath(classfile.getClass()));

      try {
         cp.makeClassIfNew(new ByteArrayInputStream(classfileBuffer));
      } catch (IOException e) {
         throw new RuntimeException(e.getMessage(), e);
      }

      FieldTransformer transformer = this.getFieldTransformer(classfile, cp);
      if (transformer != null) {
         LOG.debugf("Enhancing %s", className);
         DataOutputStream out = null;

         byte[] var11;
         try {
            transformer.transform(classfile);
            ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
            out = new DataOutputStream(byteStream);
            classfile.write(out);
            var11 = byteStream.toByteArray();
         } catch (Exception e) {
            LOG.unableToTransformClass(e.getMessage());
            throw new HibernateException("Unable to transform class: " + e.getMessage());
         } finally {
            try {
               if (out != null) {
                  out.close();
               }
            } catch (IOException var21) {
            }

         }

         return var11;
      } else {
         return classfileBuffer;
      }
   }

   protected FieldTransformer getFieldTransformer(final ClassFile classfile, ClassPool classPool) {
      return this.alreadyInstrumented(classfile) ? null : new FieldTransformer(new FieldFilter() {
         public boolean handleRead(String desc, String name) {
            return JavassistClassTransformer.this.fieldFilter.shouldInstrumentField(classfile.getName(), name);
         }

         public boolean handleWrite(String desc, String name) {
            return JavassistClassTransformer.this.fieldFilter.shouldInstrumentField(classfile.getName(), name);
         }

         public boolean handleReadAccess(String fieldOwnerClassName, String fieldName) {
            return JavassistClassTransformer.this.fieldFilter.shouldTransformFieldAccess(classfile.getName(), fieldOwnerClassName, fieldName);
         }

         public boolean handleWriteAccess(String fieldOwnerClassName, String fieldName) {
            return JavassistClassTransformer.this.fieldFilter.shouldTransformFieldAccess(classfile.getName(), fieldOwnerClassName, fieldName);
         }
      }, classPool);
   }

   private boolean alreadyInstrumented(ClassFile classfile) {
      String[] intfs = classfile.getInterfaces();

      for(int i = 0; i < intfs.length; ++i) {
         if (FieldHandled.class.getName().equals(intfs[i])) {
            return true;
         }
      }

      return false;
   }
}
