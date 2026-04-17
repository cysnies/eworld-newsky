package org.hibernate.type.descriptor.sql;

import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public abstract class ClobTypeDescriptor implements SqlTypeDescriptor {
   public static final ClobTypeDescriptor DEFAULT = new ClobTypeDescriptor() {
      public BasicBinder getClobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               if (options.useStreamForLobBinding()) {
                  ClobTypeDescriptor.STREAM_BINDING.getClobBinder(javaTypeDescriptor).doBind(st, value, index, options);
               } else {
                  ClobTypeDescriptor.CLOB_BINDING.getClobBinder(javaTypeDescriptor).doBind(st, value, index, options);
               }

            }
         };
      }
   };
   public static final ClobTypeDescriptor CLOB_BINDING = new ClobTypeDescriptor() {
      public BasicBinder getClobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               st.setClob(index, (Clob)javaTypeDescriptor.unwrap(value, Clob.class, options));
            }
         };
      }
   };
   public static final ClobTypeDescriptor STREAM_BINDING = new ClobTypeDescriptor() {
      public BasicBinder getClobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               CharacterStream characterStream = (CharacterStream)javaTypeDescriptor.unwrap(value, CharacterStream.class, options);
               st.setCharacterStream(index, characterStream.asReader(), characterStream.getLength());
            }
         };
      }
   };
   public static final ClobTypeDescriptor STREAM_BINDING_EXTRACTING = new ClobTypeDescriptor() {
      public BasicBinder getClobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               CharacterStream characterStream = (CharacterStream)javaTypeDescriptor.unwrap(value, CharacterStream.class, options);
               st.setCharacterStream(index, characterStream.asReader(), characterStream.getLength());
            }
         };
      }

      public ValueExtractor getExtractor(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicExtractor(javaTypeDescriptor, this) {
            protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
               return javaTypeDescriptor.wrap(rs.getCharacterStream(name), options);
            }
         };
      }
   };

   public int getSqlType() {
      return 2005;
   }

   public boolean canBeRemapped() {
      return true;
   }

   public ValueExtractor getExtractor(final JavaTypeDescriptor javaTypeDescriptor) {
      return new BasicExtractor(javaTypeDescriptor, this) {
         protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
            return javaTypeDescriptor.wrap(rs.getClob(name), options);
         }
      };
   }

   protected abstract BasicBinder getClobBinder(JavaTypeDescriptor var1);

   public ValueBinder getBinder(JavaTypeDescriptor javaTypeDescriptor) {
      return this.getClobBinder(javaTypeDescriptor);
   }
}
