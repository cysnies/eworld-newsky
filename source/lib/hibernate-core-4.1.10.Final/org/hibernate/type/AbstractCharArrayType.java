package org.hibernate.type;

import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Reader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;

/** @deprecated */
public abstract class AbstractCharArrayType extends MutableType {
   protected abstract Object toExternalFormat(char[] var1);

   protected abstract char[] toInternalFormat(Object var1);

   public Object get(ResultSet rs, String name) throws SQLException {
      Reader stream = rs.getCharacterStream(name);
      if (stream == null) {
         return this.toExternalFormat((char[])null);
      } else {
         CharArrayWriter writer = new CharArrayWriter();

         while(true) {
            try {
               int c = stream.read();
               if (c == -1) {
                  return this.toExternalFormat(writer.toCharArray());
               }

               writer.write(c);
            } catch (IOException var6) {
               throw new HibernateException("Unable to read character stream from rs");
            }
         }
      }
   }

   public abstract Class getReturnedClass();

   public void set(PreparedStatement st, Object value, int index) throws SQLException {
      char[] chars = this.toInternalFormat(value);
      st.setCharacterStream(index, new CharArrayReader(chars), chars.length);
   }

   public int sqlType() {
      return 12;
   }

   public String objectToSQLString(Object value, Dialect dialect) throws Exception {
      return '\'' + new String(this.toInternalFormat(value)) + '\'';
   }

   public Object stringToObject(String xml) throws Exception {
      if (xml == null) {
         return this.toExternalFormat((char[])null);
      } else {
         int length = xml.length();
         char[] chars = new char[length];

         for(int index = 0; index < length; ++index) {
            chars[index] = xml.charAt(index);
         }

         return this.toExternalFormat(chars);
      }
   }

   public String toString(Object value) {
      return value == null ? null : new String(this.toInternalFormat(value));
   }

   public Object fromStringValue(String xml) {
      if (xml == null) {
         return null;
      } else {
         int length = xml.length();
         char[] chars = new char[length];

         for(int index = 0; index < length; ++index) {
            chars[index] = xml.charAt(index);
         }

         return this.toExternalFormat(chars);
      }
   }

   protected Object deepCopyNotNull(Object value) throws HibernateException {
      char[] chars = this.toInternalFormat(value);
      char[] result = new char[chars.length];
      System.arraycopy(chars, 0, result, 0, chars.length);
      return this.toExternalFormat(result);
   }
}
