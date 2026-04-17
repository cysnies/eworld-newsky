package org.hibernate.type.descriptor.java;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.type.descriptor.WrapperOptions;

public class StringTypeDescriptor extends AbstractTypeDescriptor {
   public static final StringTypeDescriptor INSTANCE = new StringTypeDescriptor();

   public StringTypeDescriptor() {
      super(String.class);
   }

   public String toString(String value) {
      return value;
   }

   public String fromString(String string) {
      return string;
   }

   public Object unwrap(String value, Class type, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isAssignableFrom(type)) {
         return value;
      } else if (Reader.class.isAssignableFrom(type)) {
         return new StringReader(value);
      } else if (CharacterStream.class.isAssignableFrom(type)) {
         return new CharacterStreamImpl(value);
      } else if (Clob.class.isAssignableFrom(type)) {
         return options.getLobCreator().createClob(value);
      } else if (DataHelper.isNClob(type)) {
         return options.getLobCreator().createNClob(value);
      } else {
         throw this.unknownUnwrap(type);
      }
   }

   public String wrap(Object value, WrapperOptions options) {
      if (value == null) {
         return null;
      } else if (String.class.isInstance(value)) {
         return (String)value;
      } else if (Reader.class.isInstance(value)) {
         return DataHelper.extractString((Reader)value);
      } else if (Clob.class.isInstance(value)) {
         return DataHelper.extractString((Clob)value);
      } else {
         throw this.unknownWrap(value.getClass());
      }
   }
}
