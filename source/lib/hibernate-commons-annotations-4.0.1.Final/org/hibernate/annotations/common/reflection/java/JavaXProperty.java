package org.hibernate.annotations.common.reflection.java;

import java.beans.Introspector;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.hibernate.annotations.common.reflection.XProperty;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;

class JavaXProperty extends JavaXMember implements XProperty {
   static JavaXProperty create(Member member, TypeEnvironment context, JavaReflectionManager factory) {
      Type propType = typeOf(member, context);
      JavaXType xType = factory.toXType(context, propType);
      return new JavaXProperty(member, propType, context, factory, xType);
   }

   private JavaXProperty(Member member, Type type, TypeEnvironment env, JavaReflectionManager factory, JavaXType xType) {
      super(member, type, env, factory, xType);

      assert member instanceof Field || member instanceof Method;

   }

   public String getName() {
      String fullName = this.getMember().getName();
      if (this.getMember() instanceof Method) {
         if (fullName.startsWith("get")) {
            return Introspector.decapitalize(fullName.substring("get".length()));
         } else if (fullName.startsWith("is")) {
            return Introspector.decapitalize(fullName.substring("is".length()));
         } else {
            throw new RuntimeException("Method " + fullName + " is not a property getter");
         }
      } else {
         return fullName;
      }
   }

   public Object invoke(Object target, Object... parameters) {
      if (parameters.length != 0) {
         throw new IllegalArgumentException("An XProperty cannot have invoke parameters");
      } else {
         try {
            return this.getMember() instanceof Method ? ((Method)this.getMember()).invoke(target) : ((Field)this.getMember()).get(target);
         } catch (NullPointerException e) {
            throw new IllegalArgumentException("Invoking " + this.getName() + " on a  null object", e);
         } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invoking " + this.getName() + " with wrong parameters", e);
         } catch (Exception e) {
            throw new IllegalStateException("Unable to invoke " + this.getName(), e);
         }
      }
   }

   public String toString() {
      return this.getName();
   }
}
