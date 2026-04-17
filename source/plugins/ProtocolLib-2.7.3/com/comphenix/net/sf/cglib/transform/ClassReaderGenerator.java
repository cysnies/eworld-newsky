package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.Attribute;
import com.comphenix.net.sf.cglib.asm.ClassReader;
import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.core.ClassGenerator;

public class ClassReaderGenerator implements ClassGenerator {
   private final ClassReader r;
   private final Attribute[] attrs;
   private final int flags;

   public ClassReaderGenerator(ClassReader r, int flags) {
      this(r, (Attribute[])null, flags);
   }

   public ClassReaderGenerator(ClassReader r, Attribute[] attrs, int flags) {
      this.r = r;
      this.attrs = attrs != null ? attrs : new Attribute[0];
      this.flags = flags;
   }

   public void generateClass(ClassVisitor v) {
      this.r.accept(v, this.attrs, this.flags);
   }
}
