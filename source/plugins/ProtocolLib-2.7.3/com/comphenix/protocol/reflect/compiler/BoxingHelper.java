package com.comphenix.protocol.reflect.compiler;

import com.comphenix.net.sf.cglib.asm.MethodVisitor;
import com.comphenix.net.sf.cglib.asm.Type;

class BoxingHelper {
   private static final Type BYTE_TYPE = Type.getObjectType("java/lang/Byte");
   private static final Type BOOLEAN_TYPE = Type.getObjectType("java/lang/Boolean");
   private static final Type SHORT_TYPE = Type.getObjectType("java/lang/Short");
   private static final Type CHARACTER_TYPE = Type.getObjectType("java/lang/Character");
   private static final Type INTEGER_TYPE = Type.getObjectType("java/lang/Integer");
   private static final Type FLOAT_TYPE = Type.getObjectType("java/lang/Float");
   private static final Type LONG_TYPE = Type.getObjectType("java/lang/Long");
   private static final Type DOUBLE_TYPE = Type.getObjectType("java/lang/Double");
   private static final Type NUMBER_TYPE = Type.getObjectType("java/lang/Number");
   private static final Type OBJECT_TYPE = Type.getObjectType("java/lang/Object");
   private static final MethodDescriptor BOOLEAN_VALUE = MethodDescriptor.getMethod("boolean booleanValue()");
   private static final MethodDescriptor CHAR_VALUE = MethodDescriptor.getMethod("char charValue()");
   private static final MethodDescriptor INT_VALUE = MethodDescriptor.getMethod("int intValue()");
   private static final MethodDescriptor FLOAT_VALUE = MethodDescriptor.getMethod("float floatValue()");
   private static final MethodDescriptor LONG_VALUE = MethodDescriptor.getMethod("long longValue()");
   private static final MethodDescriptor DOUBLE_VALUE = MethodDescriptor.getMethod("double doubleValue()");
   private MethodVisitor mv;

   public BoxingHelper(MethodVisitor mv) {
      this.mv = mv;
   }

   public void box(Type type) {
      if (type.getSort() != 10 && type.getSort() != 9) {
         if (type == Type.VOID_TYPE) {
            this.push((String)null);
         } else {
            Type boxed = type;
            switch (type.getSort()) {
               case 1:
                  boxed = BOOLEAN_TYPE;
                  break;
               case 2:
                  boxed = CHARACTER_TYPE;
                  break;
               case 3:
                  boxed = BYTE_TYPE;
                  break;
               case 4:
                  boxed = SHORT_TYPE;
                  break;
               case 5:
                  boxed = INTEGER_TYPE;
                  break;
               case 6:
                  boxed = FLOAT_TYPE;
                  break;
               case 7:
                  boxed = LONG_TYPE;
                  break;
               case 8:
                  boxed = DOUBLE_TYPE;
            }

            this.newInstance(boxed);
            if (type.getSize() == 2) {
               this.dupX2();
               this.dupX2();
               this.pop();
            } else {
               this.dupX1();
               this.swap();
            }

            this.invokeConstructor(boxed, new MethodDescriptor("<init>", Type.VOID_TYPE, new Type[]{type}));
         }

      }
   }

   public void invokeConstructor(Type type, MethodDescriptor method) {
      this.invokeInsn(183, type, method);
   }

   public void dupX1() {
      this.mv.visitInsn(90);
   }

   public void dupX2() {
      this.mv.visitInsn(91);
   }

   public void pop() {
      this.mv.visitInsn(87);
   }

   public void swap() {
      this.mv.visitInsn(95);
   }

   public void push(boolean value) {
      this.push(value ? 1 : 0);
   }

   public void push(int value) {
      if (value >= -1 && value <= 5) {
         this.mv.visitInsn(3 + value);
      } else if (value >= -128 && value <= 127) {
         this.mv.visitIntInsn(16, value);
      } else if (value >= -32768 && value <= 32767) {
         this.mv.visitIntInsn(17, value);
      } else {
         this.mv.visitLdcInsn(new Integer(value));
      }

   }

   public void newInstance(Type type) {
      this.typeInsn(187, type);
   }

   public void push(String value) {
      if (value == null) {
         this.mv.visitInsn(1);
      } else {
         this.mv.visitLdcInsn(value);
      }

   }

   public void unbox(Type type) {
      Type t = NUMBER_TYPE;
      MethodDescriptor sig = null;
      switch (type.getSort()) {
         case 0:
            return;
         case 1:
            t = BOOLEAN_TYPE;
            sig = BOOLEAN_VALUE;
            break;
         case 2:
            t = CHARACTER_TYPE;
            sig = CHAR_VALUE;
            break;
         case 3:
         case 4:
         case 5:
            sig = INT_VALUE;
            break;
         case 6:
            sig = FLOAT_VALUE;
            break;
         case 7:
            sig = LONG_VALUE;
            break;
         case 8:
            sig = DOUBLE_VALUE;
      }

      if (sig == null) {
         this.checkCast(type);
      } else {
         this.checkCast(t);
         this.invokeVirtual(t, sig);
      }

   }

   public void checkCast(Type type) {
      if (!type.equals(OBJECT_TYPE)) {
         this.typeInsn(192, type);
      }

   }

   public void invokeVirtual(Type owner, MethodDescriptor method) {
      this.invokeInsn(182, owner, method);
   }

   private void invokeInsn(int opcode, Type type, MethodDescriptor method) {
      String owner = type.getSort() == 9 ? type.getDescriptor() : type.getInternalName();
      this.mv.visitMethodInsn(opcode, owner, method.getName(), method.getDescriptor());
   }

   private void typeInsn(int opcode, Type type) {
      String desc;
      if (type.getSort() == 9) {
         desc = type.getDescriptor();
      } else {
         desc = type.getInternalName();
      }

      this.mv.visitTypeInsn(opcode, desc);
   }
}
