package com.comphenix.net.sf.cglib.transform.impl;

import com.comphenix.net.sf.cglib.core.ClassGenerator;
import com.comphenix.net.sf.cglib.core.DefaultGeneratorStrategy;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import com.comphenix.net.sf.cglib.transform.ClassTransformer;
import com.comphenix.net.sf.cglib.transform.MethodFilter;
import com.comphenix.net.sf.cglib.transform.MethodFilterTransformer;
import com.comphenix.net.sf.cglib.transform.TransformingClassGenerator;

public class UndeclaredThrowableStrategy extends DefaultGeneratorStrategy {
   private ClassTransformer t;
   private static final MethodFilter TRANSFORM_FILTER = new MethodFilter() {
      public boolean accept(int access, String name, String desc, String signature, String[] exceptions) {
         return !TypeUtils.isPrivate(access) && name.indexOf(36) < 0;
      }
   };

   public UndeclaredThrowableStrategy(Class wrapper) {
      this.t = new UndeclaredThrowableTransformer(wrapper);
      this.t = new MethodFilterTransformer(TRANSFORM_FILTER, this.t);
   }

   protected ClassGenerator transform(ClassGenerator cg) throws Exception {
      return new TransformingClassGenerator(cg, this.t);
   }
}
