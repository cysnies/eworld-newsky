package org.hibernate.type.descriptor.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public class TimestampTypeDescriptor implements SqlTypeDescriptor {
   public static final TimestampTypeDescriptor INSTANCE = new TimestampTypeDescriptor();

   public int getSqlType() {
      return 93;
   }

   public boolean canBeRemapped() {
      return true;
   }

   public ValueBinder getBinder(final JavaTypeDescriptor javaTypeDescriptor) {
      return new BasicBinder(javaTypeDescriptor, this) {
         protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
            st.setTimestamp(index, (Timestamp)javaTypeDescriptor.unwrap(value, Timestamp.class, options));
         }
      };
   }

   public ValueExtractor getExtractor(final JavaTypeDescriptor javaTypeDescriptor) {
      return new BasicExtractor(javaTypeDescriptor, this) {
         protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
            return javaTypeDescriptor.wrap(rs.getTimestamp(name), options);
         }
      };
   }
}
