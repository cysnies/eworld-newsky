package org.hibernate.type.descriptor.java;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.util.Arrays;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class CharacterArrayTypeDescriptor extends AbstractTypeDescriptor {
   public static final CharacterArrayTypeDescriptor INSTANCE = new CharacterArrayTypeDescriptor();

   public CharacterArrayTypeDescriptor() {
      super(Character[].class, ArrayMutabilityPlan.INSTANCE);
   }

   public String toString(Character[] value) {
      return new String(this.unwrapChars(value));
   }

   public Character[] fromString(String string) {
      return this.wrapChars(string.toCharArray());
   }

   public boolean areEqual(Character[] one, Character[] another) {
      return one == another || one != null && another != null && Arrays.equals(one, another);
   }

   public int extractHashCode(Character[] chars) {
      int hashCode = 1;

      for(Character aChar : chars) {
         hashCode = 31 * hashCode + aChar;
      }

      return hashCode;
   }

   public Object unwrap(Character[] value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Character[].class.isAssignableFrom(type)) {
         return value;
      } else if (String.class.isAssignableFrom(type)) {
         return new String(this.unwrapChars(value));
      } else if (Clob.class.isAssignableFrom(type)) {
         return options.getLobCreator().createClob(new String(this.unwrapChars(value)));
      } else if (Reader.class.isAssignableFrom(type)) {
         return new StringReader(new String(this.unwrapChars(value)));
      } else if (CharacterStream.class.isAssignableFrom(type)) {
         return new CharacterStreamImpl(new String(this.unwrapChars(value)));
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public Character[] wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (Character[].class.isInstance(value)) {
         return (Character[])value;
      } else if (String.class.isInstance(value)) {
         return this.wrapChars(((String)value).toCharArray());
      } else if (Clob.class.isInstance(value)) {
         return this.wrapChars(DataHelper.extractString((Clob)value).toCharArray());
      } else if (Reader.class.isInstance(value)) {
         return this.wrapChars(DataHelper.extractString((Reader)value).toCharArray());
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }

   private Character[] wrapChars(char[] chars) {
      if (chars == null) {
         return null;
      } else {
         Character[] result = new Character[chars.length];

         for(int i = 0; i < chars.length; ++i) {
            result[i] = chars[i];
         }

         return result;
      }
   }

   private char[] unwrapChars(Character[] chars) {
      if (chars == null) {
         return null;
      } else {
         char[] result = new char[chars.length];

         for(int i = 0; i < chars.length; ++i) {
            result[i] = chars[i];
         }

         return result;
      }
   }
}
