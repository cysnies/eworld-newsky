package org.hibernate.type.descriptor.java;

import org.hibernate.HibernateException;
import org.hibernate.type.descriptor.WrapperOptions;

public class CharacterTypeDescriptor extends AbstractTypeDescriptor {
   public static final CharacterTypeDescriptor INSTANCE = new CharacterTypeDescriptor();

   public CharacterTypeDescriptor() {
      super(Character.class);
   }

   public String toString(Character value) {
      return value.toString();
   }

   public Character fromString(String string) {
      if (string.length() != 1) {
         throw new HibernateException("multiple or zero characters found parsing string");
      } else {
         return string.charAt(0);
      }
   }

   public Object unwrap(Character value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Character.class.isAssignableFrom(type)) {
         return value;
      } else if (String.class.isAssignableFrom(type)) {
         return value.toString();
      } else if (Number.class.isAssignableFrom(type)) {
         return (short)value;
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Character wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Character.class.isInstance(value)) {
         return (Character)value;
      } else if (String.class.isInstance(value)) {
         String str = (String)value;
         return str.charAt(0);
      } else if (Number.class.isInstance(value)) {
         Number nbr = (Number)value;
         return (char)nbr.shortValue();
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
