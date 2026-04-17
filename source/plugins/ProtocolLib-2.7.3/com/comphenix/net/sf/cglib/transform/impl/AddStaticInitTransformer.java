package com.comphenix.net.sf.cglib.transform.impl;

import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.Constants;
import com.comphenix.net.sf.cglib.core.EmitUtils;
import com.comphenix.net.sf.cglib.core.MethodInfo;
import com.comphenix.net.sf.cglib.core.ReflectUtils;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import com.comphenix.net.sf.cglib.transform.ClassEmitterTransformer;
import java.lang.reflect.Method;

public class AddStaticInitTransformer extends ClassEmitterTransformer {
   private MethodInfo info;

   public AddStaticInitTransformer(Method classInit) {
      this.info = ReflectUtils.getMethodInfo(classInit);
      if (!TypeUtils.isStatic(this.info.getModifiers())) {
         throw new IllegalArgumentException(classInit + " is not static");
      } else {
         Type[] types = this.info.getSignature().getArgumentTypes();
         if (types.length != 1 || !types[0].equals(Constants.TYPE_CLASS) || !this.info.getSignature().getReturnType().equals(Type.VOID_TYPE)) {
            throw new IllegalArgumentException(classInit + " illegal signature");
         }
      }
   }

   protected void init() {
      if (!TypeUtils.isInterface(this.getAccess())) {
         CodeEmitter e = this.getStaticHook();
         EmitUtils.load_class_this(e);
         e.invoke(this.info);
      }

   }
}
