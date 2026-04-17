package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.hibernate.annotations.common.reflection.XMethod;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;

public class JavaXMethod extends JavaXMember implements XMethod {
   static JavaXMethod create(Member member, TypeEnvironment context, JavaReflectionManager factory) {
      Type propType = typeOf(member, context);
      JavaXType xType = factory.toXType(context, propType);
      return new JavaXMethod(member, propType, context, factory, xType);
   }

   private JavaXMethod(Member member, Type type, TypeEnvironment env, JavaReflectionManager factory, JavaXType xType) {
      super(member, type, env, factory, xType);

      assert member instanceof Method;

   }

   public String getName() {
      return this.getMember().getName();
   }

   public Object invoke(Object target, Object... parameters) {
      try {
         return ((Method)this.getMember()).invoke(target, parameters);
      } catch (NullPointerException e) {
         throw new IllegalArgumentException("Invoking " + this.getName() + " on a  null object", e);
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("Invoking " + this.getName() + " with wrong parameters", e);
      } catch (Exception e) {
         throw new IllegalStateException("Unable to invoke " + this.getName(), e);
      }
   }
}
