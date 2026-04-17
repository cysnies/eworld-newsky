package org.hibernate.type.descriptor.java;

import org.hibernate.HibernateException;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.descriptor.WrapperOptions;

public class ClassTypeDescriptor extends AbstractTypeDescriptor {
   public static final ClassTypeDescriptor INSTANCE = new ClassTypeDescriptor();

   public ClassTypeDescriptor() {
      super(Class.class);
   }

   public String toString(Class value) {
      return value.getName();
   }

   public Class fromString(String string) {
      if (string == null) {
         return null;
      } else {
         try {
            return ReflectHelper.classForName(string);
         } catch (ClassNotFoundException var3) {
            throw new HibernateException("Unable to locate named class " + string);
         }
      }
   }

   public Object unwrap(Class value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Class.class.isAssignableFrom(type)) {
         return value;
      } else if (String.class.isAssignableFrom(type)) {
         return this.toString(value);
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Class wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Class.class.isInstance(value)) {
         return (Class)value;
      } else if (String.class.isInstance(value)) {
         return this.fromString((String)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
