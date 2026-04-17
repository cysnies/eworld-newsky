package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.ClassAdapter;
import com.comphenix.net.sf.cglib.asm.ClassVisitor;

public abstract class AbstractClassTransformer extends ClassAdapter implements ClassTransformer {
   protected AbstractClassTransformer() {
      super((ClassVisitor)null);
   }

   public void setTarget(ClassVisitor target) {
      this.cv = target;
   }
}
