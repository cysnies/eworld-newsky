package com.comphenix.net.sf.cglib.transform;

import com.comphenix.net.sf.cglib.asm.AnnotationVisitor;
import com.comphenix.net.sf.cglib.asm.Attribute;
import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.FieldVisitor;
import com.comphenix.net.sf.cglib.asm.MethodVisitor;

public abstract class AbstractClassFilterTransformer extends AbstractClassTransformer {
   private ClassTransformer pass;
   private ClassVisitor target;

   public void setTarget(ClassVisitor target) {
      super.setTarget(target);
      this.pass.setTarget(target);
   }

   protected AbstractClassFilterTransformer(ClassTransformer pass) {
      this.pass = pass;
   }

   protected abstract boolean accept(int var1, int var2, String var3, String var4, String var5, String[] var6);

   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      this.target = (ClassVisitor)(this.accept(version, access, name, signature, superName, interfaces) ? this.pass : this.cv);
      this.target.visit(version, access, name, signature, superName, interfaces);
   }

   public void visitSource(String source, String debug) {
      this.target.visitSource(source, debug);
   }

   public void visitOuterClass(String owner, String name, String desc) {
      this.target.visitOuterClass(owner, name, desc);
   }

   public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return this.target.visitAnnotation(desc, visible);
   }

   public void visitAttribute(Attribute attr) {
      this.target.visitAttribute(attr);
   }

   public void visitInnerClass(String name, String outerName, String innerName, int access) {
      this.target.visitInnerClass(name, outerName, innerName, access);
   }

   public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
      return this.target.visitField(access, name, desc, signature, value);
   }

   public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
      return this.target.visitMethod(access, name, desc, signature, exceptions);
   }

   public void visitEnd() {
      this.target.visitEnd();
      this.target = null;
   }
}
