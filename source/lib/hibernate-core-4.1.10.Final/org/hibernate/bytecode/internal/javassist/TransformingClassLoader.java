package org.hibernate.bytecode.internal.javassist;

import java.io.IOException;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.hibernate.HibernateException;

public class TransformingClassLoader extends ClassLoader {
   private ClassLoader parent;
   private ClassPool classPool;

   TransformingClassLoader(ClassLoader parent, String[] classpath) {
      this.parent = parent;
      this.classPool = new ClassPool(true);

      for(int i = 0; i < classpath.length; ++i) {
         try {
            this.classPool.appendClassPath(classpath[i]);
         } catch (NotFoundException e) {
            throw new HibernateException("Unable to resolve requested classpath for transformation [" + classpath[i] + "] : " + e.getMessage());
         }
      }

   }

   protected Class findClass(String name) throws ClassNotFoundException {
      try {
         CtClass cc = this.classPool.get(name);
         byte[] b = cc.toBytecode();
         return this.defineClass(name, b, 0, b.length);
      } catch (NotFoundException var4) {
         throw new ClassNotFoundException();
      } catch (IOException var5) {
         throw new ClassNotFoundException();
      } catch (CannotCompileException var6) {
         throw new ClassNotFoundException();
      }
   }

   public void release() {
      this.classPool = null;
      this.parent = null;
   }
}
