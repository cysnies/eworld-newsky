package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.AbstractClassGenerator;
import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import com.comphenix.net.sf.cglib.core.Signature;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class InterfaceMaker extends AbstractClassGenerator {
   private static final AbstractClassGenerator.Source SOURCE;
   private Map signatures = new HashMap();
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$InterfaceMaker;

   public InterfaceMaker() {
      super(SOURCE);
   }

   public void add(Signature sig, Type[] exceptions) {
      this.signatures.put(sig, exceptions);
   }

   public void add(Method method) {
      this.add(ReflectUtils.getSignature(method), ReflectUtils.getExceptionTypes(method));
   }

   public void add(Class clazz) {
      Method[] methods = clazz.getMethods();

      for(int i = 0; i < methods.length; ++i) {
         Method m = methods[i];
         if (!m.getDeclaringClass().getName().equals("java.lang.Object")) {
            this.add(m);
         }
      }

   }

   public Class create() {
      this.setUseCache(false);
      return (Class)super.create(this);
   }

   protected ClassLoader getDefaultClassLoader() {
      return null;
   }

   protected Object firstInstance(Class type) {
      return type;
   }

   protected Object nextInstance(Object instance) {
      throw new IllegalStateException("InterfaceMaker does not cache");
   }

   public void generateClass(ClassVisitor v) throws Exception {
      ClassEmitter ce = new ClassEmitter(v);
      ce.begin_class(46, 513, this.getClassName(), (Type)null, (Type[])null, "<generated>");

      for(Signature sig : this.signatures.keySet()) {
         Type[] exceptions = (Type[])this.signatures.get(sig);
         ce.begin_method(1025, sig, exceptions).end_method();
      }

      ce.end_class();
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      SOURCE = new AbstractClassGenerator.Source((class$net$sf$cglib$proxy$InterfaceMaker == null ? (class$net$sf$cglib$proxy$InterfaceMaker = class$("com.comphenix.net.sf.cglib.proxy.InterfaceMaker")) : class$net$sf$cglib$proxy$InterfaceMaker).getName());
   }
}
