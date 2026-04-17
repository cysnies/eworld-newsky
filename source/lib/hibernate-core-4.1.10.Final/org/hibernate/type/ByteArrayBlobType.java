package org.hibernate.type;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.dom4j.Node;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;

/** @deprecated */
@Deprecated
public class ByteArrayBlobType extends AbstractLobType {
   private static final int[] TYPES = new int[]{2004};

   public int[] sqlTypes(Mapping mapping) {
      return TYPES;
   }

   public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) {
      if (x == y) {
         return true;
      } else if (x != null && y != null) {
         if (x instanceof Byte[]) {
            Object[] o1 = x;
            Object[] o2 = y;
            return ArrayHelper.isEquals(o1, o2);
         } else {
            byte[] c1 = (byte[])x;
            byte[] c2 = (byte[])y;
            return ArrayHelper.isEquals(c1, c2);
         }
      } else {
         return false;
      }
   }

   public int getHashCode(Object x, SessionFactoryImplementor factory) {
      if (x instanceof Character[]) {
         Object[] o = x;
         return ArrayHelper.hash(o);
      } else {
         byte[] c = (byte[])x;
         return ArrayHelper.hash(c);
      }
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (value == null) {
         return null;
      } else if (!(value instanceof Byte[])) {
         byte[] array = (byte[])value;
         int length = array.length;
         byte[] copy = new byte[length];
         System.arraycopy(array, 0, copy, 0, length);
         return copy;
      } else {
         Byte[] array = (Byte[])value;
         int length = array.length;
         Byte[] copy = new Byte[length];

         for(int index = 0; index < length; ++index) {
            copy[index] = array[index];
         }

         return copy;
      }
   }

   public Class getReturnedClass() {
      return Byte[].class;
   }

   protected Object get(ResultSet rs, String name) throws SQLException {
      Blob blob = rs.getBlob(name);
      if (rs.wasNull()) {
         return null;
      } else {
         int length = (int)blob.length();
         byte[] primaryResult = blob.getBytes(1L, length);
         return this.wrap(primaryResult);
      }
   }

   protected void set(PreparedStatement st, Object value, int index, SessionImplementor session) throws SQLException {
      if (value == null) {
         st.setNull(index, this.sqlTypes((Mapping)null)[0]);
      } else {
         byte[] toSet = this.unWrap(value);
         boolean useInputStream = session.getFactory().getDialect().useInputStreamToInsertBlob();
         if (useInputStream) {
            st.setBinaryStream(index, new ByteArrayInputStream(toSet), toSet.length);
         } else {
            st.setBlob(index, Hibernate.getLobCreator(session).createBlob(toSet));
         }
      }

   }

   public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
      node.setText(this.toString(value));
   }

   public String toString(Object val) {
      byte[] bytes = this.unWrap(val);
      StringBuilder buf = new StringBuilder(2 * bytes.length);

      for(int i = 0; i < bytes.length; ++i) {
         String hexStr = Integer.toHexString(bytes[i] - -128);
         if (hexStr.length() == 1) {
            buf.append('0');
         }

         buf.append(hexStr);
      }

      return buf.toString();
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) {
      return value == null ? "null" : this.toString(value);
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      String xmlText = xml.getText();
      return xmlText != null && xmlText.length() != 0 ? this.fromString(xmlText) : null;
   }

   private Object fromString(String xmlText) {
      if (xmlText == null) {
         return null;
      } else if (xmlText.length() % 2 != 0) {
         throw new IllegalArgumentException("The string is not a valid xml representation of a binary content.");
      } else {
         byte[] bytes = new byte[xmlText.length() / 2];

         for(int i = 0; i < bytes.length; ++i) {
            String hexStr = xmlText.substring(i * 2, (i + 1) * 2);
            bytes[i] = (byte)(Integer.parseInt(hexStr, 16) + -128);
         }

         return this.wrap(bytes);
      }
   }

   protected Object wrap(byte[] bytes) {
      return this.wrapPrimitive(bytes);
   }

   protected byte[] unWrap(Object bytes) {
      return this.unwrapNonPrimitive((Byte[])bytes);
   }

   private byte[] unwrapNonPrimitive(Byte[] bytes) {
      int length = bytes.length;
      byte[] result = new byte[length];

      for(int i = 0; i < length; ++i) {
         result[i] = bytes[i];
      }

      return result;
   }

   private Byte[] wrapPrimitive(byte[] bytes) {
      int length = bytes.length;
      Byte[] result = new Byte[length];

      for(int index = 0; index < length; ++index) {
         result[index] = bytes[index];
      }

      return result;
   }

   public boolean isMutable() {
      return true;
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      return this.isEqual(original, target) ? original : this.deepCopy(original, session.getFactory());
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      return value == null ? ArrayHelper.FALSE : ArrayHelper.TRUE;
   }
}
