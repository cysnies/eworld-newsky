package org.hibernate.bytecode.internal.javassist;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.StackMapTable;
import javassist.bytecode.stackmap.MapMaker;

public class FieldTransformer {
   private static final String EACH_READ_METHOD_PREFIX = "$javassist_read_";
   private static final String EACH_WRITE_METHOD_PREFIX = "$javassist_write_";
   private static final String FIELD_HANDLED_TYPE_NAME = FieldHandled.class.getName();
   private static final String HANDLER_FIELD_NAME = "$JAVASSIST_READ_WRITE_HANDLER";
   private static final String FIELD_HANDLER_TYPE_NAME = FieldHandler.class.getName();
   private static final String HANDLER_FIELD_DESCRIPTOR;
   private static final String GETFIELDHANDLER_METHOD_NAME = "getFieldHandler";
   private static final String SETFIELDHANDLER_METHOD_NAME = "setFieldHandler";
   private static final String GETFIELDHANDLER_METHOD_DESCRIPTOR;
   private static final String SETFIELDHANDLER_METHOD_DESCRIPTOR;
   private FieldFilter filter;
   private ClassPool classPool;

   public FieldTransformer() {
      this((FieldFilter)null, (ClassPool)null);
   }

   public FieldTransformer(FieldFilter f, ClassPool c) {
      this.filter = f;
      this.classPool = c;
   }

   public void setClassPool(ClassPool c) {
      this.classPool = c;
   }

   public void setFieldFilter(FieldFilter f) {
      this.filter = f;
   }

   public void transform(File file) throws Exception {
      DataInputStream in = new DataInputStream(new FileInputStream(file));
      ClassFile classfile = new ClassFile(in);
      this.transform(classfile);
      DataOutputStream out = new DataOutputStream(new FileOutputStream(file));

      try {
         classfile.write(out);
      } finally {
         out.close();
      }

   }

   public void transform(ClassFile classfile) throws Exception {
      if (!classfile.isInterface()) {
         try {
            this.addFieldHandlerField(classfile);
            this.addGetFieldHandlerMethod(classfile);
            this.addSetFieldHandlerMethod(classfile);
            this.addFieldHandledInterface(classfile);
            this.addReadWriteMethods(classfile);
            this.transformInvokevirtualsIntoPutAndGetfields(classfile);
         } catch (CannotCompileException e) {
            throw new RuntimeException(e.getMessage(), e);
         }
      }
   }

   private void addFieldHandlerField(ClassFile classfile) throws CannotCompileException {
      ConstPool cp = classfile.getConstPool();
      FieldInfo finfo = new FieldInfo(cp, "$JAVASSIST_READ_WRITE_HANDLER", HANDLER_FIELD_DESCRIPTOR);
      finfo.setAccessFlags(130);
      classfile.addField(finfo);
   }

   private void addGetFieldHandlerMethod(ClassFile classfile) throws CannotCompileException, BadBytecode {
      ConstPool cp = classfile.getConstPool();
      int this_class_index = cp.getThisClassInfo();
      MethodInfo minfo = new MethodInfo(cp, "getFieldHandler", GETFIELDHANDLER_METHOD_DESCRIPTOR);
      Bytecode code = new Bytecode(cp, 2, 1);
      code.addAload(0);
      code.addOpcode(180);
      int field_index = cp.addFieldrefInfo(this_class_index, "$JAVASSIST_READ_WRITE_HANDLER", HANDLER_FIELD_DESCRIPTOR);
      code.addIndex(field_index);
      code.addOpcode(176);
      minfo.setCodeAttribute(code.toCodeAttribute());
      minfo.setAccessFlags(1);
      CodeAttribute codeAttribute = minfo.getCodeAttribute();
      if (codeAttribute != null) {
         StackMapTable smt = MapMaker.make(this.classPool, minfo);
         codeAttribute.setAttribute(smt);
      }

      classfile.addMethod(minfo);
   }

