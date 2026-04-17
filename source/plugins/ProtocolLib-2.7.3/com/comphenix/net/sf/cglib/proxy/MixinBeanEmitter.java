package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import java.lang.reflect.Method;

class MixinBeanEmitter extends MixinEmitter {
   public MixinBeanEmitter(ClassVisitor v, String className, Class[] classes) {
      super(v, className, classes, (int[])null);
   }

   protected Class[] getInterfaces(Class[] classes) {
      return null;
   }

   protected Method[] getMethods(Class type) {
      return ReflectUtils.getPropertyMethods(ReflectUtils.getBeanProperties(type), true, true);
   }
}
