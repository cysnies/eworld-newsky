package com.comphenix.net.sf.cglib.core;

import com.comphenix.net.sf.cglib.asm.ClassReader;
import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import com.comphenix.net.sf.cglib.asm.ClassWriter;
import com.comphenix.net.sf.cglib.asm.util.TraceClassVisitor;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;

public class DebuggingClassWriter extends ClassWriter {
   public static final String DEBUG_LOCATION_PROPERTY = "cglib.debugLocation";
   private static String debugLocation = System.getProperty("cglib.debugLocation");
   private static boolean traceEnabled;
   private String className;
   private String superName;

   public DebuggingClassWriter(int flags) {
      super(flags);
   }

   public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
      this.className = name.replace('/', '.');
      this.superName = superName.replace('/', '.');
      super.visit(version, access, name, signature, superName, interfaces);
   }

   public String getClassName() {
      return this.className;
   }

   public String getSuperName() {
      return this.superName;
   }

   public byte[] toByteArray() {
      return (byte[])AccessController.doPrivileged(new PrivilegedAction() {
         public Object run() {
            byte[] b = DebuggingClassWriter.super.toByteArray();
            if (DebuggingClassWriter.debugLocation != null) {
               String dirs = DebuggingClassWriter.this.className.replace('.', File.separatorChar);

               try {
                  (new File(DebuggingClassWriter.debugLocation + File.separatorChar + dirs)).getParentFile().mkdirs();
                  File file = new File(new File(DebuggingClassWriter.debugLocation), dirs + ".class");
                  OutputStream out = new BufferedOutputStream(new FileOutputStream(file));

                  try {
                     out.write(b);
                  } finally {
                     out.close();
                  }

                  if (DebuggingClassWriter.traceEnabled) {
                     file = new File(new File(DebuggingClassWriter.debugLocation), dirs + ".asm");
                     out = new BufferedOutputStream(new FileOutputStream(file));

                     try {
                        ClassReader cr = new ClassReader(b);
                        PrintWriter pw = new PrintWriter(new OutputStreamWriter(out));
                        TraceClassVisitor tcv = new TraceClassVisitor((ClassVisitor)null, pw);
                        cr.accept(tcv, 0);
                        pw.flush();
                     } finally {
                        out.close();
                     }
                  }
               } catch (IOException e) {
                  throw new CodeGenerationException(e);
               }
            }

            return b;
         }
      });
   }

   static {
      if (debugLocation != null) {
         System.err.println("CGLIB debugging enabled, writing to '" + debugLocation + "'");

         try {
            Class.forName("com.comphenix.net.sf.cglib.asm.util.TraceClassVisitor");
            traceEnabled = true;
         } catch (Throwable var1) {
         }
      }

   }
}