   private void addSetFieldHandlerMethod(ClassFile classfile) throws CannotCompileException, BadBytecode {
      ConstPool cp = classfile.getConstPool();
      int this_class_index = cp.getThisClassInfo();
      MethodInfo minfo = new MethodInfo(cp, "setFieldHandler", SETFIELDHANDLER_METHOD_DESCRIPTOR);
      Bytecode code = new Bytecode(cp, 3, 3);
      code.addAload(0);
      code.addAload(1);
      code.addOpcode(181);
      int field_index = cp.addFieldrefInfo(this_class_index, "$JAVASSIST_READ_WRITE_HANDLER", HANDLER_FIELD_DESCRIPTOR);
      code.addIndex(field_index);
      code.addOpcode(177);
      minfo.setCodeAttribute(code.toCodeAttribute());
      minfo.setAccessFlags(1);
      CodeAttribute codeAttribute = minfo.getCodeAttribute();
      if (codeAttribute != null) {
         StackMapTable smt = MapMaker.make(this.classPool, minfo);
         codeAttribute.setAttribute(smt);
      }

      classfile.addMethod(minfo);
   }

   private void addFieldHandledInterface(ClassFile classfile) {
      String[] interfaceNames = classfile.getInterfaces();
      String[] newInterfaceNames = new String[interfaceNames.length + 1];
      System.arraycopy(interfaceNames, 0, newInterfaceNames, 0, interfaceNames.length);
      newInterfaceNames[newInterfaceNames.length - 1] = FIELD_HANDLED_TYPE_NAME;
      classfile.setInterfaces(newInterfaceNames);
   }

   private void addReadWriteMethods(ClassFile classfile) throws CannotCompileException, BadBytecode {
      for(FieldInfo finfo : classfile.getFields()) {
         if ((finfo.getAccessFlags() & 8) == 0 && !finfo.getName().equals("$JAVASSIST_READ_WRITE_HANDLER")) {
            if (this.filter.handleRead(finfo.getDescriptor(), finfo.getName())) {
               this.addReadMethod(classfile, finfo);
            }

            if (this.filter.handleWrite(finfo.getDescriptor(), finfo.getName())) {
               this.addWriteMethod(classfile, finfo);
            }
         }
      }

   }

   private void addReadMethod(ClassFile classfile, FieldInfo finfo) throws CannotCompileException, BadBytecode {
      ConstPool cp = classfile.getConstPool();
      int this_class_index = cp.getThisClassInfo();
      String desc = "()" + finfo.getDescriptor();
      MethodInfo minfo = new MethodInfo(cp, "$javassist_read_" + finfo.getName(), desc);
      Bytecode code = new Bytecode(cp, 5, 3);
      code.addAload(0);
      code.addOpcode(180);
      int base_field_index = cp.addFieldrefInfo(this_class_index, finfo.getName(), finfo.getDescriptor());
      code.addIndex(base_field_index);
      code.addAload(0);
      int enabled_class_index = cp.addClassInfo(FIELD_HANDLED_TYPE_NAME);
      code.addInvokeinterface(enabled_class_index, "getFieldHandler", GETFIELDHANDLER_METHOD_DESCRIPTOR, 1);
      code.addOpcode(199);
      code.addIndex(4);
      addTypeDependDataReturn(code, finfo.getDescriptor());
      addTypeDependDataStore(code, finfo.getDescriptor(), 1);
      code.addAload(0);
      code.addInvokeinterface(enabled_class_index, "getFieldHandler", GETFIELDHANDLER_METHOD_DESCRIPTOR, 1);
      code.addAload(0);
      code.addLdc(finfo.getName());
      addTypeDependDataLoad(code, finfo.getDescriptor(), 1);
      addInvokeFieldHandlerMethod(classfile, code, finfo.getDescriptor(), true);
      addTypeDependDataReturn(code, finfo.getDescriptor());
      minfo.setCodeAttribute(code.toCodeAttribute());
      minfo.setAccessFlags(1);
      CodeAttribute codeAttribute = minfo.getCodeAttribute();
      if (codeAttribute != null) {
         StackMapTable smt = MapMaker.make(this.classPool, minfo);
         codeAttribute.setAttribute(smt);
      }

      classfile.addMethod(minfo);
   }

