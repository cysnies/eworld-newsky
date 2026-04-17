package org.hibernate.annotations.common.reflection.java;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.hibernate.annotations.common.reflection.XClass;
import org.hibernate.annotations.common.reflection.XMember;
import org.hibernate.annotations.common.reflection.java.generics.TypeEnvironment;

public abstract class JavaXMember extends JavaXAnnotatedElement implements XMember {
   private final Type type;
   private final TypeEnvironment env;
   private final JavaXType xType;

   protected static Type typeOf(Member member, TypeEnvironment env) {
      if (member instanceof Field) {
         return env.bind(((Field)member).getGenericType());
      } else if (member instanceof Method) {
         return env.bind(((Method)member).getGenericReturnType());
      } else {
         throw new IllegalArgumentException("Member " + member + " is neither a field nor a method");
      }
   }

   protected JavaXMember(Member member, Type type, TypeEnvironment env, JavaReflectionManager factory, JavaXType xType) {
      super((AnnotatedElement)member, factory);
      this.type = type;
      this.env = env;
      this.xType = xType;
   }

   public XClass getType() {
      return this.xType.getType();
   }

   public abstract String getName();

   protected Type getJavaType() {
      return this.env.bind(this.type);
   }

   protected TypeEnvironment getTypeEnvironment() {
      return this.env;
   }

   protected Member getMember() {
      return (Member)this.toAnnotatedElement();
   }

   public Class getCollectionClass() {
      return this.xType.getCollectionClass();
   }

   public XClass getClassOrElementClass() {
      return this.xType.getClassOrElementClass();
   }

   public XClass getElementClass() {
      return this.xType.getElementClass();
   }

   public XClass getMapKey() {
      return this.xType.getMapKey();
   }

   public boolean isArray() {
      return this.xType.isArray();
   }

   public boolean isCollection() {
      return this.xType.isCollection();
   }

   public int getModifiers() {
      return this.getMember().getModifiers();
   }

   public final boolean isTypeResolved() {
      return this.xType.isResolved();
   }

   public void setAccessible(boolean accessible) {
      ((AccessibleObject)this.getMember()).setAccessible(accessible);
   }
}
