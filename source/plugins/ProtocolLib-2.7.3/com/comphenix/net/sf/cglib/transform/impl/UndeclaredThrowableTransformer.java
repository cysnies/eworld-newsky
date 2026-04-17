package com.comphenix.net.sf.cglib.transform.impl;

import com.comphenix.net.sf.cglib.asm.Type;
import com.comphenix.net.sf.cglib.core.Block;
import com.comphenix.net.sf.cglib.core.CodeEmitter;
import com.comphenix.net.sf.cglib.core.Constants;
import com.comphenix.net.sf.cglib.core.EmitUtils;
import com.comphenix.net.sf.cglib.core.Signature;
import com.comphenix.net.sf.cglib.core.TypeUtils;
import com.comphenix.net.sf.cglib.transform.ClassEmitterTransformer;
import java.lang.reflect.Constructor;

public class UndeclaredThrowableTransformer extends ClassEmitterTransformer {
   private Type wrapper;
   // $FF: synthetic field
   static Class class$java$lang$Throwable;

   public UndeclaredThrowableTransformer(Class wrapper) {
      this.wrapper = Type.getType(wrapper);
      boolean found = false;
      Constructor[] cstructs = wrapper.getConstructors();

      for(int i = 0; i < cstructs.length; ++i) {
         Class[] types = cstructs[i].getParameterTypes();
         if (types.length == 1 && types[0].equals(class$java$lang$Throwable == null ? (class$java$lang$Throwable = class$("java.lang.Throwable")) : class$java$lang$Throwable)) {
            found = true;
            break;
         }
      }

      if (!found) {
         throw new IllegalArgumentException(wrapper + " does not have a single-arg constructor that takes a Throwable");
      }
   }

   public CodeEmitter begin_method(int access, Signature sig, final Type[] exceptions) {
      CodeEmitter e = super.begin_method(access, sig, exceptions);
      return !TypeUtils.isAbstract(access) && !sig.equals(Constants.SIG_STATIC) ? new CodeEmitter(e) {
         private Block handler = this.begin_block();

         public void visitMaxs(int maxStack, int maxLocals) {
            this.handler.end();
            EmitUtils.wrap_undeclared_throwable(this, this.handler, exceptions, UndeclaredThrowableTransformer.this.wrapper);
            super.visitMaxs(maxStack, maxLocals);
         }
      } : e;
   }

   // $FF: synthetic method
   static Class class$(String x0) {
      try {
         return Class.forName(x0);
      } catch (ClassNotFoundException x1) {
         throw new NoClassDefFoundError(x1.getMessage());
      }
   }
}
