package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.core.CollectionUtils;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import com.comphenix.net.sf.cglib.core.RejectModifierPredicate;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class MixinEverythingEmitter extends MixinEmitter {
   public MixinEverythingEmitter(ClassVisitor v, String className, Class[] classes) {
      super(v, className, classes, (int[])null);
   }

   protected Class[] getInterfaces(Class[] classes) {
      List list = new ArrayList();

      for(int i = 0; i < classes.length; ++i) {
         ReflectUtils.addAllInterfaces(classes[i], list);
      }

      return (Class[])list.toArray(new Class[list.size()]);
   }

   protected Method[] getMethods(Class type) {
      List methods = new ArrayList(Arrays.asList(type.getMethods()));
      CollectionUtils.filter(methods, new RejectModifierPredicate(24));
      return (Method[])methods.toArray(new Method[methods.size()]);
   }
}
