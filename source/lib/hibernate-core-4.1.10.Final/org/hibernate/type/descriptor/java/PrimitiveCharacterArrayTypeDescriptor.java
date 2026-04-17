package org.hibernate.type.descriptor.java;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.util.Arrays;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class PrimitiveCharacterArrayTypeDescriptor extends AbstractTypeDescriptor {
   public static final PrimitiveCharacterArrayTypeDescriptor INSTANCE = new PrimitiveCharacterArrayTypeDescriptor();

   protected PrimitiveCharacterArrayTypeDescriptor() {
      super(char[].class, ArrayMutabilityPlan.INSTANCE);
   }

   public String toString(char[] value) {
      return new String(value);
   }

   public char[] fromString(String string) {
      return string.toCharArray();
   }

   public boolean areEqual(char[] one, char[] another) {
      return one == another || one != null && another != null && Arrays.equals(one, another);
   }

   public int extractHashCode(char[] chars) {
      int hashCode = 1;

      for(char aChar : chars) {
         hashCode = 31 * hashCode + aChar;
      }

      return hashCode;
   }

   public Object unwrap(char[] value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (char[].class.isAssignableFrom(type)) {
         return value;
      } else if (String.class.isAssignableFrom(type)) {
         return new String(value);
      } else if (Clob.class.isAssignableFrom(type)) {
         return options.getLobCreator().createClob(new String(value));
      } else if (Reader.class.isAssignableFrom(type)) {
         return new StringReader(new String(value));
      } else if (CharacterStream.class.isAssignableFrom(type)) {
         return new CharacterStreamImpl(new String(value));
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public char[] wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (char[].class.isInstance(value)) {
         return (char[])value;
      } else if (String.class.isInstance(value)) {
         return ((String)value).toCharArray();
      } else if (Clob.class.isInstance(value)) {
         return DataHelper.extractString((Clob)value).toCharArray();
      } else if (Reader.class.isInstance(value)) {
         return DataHelper.extractString((Reader)value).toCharArray();
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
