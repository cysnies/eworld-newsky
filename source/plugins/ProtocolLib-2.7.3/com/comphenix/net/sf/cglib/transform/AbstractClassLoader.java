package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.Attribute;
import com.comphenix.net.sf.cglib.asm.ClassReader;
import com.comphenix.net.sf.cglib.asm.ClassWriter;
import com.comphenix.net.sf.cglib.core.ClassGenerator;
import com.comphenix.net.sf.cglib.core.CodeGenerationException;
import com.comphenix.net.sf.cglib.core.DebuggingClassWriter;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.ProtectionDomain;

public abstract class AbstractClassLoader extends ClassLoader {
   private ClassFilter filter;
   private ClassLoader classPath;
   private static ProtectionDomain DOMAIN = (ProtectionDomain)AccessController.doPrivileged(new PrivilegedAction() {
      public Object run() {
         return (AbstractClassLoader.class$net$sf$cglib$transform$AbstractClassLoader == null ? (AbstractClassLoader.class$net$sf$cglib$transform$AbstractClassLoader = AbstractClassLoader.class$("com.comphenix.net.sf.cglib.transform.AbstractClassLoader")) : AbstractClassLoader.class$net$sf$cglib$transform$AbstractClassLoader).getProtectionDomain();
      }
   });
   // $FF: synthetic field
   static Class class$net$sf$cglib$transform$AbstractClassLoader;

   protected AbstractClassLoader(ClassLoader parent, ClassLoader classPath, ClassFilter filter) {
      super(parent);
      this.filter = filter;
      this.classPath = classPath;
   }

   public Class loadClass(String name) throws ClassNotFoundException {
      Class loaded = this.findLoadedClass(name);
      if (loaded != null && loaded.getClassLoader() == this) {
         return loaded;
      } else if (!this.filter.accept(name)) {
         return super.loadClass(name);
      } else {
         ClassReader r;
         try {
            InputStream is = this.classPath.getResourceAsStream(name.replace('.', '/') + ".class");
            if (is == null) {
               throw new ClassNotFoundException(name);
            }

            try {
               r = new ClassReader(is);
            } finally {
               is.close();
            }
         } catch (IOException e) {
            throw new ClassNotFoundException(name + ":" + e.getMessage());
         }

         try {
            ClassWriter w = new DebuggingClassWriter(1);
            this.getGenerator(r).generateClass(w);
            byte[] b = w.toByteArray();
            Class c = super.defineClass(name, b, 0, b.length, DOMAIN);
            this.postProcess(c);
            return c;
         } catch (RuntimeException e) {
            throw e;
         } catch (Error e) {
            throw e;
         } catch (Exception e) {
            throw new CodeGenerationException(e);
         }
      }
   }

   protected ClassGenerator getGenerator(ClassReader r) {
      return new ClassReaderGenerator(r, this.attributes(), this.getFlags());
   }

   protected int getFlags() {
      return 0;
   }

   protected Attribute[] attributes() {
      return null;
   }

   protected void postProcess(Class c) {
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }
}