   private void addWriteMethod(ClassFile classfile, FieldInfo finfo) throws CannotCompileException, BadBytecode {
      ConstPool cp = classfile.getConstPool();
      int this_class_index = cp.getThisClassInfo();
      String desc = "(" + finfo.getDescriptor() + ")V";
      MethodInfo minfo = new MethodInfo(cp, "$javassist_write_" + finfo.getName(), desc);
      Bytecode code = new Bytecode(cp, 6, 3);
      code.addAload(0);
      int enabled_class_index = cp.addClassInfo(FIELD_HANDLED_TYPE_NAME);
      code.addInvokeinterface(enabled_class_index, "getFieldHandler", GETFIELDHANDLER_METHOD_DESCRIPTOR, 1);
      code.addOpcode(199);
      code.addIndex(9);
      code.addAload(0);
      addTypeDependDataLoad(code, finfo.getDescriptor(), 1);
      code.addOpcode(181);
      int base_field_index = cp.addFieldrefInfo(this_class_index, finfo.getName(), finfo.getDescriptor());
      code.addIndex(base_field_index);
      code.growStack(-Descriptor.dataSize(finfo.getDescriptor()));
      code.addOpcode(177);
      code.addAload(0);
      code.addOpcode(89);
      code.addInvokeinterface(enabled_class_index, "getFieldHandler", GETFIELDHANDLER_METHOD_DESCRIPTOR, 1);
      code.addAload(0);
      code.addLdc(finfo.getName());
      code.addAload(0);
      code.addOpcode(180);
      code.addIndex(base_field_index);
      code.growStack(Descriptor.dataSize(finfo.getDescriptor()) - 1);
      addTypeDependDataLoad(code, finfo.getDescriptor(), 1);
      addInvokeFieldHandlerMethod(classfile, code, finfo.getDescriptor(), false);
      code.addOpcode(181);
      code.addIndex(base_field_index);
      code.growStack(-Descriptor.dataSize(finfo.getDescriptor()));
      code.addOpcode(177);
      minfo.setCodeAttribute(code.toCodeAttribute());
      minfo.setAccessFlags(1);
      CodeAttribute codeAttribute = minfo.getCodeAttribute();
      if (codeAttribute != null) {
         StackMapTable smt = MapMaker.make(this.classPool, minfo);
         codeAttribute.setAttribute(smt);
      }

      classfile.addMethod(minfo);
   }

   private void transformInvokevirtualsIntoPutAndGetfields(ClassFile classfile) throws CannotCompileException, BadBytecode {
      for(MethodInfo minfo : classfile.getMethods()) {
         String methodName = minfo.getName();
         if (!methodName.startsWith("$javassist_read_") && !methodName.startsWith("$javassist_write_") && !methodName.equals("getFieldHandler") && !methodName.equals("setFieldHandler")) {
            CodeAttribute codeAttr = minfo.getCodeAttribute();
            if (codeAttr != null) {
               CodeIterator iter = codeAttr.iterator();

               while(iter.hasNext()) {
                  int pos = iter.next();
                  pos = this.transformInvokevirtualsIntoGetfields(classfile, iter, pos);
                  this.transformInvokevirtualsIntoPutfields(classfile, iter, pos);
               }

               StackMapTable smt = MapMaker.make(this.classPool, minfo);
               codeAttr.setAttribute(smt);
            }
         }
      }

   }

