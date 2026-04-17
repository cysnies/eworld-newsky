package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.ClassEmitter;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.MethodInfo;
import com.comphenix.net.sf.cglib.core.Signature;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import java.util.List;

class DispatcherGenerator implements CallbackGenerator {
   public static final DispatcherGenerator INSTANCE = new DispatcherGenerator(false);
   public static final DispatcherGenerator PROXY_REF_INSTANCE = new DispatcherGenerator(true);
   private static final Type DISPATCHER = TypeUtils.parseType("com.comphenix.net.sf.cglib.proxy.Dispatcher");
   private static final Type PROXY_REF_DISPATCHER = TypeUtils.parseType("com.comphenix.net.sf.cglib.proxy.ProxyRefDispatcher");
   private static final Signature LOAD_OBJECT = TypeUtils.parseSignature("Object loadObject()");
   private static final Signature PROXY_REF_LOAD_OBJECT = TypeUtils.parseSignature("Object loadObject(Object)");
   private boolean proxyRef;

   private DispatcherGenerator(boolean proxyRef) {
      this.proxyRef = proxyRef;
   }

   public void generate(ClassEmitter ce, CallbackGenerator.Context context, List methods) {
      for(MethodInfo method : methods) {
         if (!TypeUtils.isProtected(method.getModifiers())) {
            CodeEmitter e = context.beginMethod(ce, method);
            context.emitCallback(e, context.getIndex(method));
            if (this.proxyRef) {
               e.load_this();
               e.invoke_interface(PROXY_REF_DISPATCHER, PROXY_REF_LOAD_OBJECT);
            } else {
               e.invoke_interface(DISPATCHER, LOAD_OBJECT);
            }

            e.checkcast(method.getClassInfo().getType());
            e.load_args();
            e.invoke(method);
            e.return_value();
            e.end_method();
         }
      }

   }

   public void generateStatic(CodeEmitter e, CallbackGenerator.Context context, List methods) {
   }
}
