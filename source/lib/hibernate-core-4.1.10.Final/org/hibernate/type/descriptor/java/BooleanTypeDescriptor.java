package org.hibernate.type.descriptor.java;

import org.hibernate.type.descriptor.WrapperOptions;

public class BooleanTypeDescriptor extends AbstractTypeDescriptor {
   public static final BooleanTypeDescriptor INSTANCE = new BooleanTypeDescriptor();
   private final char characterValueTrue;
   private final char characterValueFalse;
   private final char characterValueTrueLC;
   private final String stringValueTrue;
   private final String stringValueFalse;

   public BooleanTypeDescriptor() {
      this('Y', 'N');
   }

   public BooleanTypeDescriptor(char characterValueTrue, char characterValueFalse) {
      super(Boolean.class);
      this.characterValueTrue = Character.toUpperCase(characterValueTrue);
      this.characterValueFalse = Character.toUpperCase(characterValueFalse);
      this.characterValueTrueLC = Character.toLowerCase(characterValueTrue);
      this.stringValueTrue = String.valueOf(characterValueTrue);
      this.stringValueFalse = String.valueOf(characterValueFalse);
   }

   public String toString(Boolean value) {
      return value == null ? null : value.toString();
   }

   public Boolean fromString(String string) {
      return Boolean.valueOf(string);
   }

   public Object unwrap(Boolean value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Boolean.class.isAssignableFrom(type)) {
         return value;
      } else if (Byte.class.isAssignableFrom(type)) {
         return this.toByte(value);
      } else if (Short.class.isAssignableFrom(type)) {
         return this.toShort(value);
      } else if (Integer.class.isAssignableFrom(type)) {
         return this.toInteger(value);
      } else if (Long.class.isAssignableFrom(type)) {
         return this.toInteger(value);
      } else if (Character.class.isAssignableFrom(type)) {
         return value ? this.characterValueTrue : this.characterValueFalse;
      } else if (String.class.isAssignableFrom(type)) {
         return value ? this.stringValueTrue : this.stringValueFalse;
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Boolean wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Boolean.class.isInstance(value)) {
         return (Boolean)value;
      } else if (Number.class.isInstance(value)) {
         int intValue = ((Number)value).intValue();
         return intValue == 0 ? Boolean.FALSE : Boolean.TRUE;
      } else if (Character.class.isInstance(value)) {
         return this.isTrue((Character)value) ? Boolean.TRUE : Boolean.FALSE;
      } else if (String.class.isInstance(value)) {
         return this.isTrue(((String)value).charAt(0)) ? Boolean.TRUE : Boolean.FALSE;
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   private boolean isTrue(char charValue) {
      return charValue == this.characterValueTrue || charValue == this.characterValueTrueLC;
   }

   public int toInt(Boolean value) {
      return value ? 1 : 0;
   }

   public Byte toByte(Boolean value) {
      return (byte)this.toInt(value);
   }

   public Short toShort(Boolean value) {
      return (short)this.toInt(value);
   }

   public Integer toInteger(Boolean value) {
      return this.toInt(value);
   }

   public Long toLong(Boolean value) {
      return (long)this.toInt(value);
   }
}
