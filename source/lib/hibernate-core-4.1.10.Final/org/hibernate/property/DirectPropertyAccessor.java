package org.hibernate.property;

import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.PropertyAccessException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ReflectHelper;

public class DirectPropertyAccessor implements PropertyAccessor {
   private static Field getField(Class clazz, String name) throws PropertyNotFoundException {
      if (clazz != null && clazz != Object.class) {
         Field field;
         try {
            field = clazz.getDeclaredField(name);
         } catch (NoSuchFieldException var4) {
            field = getField(clazz, clazz.getSuperclass(), name);
         }

         if (!ReflectHelper.isPublic(clazz, field)) {
            field.setAccessible(true);
         }

         return field;
      } else {
         throw new PropertyNotFoundException("field not found: " + name);
      }
   }

   private static Field getField(Class root, Class clazz, String name) throws PropertyNotFoundException {
      if (clazz != null && clazz != Object.class) {
         Field field;
         try {
            field = clazz.getDeclaredField(name);
         } catch (NoSuchFieldException var5) {
            field = getField(root, clazz.getSuperclass(), name);
         }

         if (!ReflectHelper.isPublic(clazz, field)) {
            field.setAccessible(true);
         }

         return field;
      } else {
         throw new PropertyNotFoundException("field [" + name + "] not found on " + root.getName());
      }
   }

   public Getter getGetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      return new DirectGetter(getField(theClass, propertyName), theClass, propertyName);
   }

   public Setter getSetter(Class theClass, String propertyName) throws PropertyNotFoundException {
      return new DirectSetter(getField(theClass, propertyName), theClass, propertyName);
   }

   public static final class DirectGetter implements Getter {
      private final transient Field field;
      private final Class clazz;
      private final String name;

      DirectGetter(Field field, Class clazz, String name) {
         this.field = field;
         this.clazz = clazz;
         this.name = name;
      }

      public Object get(Object target) throws HibernateException {
         try {
            return this.field.get(target);
         } catch (Exception e) {
            throw new PropertyAccessException(e, "could not get a field value by reflection", false, this.clazz, this.name);
         }
      }

      public Object getForInsert(Object target, Map mergeMap, SessionImplementor session) {
         return this.get(target);
      }

      public Member getMember() {
         return this.field;
      }

      public Method getMethod() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public Class getReturnType() {
         return this.field.getType();
      }

      Object readResolve() {
         return new DirectGetter(DirectPropertyAccessor.getField(this.clazz, this.name), this.clazz, this.name);
      }

      public String toString() {
         return "DirectGetter(" + this.clazz.getName() + '.' + this.name + ')';
      }
   }

   public static final class DirectSetter implements Setter {
      private final transient Field field;
      private final Class clazz;
      private final String name;

      DirectSetter(Field field, Class clazz, String name) {
         this.field = field;
         this.clazz = clazz;
         this.name = name;
      }

      public Method getMethod() {
         return null;
      }

      public String getMethodName() {
         return null;
      }

      public void set(Object target, Object value, SessionFactoryImplementor factory) throws HibernateException {
         try {
            this.field.set(target, value);
         } catch (Exception e) {
            if (value == null && this.field.getType().isPrimitive()) {
               throw new PropertyAccessException(e, "Null value was assigned to a property of primitive type", true, this.clazz, this.name);
            } else {
               throw new PropertyAccessException(e, "could not set a field value by reflection", true, this.clazz, this.name);
            }
         }
      }

      public String toString() {
         return "DirectSetter(" + this.clazz.getName() + '.' + this.name + ')';
      }

      Object readResolve() {
         return new DirectSetter(DirectPropertyAccessor.getField(this.clazz, this.name), this.clazz, this.name);
      }
   }
}
