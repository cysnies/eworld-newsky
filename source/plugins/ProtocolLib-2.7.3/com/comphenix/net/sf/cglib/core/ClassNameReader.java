package com.comphenix.net.sf.cglib.core;

import com.comphenix.net.sf.cglib.asm.ClassAdapter;
import com.comphenix.net.sf.cglib.asm.ClassReader;
import com.comphenix.net.sf.cglib.asm.ClassVisitor;
import java.util.ArrayList;
import java.util.List;

public class ClassNameReader {
   private static final EarlyExitException EARLY_EXIT = new EarlyExitException();

   private ClassNameReader() {
   }

   public static String getClassName(ClassReader r) {
      return getClassInfo(r)[0];
   }

   public static String[] getClassInfo(ClassReader r) {
      final List array = new ArrayList();

      try {
         r.accept(new ClassAdapter((ClassVisitor)null) {
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
               array.add(name.replace('/', '.'));
               if (superName != null) {
                  array.add(superName.replace('/', '.'));
               }

               for(int i = 0; i < interfaces.length; ++i) {
                  array.add(interfaces[i].replace('/', '.'));
               }

               throw ClassNameReader.EARLY_EXIT;
            }
         }, 6);
      } catch (EarlyExitException var3) {
      }

      return (String[])array.toArray(new String[0]);
   }

   private static class EarlyExitException extends RuntimeException {
      private EarlyExitException() {
      }
   }
}
