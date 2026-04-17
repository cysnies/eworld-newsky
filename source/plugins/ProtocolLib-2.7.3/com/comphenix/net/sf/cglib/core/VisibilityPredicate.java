package com.comphenix.net.sf.cglib.core;

import com.comphenix.net.sf.cglib.asm.Type;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public class VisibilityPredicate implements Predicate {
   private boolean protectedOk;
   private String pkg;

   public VisibilityPredicate(Class source, boolean protectedOk) {
      this.protectedOk = protectedOk;
      this.pkg = TypeUtils.getPackageName(Type.getType(source));
   }

   public boolean evaluate(Object arg) {
      int mod = arg instanceof Member ? ((Member)arg).getModifiers() : (Integer)arg;
      if (Modifier.isPrivate(mod)) {
         return false;
      } else if (Modifier.isPublic(mod)) {
         return true;
      } else {
         return Modifier.isProtected(mod) ? this.protectedOk : this.pkg.equals(TypeUtils.getPackageName(Type.getType(((Member)arg).getDeclaringClass())));
      }
   }
}
