package org.hibernate.type.descriptor.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;

public class NVarcharTypeDescriptor implements SqlTypeDescriptor {
   public static final NVarcharTypeDescriptor INSTANCE = new NVarcharTypeDescriptor();

   public int getSqlType() {
      return -9;
   }

   public boolean canBeRemapped() {
      return true;
   }

   public ValueBinder getBinder(final JavaTypeDescriptor javaTypeDescriptor) {
      return new BasicBinder(javaTypeDescriptor, this) {
         protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
            st.setNString(index, (String)javaTypeDescriptor.unwrap(value, String.class, options));
         }
      };
   }

   public ValueExtractor getExtractor(final JavaTypeDescriptor javaTypeDescriptor) {
      return new BasicExtractor(javaTypeDescriptor, this) {
         protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
            return javaTypeDescriptor.wrap(rs.getNString(name), options);
         }
      };
   }
}
