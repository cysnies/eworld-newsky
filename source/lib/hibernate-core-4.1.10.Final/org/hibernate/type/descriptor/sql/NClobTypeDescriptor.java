package org.hibernate.type.descriptor.sql;

import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public abstract class NClobTypeDescriptor implements SqlTypeDescriptor {
   public static final NClobTypeDescriptor DEFAULT = new NClobTypeDescriptor() {
      public BasicBinder getNClobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               if (options.useStreamForLobBinding()) {
                  NClobTypeDescriptor.STREAM_BINDING.getNClobBinder(javaTypeDescriptor).doBind(st, value, index, options);
               } else {
                  NClobTypeDescriptor.NCLOB_BINDING.getNClobBinder(javaTypeDescriptor).doBind(st, value, index, options);
               }

            }
         };
      }
   };
   public static final NClobTypeDescriptor NCLOB_BINDING = new NClobTypeDescriptor() {
      public BasicBinder getNClobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               st.setNClob(index, (NClob)javaTypeDescriptor.unwrap(value, NClob.class, options));
            }
         };
      }
   };
   public static final NClobTypeDescriptor STREAM_BINDING = new NClobTypeDescriptor() {
      public BasicBinder getNClobBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               CharacterStream characterStream = (CharacterStream)javaTypeDescriptor.unwrap(value, CharacterStream.class, options);
               st.setCharacterStream(index, characterStream.asReader(), characterStream.getLength());
            }
         };
      }
   };

   public int getSqlType() {
      return 2011;
   }

   public boolean canBeRemapped() {
      return true;
   }

   public ValueExtractor getExtractor(final JavaTypeDescriptor javaTypeDescriptor) {
      return new BasicExtractor(javaTypeDescriptor, this) {
         protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
            return javaTypeDescriptor.wrap(rs.getNClob(name), options);
         }
      };
   }

   protected abstract BasicBinder getNClobBinder(JavaTypeDescriptor var1);

   public ValueBinder getBinder(JavaTypeDescriptor javaTypeDescriptor) {
      return this.getNClobBinder(javaTypeDescriptor);
   }
}
