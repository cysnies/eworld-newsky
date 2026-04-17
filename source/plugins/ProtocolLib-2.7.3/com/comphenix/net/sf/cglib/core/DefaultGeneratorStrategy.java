package com.comphenix.net.sf.cglib.core;

import com.comphenix.net.sf.cglib.asm.ClassWriter;

public class DefaultGeneratorStrategy implements GeneratorStrategy {
   public static final DefaultGeneratorStrategy INSTANCE = new DefaultGeneratorStrategy();

   public byte[] generate(ClassGenerator cg) throws Exception {
      ClassWriter cw = this.getClassWriter();
      this.transform(cg).generateClass(cw);
      return this.transform(cw.toByteArray());
   }

   protected ClassWriter getClassWriter() throws Exception {
      return new DebuggingClassWriter(1);
   }

   protected byte[] transform(byte[] b) throws Exception {
      return b;
   }

   protected ClassGenerator transform(ClassGenerator cg) throws Exception {
      return cg;
   }
}
