package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.MethodInfo;
import com.comphenix.net.sf.cglib.core.Signature;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import java.util.List;

class FixedValueGenerator implements CallbackGenerator {
   public static final FixedValueGenerator INSTANCE = new FixedValueGenerator();
   private static final Type FIXED_VALUE = TypeUtils.parseType("com.comphenix.net.sf.cglib.proxy.FixedValue");
   private static final Signature LOAD_OBJECT = TypeUtils.parseSignature("Object loadObject()");

   public void generate(ClassEmitter ce, CallbackGenerator.Context context, List methods) {
      for(MethodInfo method : methods) {
         CodeEmitter e = context.beginMethod(ce, method);
         context.emitCallback(e, context.getIndex(method));
         e.invoke_interface(FIXED_VALUE, LOAD_OBJECT);
         e.unbox_or_zero(e.getReturnType());
         e.return_value();
         e.end_method();
      }

   }

   public void generateStatic(CodeEmitter e, CallbackGenerator.Context context, List methods) {
   }
}