   private int transformInvokevirtualsIntoGetfields(ClassFile classfile, CodeIterator iter, int pos) {
      ConstPool cp = classfile.getConstPool();
      int c = iter.byteAt(pos);
      if (c != 180) {
         return pos;
      } else {
         int index = iter.u16bitAt(pos + 1);
         String fieldName = cp.getFieldrefName(index);
         String className = cp.getFieldrefClassName(index);
         if (!this.filter.handleReadAccess(className, fieldName)) {
            return pos;
         } else {
            String desc = "()" + cp.getFieldrefType(index);
            int read_method_index = cp.addMethodrefInfo(cp.getThisClassInfo(), "$javassist_read_" + fieldName, desc);
            iter.writeByte(182, pos);
            iter.write16bit(read_method_index, pos + 1);
            return pos;
         }
      }
   }

   private int transformInvokevirtualsIntoPutfields(ClassFile classfile, CodeIterator iter, int pos) {
      ConstPool cp = classfile.getConstPool();
      int c = iter.byteAt(pos);
      if (c != 181) {
         return pos;
      } else {
         int index = iter.u16bitAt(pos + 1);
         String fieldName = cp.getFieldrefName(index);
         String className = cp.getFieldrefClassName(index);
         if (!this.filter.handleWriteAccess(className, fieldName)) {
            return pos;
         } else {
            String desc = "(" + cp.getFieldrefType(index) + ")V";
            int write_method_index = cp.addMethodrefInfo(cp.getThisClassInfo(), "$javassist_write_" + fieldName, desc);
            iter.writeByte(182, pos);
            iter.write16bit(write_method_index, pos + 1);
            return pos;
         }
      }
   }

