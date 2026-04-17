package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.AnnotationVisitor;
import com.comphenix.net.sf.cglib.asm.Attribute;
import com.comphenix.net.sf.cglib.asm.FieldVisitor;

public class FieldVisitorTee implements FieldVisitor {
   private FieldVisitor fv1;
   private FieldVisitor fv2;

   public FieldVisitorTee(FieldVisitor fv1, FieldVisitor fv2) {
      this.fv1 = fv1;
      this.fv2 = fv2;
   }

   public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return AnnotationVisitorTee.getInstance(this.fv1.visitAnnotation(desc, visible), this.fv2.visitAnnotation(desc, visible));
   }

   public void visitAttribute(Attribute attr) {
      this.fv1.visitAttribute(attr);
      this.fv2.visitAttribute(attr);
   }

   public void visitEnd() {
      this.fv1.visitEnd();
      this.fv2.visitEnd();
   }
}
