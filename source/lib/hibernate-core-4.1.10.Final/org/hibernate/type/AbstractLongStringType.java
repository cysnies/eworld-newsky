package org.hibernate.type;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;

/** @deprecated */
public abstract class AbstractLongStringType extends ImmutableType {
   public void set(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
      String str = (String)value;
      st.setCharacterStream(index, new StringReader(str), str.length());
   }

   public Object get(ResultSet rs, String name) throws HibernateException, SQLException {
      Reader charReader = rs.getCharacterStream(name);
      if (charReader == null) {
         return null;
      } else {
         StringBuilder sb = new StringBuilder();

         try {
            char[] buffer = new char[2048];

            while(true) {
               int amountRead = charReader.read(buffer, 0, buffer.length);
               if (amountRead == -1) {
                  return sb.toString();
               }

               sb.append(buffer, 0, amountRead);
            }
         } catch (IOException ioe) {
            throw new HibernateException("IOException occurred reading text", ioe);
         } finally {
            try {
               charReader.close();
            } catch (IOException e) {
               throw new HibernateException("IOException occurred closing stream", e);
            }
         }
      }
   }

   public Class getReturnedClass() {
      return String.class;
   }

   public String toString(Object val) {
      return (String)val;
   }

   public Object fromStringValue(String xml) {
      return xml;
   }
}
