package com.comphenix.net.sf.cglib.asm;

public interface FieldVisitor {
   AnnotationVisitor visitAnnotation(String var1, boolean var2);

   void visitAttribute(Attribute var1);

   void visitEnd();
}
