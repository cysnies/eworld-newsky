package com.comphenix.net.sf.cglib.proxy;

import com.comphenix.net.sf.cglib.asm.Type;

class CallbackInfo {
   private Class cls;
   private CallbackGenerator generator;
   private Type type;
   private static final CallbackInfo[] CALLBACKS;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$NoOp;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$MethodInterceptor;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$InvocationHandler;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$LazyLoader;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$Dispatcher;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$FixedValue;
   // $FF: synthetic field
   static Class class$net$sf$cglib$proxy$ProxyRefDispatcher;

   public static Type[] determineTypes(Class[] callbackTypes) {
      Type[] types = new Type[callbackTypes.length];

      for(int i = 0; i < types.length; ++i) {
         types[i] = determineType(callbackTypes[i]);
      }

      return types;
   }

   public static Type[] determineTypes(Callback[] callbacks) {
      Type[] types = new Type[callbacks.length];

      for(int i = 0; i < types.length; ++i) {
         types[i] = determineType(callbacks[i]);
      }

      return types;
   }

   public static CallbackGenerator[] getGenerators(Type[] callbackTypes) {
      CallbackGenerator[] generators = new CallbackGenerator[callbackTypes.length];

      for(int i = 0; i < generators.length; ++i) {
         generators[i] = getGenerator(callbackTypes[i]);
      }

      return generators;
   }

   private CallbackInfo(Class cls, CallbackGenerator generator) {
      this.cls = cls;
      this.generator = generator;
      this.type = Type.getType(cls);
   }

   private static Type determineType(Callback callback) {
      if (callback == null) {
         throw new IllegalStateException("Callback is null");
      } else {
         return determineType(callback.getClass());
      }
   }

   private static Type determineType(Class callbackType) {
      Class cur = null;

      for(int i = 0; i < CALLBACKS.length; ++i) {
         CallbackInfo info = CALLBACKS[i];
         if (info.cls.isAssignableFrom(callbackType)) {
            if (cur != null) {
               throw new IllegalStateException("Callback implements both " + cur + " and " + info.cls);
            }

            cur = info.cls;
         }
      }

      if (cur == null) {
         throw new IllegalStateException("Unknown callback type " + callbackType);
      } else {
         return Type.getType(cur);
      }
   }

   private static CallbackGenerator getGenerator(Type callbackType) {
      for(int i = 0; i < CALLBACKS.length; ++i) {
         CallbackInfo info = CALLBACKS[i];
         if (info.type.equals(callbackType)) {
            return info.generator;
         }
      }

      throw new IllegalStateException("Unknown callback type " + callbackType);
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }

   static {
      CALLBACKS = new CallbackInfo[]{new CallbackInfo(class$net$sf$cglib$proxy$NoOp == null ? (class$net$sf$cglib$proxy$NoOp = class$("com.comphenix.net.sf.cglib.proxy.NoOp")) : class$net$sf$cglib$proxy$NoOp, NoOpGenerator.INSTANCE), new CallbackInfo(class$net$sf$cglib$proxy$MethodInterceptor == null ? (class$net$sf$cglib$proxy$MethodInterceptor = class$("com.comphenix.net.sf.cglib.proxy.MethodInterceptor")) : class$net$sf$cglib$proxy$MethodInterceptor, MethodInterceptorGenerator.INSTANCE), new CallbackInfo(class$net$sf$cglib$proxy$InvocationHandler == null ? (class$net$sf$cglib$proxy$InvocationHandler = class$("com.comphenix.net.sf.cglib.proxy.InvocationHandler")) : class$net$sf$cglib$proxy$InvocationHandler, InvocationHandlerGenerator.INSTANCE), new CallbackInfo(class$net$sf$cglib$proxy$LazyLoader == null ? (class$net$sf$cglib$proxy$LazyLoader = class$("com.comphenix.net.sf.cglib.proxy.LazyLoader")) : class$net$sf$cglib$proxy$LazyLoader, LazyLoaderGenerator.INSTANCE), new CallbackInfo(class$net$sf$cglib$proxy$Dispatcher == null ? (class$net$sf$cglib$proxy$Dispatcher = class$("com.comphenix.net.sf.cglib.proxy.Dispatcher")) : class$net$sf$cglib$proxy$Dispatcher, DispatcherGenerator.INSTANCE), new CallbackInfo(class$net$sf$cglib$proxy$FixedValue == null ? (class$net$sf$cglib$proxy$FixedValue = class$("com.comphenix.net.sf.cglib.proxy.FixedValue")) : class$net$sf$cglib$proxy$FixedValue, FixedValueGenerator.INSTANCE), new CallbackInfo(class$net$sf$cglib$proxy$ProxyRefDispatcher == null ? (class$net$sf$cglib$proxy$ProxyRefDispatcher = class$("com.comphenix.net.sf.cglib.proxy.ProxyRefDispatcher")) : class$net$sf$cglib$proxy$ProxyRefDispatcher, DispatcherGenerator.PROXY_REF_INSTANCE)};
   }
}
