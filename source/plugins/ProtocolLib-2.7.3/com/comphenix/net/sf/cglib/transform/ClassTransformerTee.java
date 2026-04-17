package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.ClassAdapter;
import com.comphenix.net.sf.cglib.asm.ClassVisitor;

public class ClassTransformerTee extends ClassAdapter implements ClassTransformer {
   private ClassVisitor branch;

   public ClassTransformerTee(ClassVisitor branch) {
      super((ClassVisitor)null);
      this.branch = branch;
   }

   public void setTarget(ClassVisitor target) {
      this.cv = new ClassVisitorTee(this.branch, target);
   }
}
