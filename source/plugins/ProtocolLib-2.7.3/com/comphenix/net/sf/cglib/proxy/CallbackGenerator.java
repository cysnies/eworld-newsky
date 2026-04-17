package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.MethodInfo;
import com.comphenix.net.sf.cglib.core.Signature;
import java.util.List;

interface CallbackGenerator {
   void generate(ClassEmitter var1, Context var2, List var3) throws Exception;

   void generateStatic(CodeEmitter var1, Context var2, List var3) throws Exception;

   public interface Context {
      ClassLoader getClassLoader();

      CodeEmitter beginMethod(ClassEmitter var1, MethodInfo var2);

      int getOriginalModifiers(MethodInfo var1);

      int getIndex(MethodInfo var1);

      void emitCallback(CodeEmitter var1, int var2);

      Signature getImplSignature(MethodInfo var1);

      void emitInvoke(CodeEmitter var1, MethodInfo var2);
   }
}
