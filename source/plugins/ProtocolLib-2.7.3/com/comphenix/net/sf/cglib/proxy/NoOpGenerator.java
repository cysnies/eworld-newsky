package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.EmitUtils;
import com.comphenix.net.sf.cglib.core.MethodInfo;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import java.util.List;

class NoOpGenerator implements CallbackGenerator {
   public static final NoOpGenerator INSTANCE = new NoOpGenerator();

   public void generate(ClassEmitter ce, CallbackGenerator.Context context, List methods) {
      for(MethodInfo method : methods) {
         if (TypeUtils.isBridge(method.getModifiers()) || TypeUtils.isProtected(context.getOriginalModifiers(method)) && TypeUtils.isPublic(method.getModifiers())) {
            CodeEmitter e = EmitUtils.begin_method(ce, method);
            e.load_this();
            e.load_args();
            context.emitInvoke(e, method);
            e.return_value();
            e.end_method();
         }
      }

   }

   public void generateStatic(CodeEmitter e, CallbackGenerator.Context context, List methods) {
   }
}
