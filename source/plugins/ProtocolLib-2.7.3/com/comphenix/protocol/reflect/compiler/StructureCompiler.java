package com.comphenix.protocol.reflect.compiler;

import com.comphenix.net.sf.cglib.asm.ClassWriter;
import com.comphenix.net.sf.cglib.asm.FieldVisitor;
import com.comphenix.net.sf.cglib.asm.Label;
import com.comphenix.net.sf.cglib.asm.MethodVisitor;
import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.error.Report;
import com.comphenix.protocol.error.ReportType;
import com.comphenix.protocol.reflect.StructureModifier;
import com.google.common.base.Objects;
import com.google.common.primitives.Primitives;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class StructureCompiler {
   public static final ReportType REPORT_TOO_MANY_GENERATED_CLASSES = new ReportType("Generated too many classes (count: %s)");
   private static volatile Method defineMethod;
   private Map compiledCache = new ConcurrentHashMap();
   private ClassLoader loader;
   private static String PACKAGE_NAME = "com/comphenix/protocol/reflect/compiler";
   private static String SUPER_CLASS = "com/comphenix/protocol/reflect/StructureModifier";
   private static String COMPILED_CLASS;
   private static String FIELD_EXCEPTION_CLASS;

   StructureCompiler(ClassLoader loader) {
      this.loader = loader;
   }

   public boolean lookupClassLoader(StructureModifier source) {
      StructureKey key = new StructureKey(source);
      if (this.compiledCache.containsKey(key)) {
         return true;
      } else {
         try {
            String className = this.getCompiledName(source);
            Class<?> before = this.loader.loadClass(PACKAGE_NAME.replace('/', '.') + "." + className);
            if (before != null) {
               this.compiledCache.put(key, before);
               return true;
            }
         } catch (ClassNotFoundException var5) {
         }

         return false;
      }
   }

   public synchronized StructureModifier compile(StructureModifier source) {
      if (!this.isAnyPublic(source.getFields())) {
         return source;
      } else {
         StructureKey key = new StructureKey(source);
         Class<?> compiledClass = (Class)this.compiledCache.get(key);
         if (!this.compiledCache.containsKey(key)) {
            compiledClass = this.generateClass(source);
            this.compiledCache.put(key, compiledClass);
         }

         try {
            return (StructureModifier)compiledClass.getConstructor(StructureModifier.class, StructureCompiler.class).newInstance(source, this);
         } catch (OutOfMemoryError e) {
            ProtocolLibrary.getErrorReporter().reportWarning(this, (Report.ReportBuilder)Report.newBuilder(REPORT_TOO_MANY_GENERATED_CLASSES).messageParam(this.compiledCache.size()));
            throw e;
         } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Used invalid parameters in instance creation", e);
         } catch (SecurityException e) {
            throw new RuntimeException("Security limitation!", e);
         } catch (InstantiationException e) {
            throw new RuntimeException("Error occured while instancing generated class.", e);
         } catch (IllegalAccessException e) {
            throw new RuntimeException("Security limitation! Cannot create instance of dynamic class.", e);
         } catch (InvocationTargetException e) {
            throw new RuntimeException("Error occured while instancing generated class.", e);
         } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Cannot happen.", e);
         }
      }
   }

   private String getSafeTypeName(Class type) {
      return type.getCanonicalName().replace("[]", "Array").replace(".", "_");
   }

   private String getCompiledName(StructureModifier source) {
      Class<?> targetType = source.getTargetType();
      return "CompiledStructure$" + this.getSafeTypeName(targetType) + "$" + this.getSafeTypeName(source.getFieldType());
   }

   private Class generateClass(StructureModifier source) {
      ClassWriter cw = new ClassWriter(0);
      Class<?> targetType = source.getTargetType();
      String className = this.getCompiledName(source);
      String targetSignature = Type.getDescriptor(targetType);
      String targetName = targetType.getName().replace('.', '/');
      cw.visit(50, 33, PACKAGE_NAME + "/" + className, (String)null, COMPILED_CLASS, (String[])null);
      this.createFields(cw, targetSignature);
      this.createConstructor(cw, className, targetSignature, targetName);
      this.createReadMethod(cw, className, source.getFields(), targetSignature, targetName);
      this.createWriteMethod(cw, className, source.getFields(), targetSignature, targetName);
      cw.visitEnd();
      byte[] data = cw.toByteArray();

      try {
         if (defineMethod == null) {
            Method defined = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, Integer.TYPE, Integer.TYPE);
            defined.setAccessible(true);
            defineMethod = defined;
         }

         Class clazz = (Class)defineMethod.invoke(this.loader, null, data, 0, data.length);
         return clazz;
      } catch (SecurityException e) {
         throw new RuntimeException("Cannot use reflection to dynamically load a class.", e);
      } catch (NoSuchMethodException e) {
         throw new IllegalStateException("Incompatible JVM.", e);
      } catch (IllegalArgumentException e) {
         throw new IllegalStateException("Cannot call defineMethod - wrong JVM?", e);
      } catch (IllegalAccessException e) {
         throw new RuntimeException("Security limitation! Cannot dynamically load class.", e);
      } catch (InvocationTargetException e) {
         throw new RuntimeException("Error occured in code generator.", e);
      }
   }

   private boolean isAnyPublic(List fields) {
      for(int i = 0; i < fields.size(); ++i) {
         if (this.isPublic((Field)fields.get(i))) {
            return true;
         }
      }

      return false;
   }

   private boolean isPublic(Field field) {
      return Modifier.isPublic(field.getModifiers());
   }

   private boolean isNonFinal(Field field) {
      return !Modifier.isFinal(field.getModifiers());
   }

   private void createFields(ClassWriter cw, String targetSignature) {
      FieldVisitor typedField = cw.visitField(2, "typedTarget", targetSignature, (String)null, (Object)null);
      typedField.visitEnd();
   }

   private void createWriteMethod(ClassWriter cw, String className, List fields, String targetSignature, String targetName) {
      String methodDescriptor = "(ILjava/lang/Object;)L" + SUPER_CLASS + ";";
      String methodSignature = "(ILjava/lang/Object;)L" + SUPER_CLASS + "<Ljava/lang/Object;>;";
      MethodVisitor mv = cw.visitMethod(4, "writeGenerated", methodDescriptor, methodSignature, new String[]{FIELD_EXCEPTION_CLASS});
      BoxingHelper boxingHelper = new BoxingHelper(mv);
      String generatedClassName = PACKAGE_NAME + "/" + className;
      mv.visitCode();
      mv.visitVarInsn(25, 0);
      mv.visitFieldInsn(180, generatedClassName, "typedTarget", targetSignature);
      mv.visitVarInsn(58, 3);
      mv.visitVarInsn(21, 1);
      Label[] labels = new Label[fields.size()];
      Label errorLabel = new Label();
      Label returnLabel = new Label();

      for(int i = 0; i < fields.size(); ++i) {
         labels[i] = new Label();
      }

      mv.visitTableSwitchInsn(0, labels.length - 1, errorLabel, labels);

      for(int i = 0; i < fields.size(); ++i) {
         Field field = (Field)fields.get(i);
         Class<?> outputType = field.getType();
         Class<?> inputType = Primitives.wrap(outputType);
         String typeDescriptor = Type.getDescriptor(outputType);
         String inputPath = inputType.getName().replace('.', '/');
         mv.visitLabel(labels[i]);
         if (i == 0) {
            mv.visitFrame(1, 1, new Object[]{targetName}, 0, (Object[])null);
         } else {
            mv.visitFrame(3, 0, (Object[])null, 0, (Object[])null);
         }

         if (this.isPublic(field) && this.isNonFinal(field)) {
            mv.visitVarInsn(25, 3);
            mv.visitVarInsn(25, 2);
            if (!outputType.isPrimitive()) {
               mv.visitTypeInsn(192, inputPath);
            } else {
               boxingHelper.unbox(Type.getType(outputType));
            }

            mv.visitFieldInsn(181, targetName, field.getName(), typeDescriptor);
         } else {
            mv.visitVarInsn(25, 0);
            mv.visitVarInsn(21, 1);
            mv.visitVarInsn(25, 2);
            mv.visitMethodInsn(182, generatedClassName, "writeReflected", "(ILjava/lang/Object;)V");
         }

         mv.visitJumpInsn(167, returnLabel);
      }

      mv.visitLabel(errorLabel);
      mv.visitFrame(3, 0, (Object[])null, 0, (Object[])null);
      mv.visitTypeInsn(187, FIELD_EXCEPTION_CLASS);
      mv.visitInsn(89);
      mv.visitTypeInsn(187, "java/lang/StringBuilder");
      mv.visitInsn(89);
      mv.visitLdcInsn("Invalid index ");
      mv.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
      mv.visitVarInsn(21, 1);
      mv.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
      mv.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
      mv.visitMethodInsn(183, FIELD_EXCEPTION_CLASS, "<init>", "(Ljava/lang/String;)V");
      mv.visitInsn(191);
      mv.visitLabel(returnLabel);
      mv.visitFrame(3, 0, (Object[])null, 0, (Object[])null);
      mv.visitVarInsn(25, 0);
      mv.visitInsn(176);
      mv.visitMaxs(5, 4);
      mv.visitEnd();
   }

   private void createReadMethod(ClassWriter cw, String className, List fields, String targetSignature, String targetName) {
      MethodVisitor mv = cw.visitMethod(4, "readGenerated", "(I)Ljava/lang/Object;", (String)null, new String[]{"com/comphenix/protocol/reflect/FieldAccessException"});
      BoxingHelper boxingHelper = new BoxingHelper(mv);
      String generatedClassName = PACKAGE_NAME + "/" + className;
      mv.visitCode();
      mv.visitVarInsn(25, 0);
      mv.visitFieldInsn(180, generatedClassName, "typedTarget", targetSignature);
      mv.visitVarInsn(58, 2);
      mv.visitVarInsn(21, 1);
      Label[] labels = new Label[fields.size()];
      Label errorLabel = new Label();

      for(int i = 0; i < fields.size(); ++i) {
         labels[i] = new Label();
      }

      mv.visitTableSwitchInsn(0, fields.size() - 1, errorLabel, labels);

      for(int i = 0; i < fields.size(); ++i) {
         Field field = (Field)fields.get(i);
         Class<?> outputType = field.getType();
         String typeDescriptor = Type.getDescriptor(outputType);
         mv.visitLabel(labels[i]);
         if (i == 0) {
            mv.visitFrame(1, 1, new Object[]{targetName}, 0, (Object[])null);
         } else {
            mv.visitFrame(3, 0, (Object[])null, 0, (Object[])null);
         }

         if (this.isPublic(field)) {
            mv.visitVarInsn(25, 2);
            mv.visitFieldInsn(180, targetName, field.getName(), typeDescriptor);
            boxingHelper.box(Type.getType(outputType));
         } else {
            mv.visitVarInsn(25, 0);
            mv.visitVarInsn(21, 1);
            mv.visitMethodInsn(182, generatedClassName, "readReflected", "(I)Ljava/lang/Object;");
         }

         mv.visitInsn(176);
      }

      mv.visitLabel(errorLabel);
      mv.visitFrame(3, 0, (Object[])null, 0, (Object[])null);
      mv.visitTypeInsn(187, FIELD_EXCEPTION_CLASS);
      mv.visitInsn(89);
      mv.visitTypeInsn(187, "java/lang/StringBuilder");
      mv.visitInsn(89);
      mv.visitLdcInsn("Invalid index ");
      mv.visitMethodInsn(183, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V");
      mv.visitVarInsn(21, 1);
      mv.visitMethodInsn(182, "java/lang/StringBuilder", "append", "(I)Ljava/lang/StringBuilder;");
      mv.visitMethodInsn(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;");
      mv.visitMethodInsn(183, FIELD_EXCEPTION_CLASS, "<init>", "(Ljava/lang/String;)V");
      mv.visitInsn(191);
      mv.visitMaxs(5, 3);
      mv.visitEnd();
   }

   private void createConstructor(ClassWriter cw, String className, String targetSignature, String targetName) {
      MethodVisitor mv = cw.visitMethod(1, "<init>", "(L" + SUPER_CLASS + ";L" + PACKAGE_NAME + "/StructureCompiler;)V", "(L" + SUPER_CLASS + "<Ljava/lang/Object;>;L" + PACKAGE_NAME + "/StructureCompiler;)V", (String[])null);
      String fullClassName = PACKAGE_NAME + "/" + className;
      mv.visitCode();
      mv.visitVarInsn(25, 0);
      mv.visitMethodInsn(183, COMPILED_CLASS, "<init>", "()V");
      mv.visitVarInsn(25, 0);
      mv.visitVarInsn(25, 1);
      mv.visitMethodInsn(182, fullClassName, "initialize", "(L" + SUPER_CLASS + ";)V");
      mv.visitVarInsn(25, 0);
      mv.visitVarInsn(25, 1);
      mv.visitMethodInsn(182, SUPER_CLASS, "getTarget", "()Ljava/lang/Object;");
      mv.visitFieldInsn(181, fullClassName, "target", "Ljava/lang/Object;");
      mv.visitVarInsn(25, 0);
      mv.visitVarInsn(25, 0);
      mv.visitFieldInsn(180, fullClassName, "target", "Ljava/lang/Object;");
      mv.visitTypeInsn(192, targetName);
      mv.visitFieldInsn(181, fullClassName, "typedTarget", targetSignature);
      mv.visitVarInsn(25, 0);
      mv.visitVarInsn(25, 2);
      mv.visitFieldInsn(181, fullClassName, "compiler", "L" + PACKAGE_NAME + "/StructureCompiler;");
      mv.visitInsn(177);
      mv.visitMaxs(2, 3);
      mv.visitEnd();
   }

   static {
      COMPILED_CLASS = PACKAGE_NAME + "/CompiledStructureModifier";
      FIELD_EXCEPTION_CLASS = "com/comphenix/protocol/reflect/FieldAccessException";
   }

   static class StructureKey {
      private Class targetType;
      private Class fieldType;

      public StructureKey(StructureModifier source) {
         this(source.getTargetType(), source.getFieldType());
      }

      public StructureKey(Class targetType, Class fieldType) {
         this.targetType = targetType;
         this.fieldType = fieldType;
      }

      public int hashCode() {
         return Objects.hashCode(new Object[]{this.targetType, this.fieldType});
      }

      public boolean equals(Object obj) {
         if (!(obj instanceof StructureKey)) {
            return false;
         } else {
            StructureKey other = (StructureKey)obj;
            return Objects.equal(this.targetType, other.targetType) && Objects.equal(this.fieldType, other.fieldType);
         }
      }
   }
}