   private static void addInvokeFieldHandlerMethod(ClassFile classfile, Bytecode code, String typeName, boolean isReadMethod) {
      ConstPool cp = classfile.getConstPool();
      int callback_type_index = cp.addClassInfo(FIELD_HANDLER_TYPE_NAME);
      if ((typeName.charAt(0) != 'L' || typeName.charAt(typeName.length() - 1) != ';') && typeName.charAt(0) != '[') {
         if (typeName.equals("Z")) {
            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readBoolean", "(Ljava/lang/Object;Ljava/lang/String;Z)Z", 4);
            } else {
               code.addInvokeinterface(callback_type_index, "writeBoolean", "(Ljava/lang/Object;Ljava/lang/String;ZZ)Z", 5);
            }
         } else if (typeName.equals("B")) {
            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readByte", "(Ljava/lang/Object;Ljava/lang/String;B)B", 4);
            } else {
               code.addInvokeinterface(callback_type_index, "writeByte", "(Ljava/lang/Object;Ljava/lang/String;BB)B", 5);
            }
         } else if (typeName.equals("C")) {
            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readChar", "(Ljava/lang/Object;Ljava/lang/String;C)C", 4);
            } else {
               code.addInvokeinterface(callback_type_index, "writeChar", "(Ljava/lang/Object;Ljava/lang/String;CC)C", 5);
            }
         } else if (typeName.equals("I")) {
            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readInt", "(Ljava/lang/Object;Ljava/lang/String;I)I", 4);
            } else {
               code.addInvokeinterface(callback_type_index, "writeInt", "(Ljava/lang/Object;Ljava/lang/String;II)I", 5);
            }
         } else if (typeName.equals("S")) {
            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readShort", "(Ljava/lang/Object;Ljava/lang/String;S)S", 4);
            } else {
               code.addInvokeinterface(callback_type_index, "writeShort", "(Ljava/lang/Object;Ljava/lang/String;SS)S", 5);
            }
         } else if (typeName.equals("D")) {
            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readDouble", "(Ljava/lang/Object;Ljava/lang/String;D)D", 5);
            } else {
               code.addInvokeinterface(callback_type_index, "writeDouble", "(Ljava/lang/Object;Ljava/lang/String;DD)D", 7);
            }
         } else if (typeName.equals("F")) {
            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readFloat", "(Ljava/lang/Object;Ljava/lang/String;F)F", 4);
            } else {
               code.addInvokeinterface(callback_type_index, "writeFloat", "(Ljava/lang/Object;Ljava/lang/String;FF)F", 5);
            }
         } else {
            if (!typeName.equals("J")) {
               throw new RuntimeException("bad type: " + typeName);
            }

            if (isReadMethod) {
               code.addInvokeinterface(callback_type_index, "readLong", "(Ljava/lang/Object;Ljava/lang/String;J)J", 5);
            } else {
               code.addInvokeinterface(callback_type_index, "writeLong", "(Ljava/lang/Object;Ljava/lang/String;JJ)J", 7);
            }
         }
      } else {
         int indexOfL = typeName.indexOf(76);
         String type;
         if (indexOfL == 0) {
            type = typeName.substring(1, typeName.length() - 1);
            type = type.replace('/', '.');
         } else if (indexOfL == -1) {
            type = typeName;
         } else {
            type = typeName.replace('/', '.');
         }

         if (isReadMethod) {
            code.addInvokeinterface(callback_type_index, "readObject", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;)Ljava/lang/Object;", 4);
            code.addCheckcast(type);
         } else {
            code.addInvokeinterface(callback_type_index, "writeObject", "(Ljava/lang/Object;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", 5);
            code.addCheckcast(type);
         }
      }

   }

   private static void addTypeDependDataLoad(Bytecode code, String typeName, int i) {
      if ((typeName.charAt(0) != 'L' || typeName.charAt(typeName.length() - 1) != ';') && typeName.charAt(0) != '[') {
         if (!typeName.equals("Z") && !typeName.equals("B") && !typeName.equals("C") && !typeName.equals("I") && !typeName.equals("S")) {
            if (typeName.equals("D")) {
               code.addDload(i);
            } else if (typeName.equals("F")) {
               code.addFload(i);
            } else {
               if (!typeName.equals("J")) {
                  throw new RuntimeException("bad type: " + typeName);
               }

               code.addLload(i);
            }
         } else {
            code.addIload(i);
         }
      } else {
         code.addAload(i);
      }

   }

   private static void addTypeDependDataStore(Bytecode code, String typeName, int i) {
      if ((typeName.charAt(0) != 'L' || typeName.charAt(typeName.length() - 1) != ';') && typeName.charAt(0) != '[') {
         if (!typeName.equals("Z") && !typeName.equals("B") && !typeName.equals("C") && !typeName.equals("I") && !typeName.equals("S")) {
            if (typeName.equals("D")) {
               code.addDstore(i);
            } else if (typeName.equals("F")) {
               code.addFstore(i);
            } else {
               if (!typeName.equals("J")) {
                  throw new RuntimeException("bad type: " + typeName);
               }

               code.addLstore(i);
            }
         } else {
            code.addIstore(i);
         }
      } else {
         code.addAstore(i);
      }

   }

   private static void addTypeDependDataReturn(Bytecode code, String typeName) {
      if ((typeName.charAt(0) != 'L' || typeName.charAt(typeName.length() - 1) != ';') && typeName.charAt(0) != '[') {
         if (!typeName.equals("Z") && !typeName.equals("B") && !typeName.equals("C") && !typeName.equals("I") && !typeName.equals("S")) {
            if (typeName.equals("D")) {
               code.addOpcode(175);
            } else if (typeName.equals("F")) {
               code.addOpcode(174);
            } else {
               if (!typeName.equals("J")) {
                  throw new RuntimeException("bad type: " + typeName);
               }

               code.addOpcode(173);
            }
         } else {
            code.addOpcode(172);
         }
      } else {
         code.addOpcode(176);
      }

   }

   static {
      HANDLER_FIELD_DESCRIPTOR = 'L' + FIELD_HANDLER_TYPE_NAME.replace('.', '/') + ';';
      GETFIELDHANDLER_METHOD_DESCRIPTOR = "()" + HANDLER_FIELD_DESCRIPTOR;
      SETFIELDHANDLER_METHOD_DESCRIPTOR = "(" + HANDLER_FIELD_DESCRIPTOR + ")V";
   }
}
