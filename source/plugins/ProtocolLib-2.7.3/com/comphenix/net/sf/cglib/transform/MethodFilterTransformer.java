package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.MethodVisitor;

public class MethodFilterTransformer extends AbstractClassTransformer {
   private MethodFilter filter;
   private ClassTransformer pass;
   private ClassVisitor direct;

   public MethodFilterTransformer(MethodFilter filter, ClassTransformer pass) {
      this.filter = filter;
      this.pass = pass;
      super.setTarget(pass);
   }

   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      return ((ClassVisitor)(this.filter.accept(access, name, desc, signature, exceptions) ? this.pass : this.direct)).visitMethod(access, name, desc, signature, exceptions);
   }

   public void setTarget(ClassVisitor target) {
      this.pass.setTarget(target);
      this.direct = target;
   }
}
