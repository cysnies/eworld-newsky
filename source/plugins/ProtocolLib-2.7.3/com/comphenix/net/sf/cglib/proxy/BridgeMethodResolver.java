package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.AnnotationVisitor;
import com.comphenix.net.sf.cglib.asm.Attribute;
import com.comphenix.net.sf.cglib.asm.ClassReader;
import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.FieldVisitor;
import com.comphenix.net.sf.cglib.asm.Label;
import com.comphenix.net.sf.cglib.asm.MethodVisitor;
import com.comphenix.net.sf.cglib.core.Signature;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

class BridgeMethodResolver {
   private final Map declToBridge;

   public BridgeMethodResolver(Map declToBridge) {
      this.declToBridge = declToBridge;
   }

   public Map resolveAll() {
      Map resolved = new HashMap();

      for(Map.Entry entry : this.declToBridge.entrySet()) {
         Class owner = (Class)entry.getKey();
         Set bridges = (Set)entry.getValue();

         try {
            (new ClassReader(owner.getName())).accept(new BridgedFinder(bridges, resolved), 6);
         } catch (IOException var7) {
         }
      }

      return resolved;
   }

   private static class BridgedFinder implements ClassVisitor, MethodVisitor {
      private Map resolved;
      private Set eligableMethods;
      private Signature currentMethod = null;

      BridgedFinder(Set eligableMethods, Map resolved) {
         this.resolved = resolved;
         this.eligableMethods = eligableMethods;
      }

      public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      }

      public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
         Signature sig = new Signature(name, desc);
         if (this.eligableMethods.remove(sig)) {
            this.currentMethod = sig;
            return this;
         } else {
            return null;
         }
      }

      public void visitSource(String source, String debug) {
      }

      public void visitLineNumber(int line, Label start) {
      }

      public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      }

      public void visitEnd() {
      }

      public void visitInnerClass(String name, String outerName, String innerName, int access) {
      }

      public void visitOuterClass(String owner, String name, String desc) {
      }

      public void visitAttribute(Attribute attr) {
      }

      public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
         return null;
      }

      public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
         return null;
      }

      public AnnotationVisitor visitAnnotationDefault() {
         return null;
      }

      public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
         return null;
      }

      public void visitCode() {
      }

      public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
      }

      public void visitIincInsn(int var, int increment) {
      }

      public void visitInsn(int opcode) {
      }

      public void visitIntInsn(int opcode, int operand) {
      }

      public void visitJumpInsn(int opcode, Label label) {
      }

      public void visitLabel(Label label) {
      }

      public void visitLdcInsn(Object cst) {
      }

      public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
      }

      public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      }

      public void visitMaxs(int maxStack, int maxLocals) {
      }

      public void visitMethodInsn(int opcode, String owner, String name, String desc) {
         if (opcode == 183 && this.currentMethod != null) {
            Signature target = new Signature(name, desc);
            if (!target.equals(this.currentMethod)) {
               this.resolved.put(this.currentMethod, target);
            }

            this.currentMethod = null;
         }

      }

      public void visitMultiANewArrayInsn(String desc, int dims) {
      }

      public void visitTableSwitchInsn(int min, int max, Label dflt, Label[] labels) {
      }

      public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
      }

      public void visitTypeInsn(int opcode, String desc) {
      }

      public void visitVarInsn(int opcode, int var) {
      }
   }
}
