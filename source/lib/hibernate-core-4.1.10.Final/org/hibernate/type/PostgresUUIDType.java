package org.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.sql.BasicBinder;
import org.hibernate.type.descriptor.sql.BasicExtractor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class PostgresUUIDType extends AbstractSingleColumnStandardBasicType {
   public static final PostgresUUIDType INSTANCE = new PostgresUUIDType();

   public PostgresUUIDType() {
      super(PostgresUUIDType.PostgresUUIDSqlTypeDescriptor.INSTANCE, UUIDTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "pg-uuid";
   }

   public static class PostgresUUIDSqlTypeDescriptor implements SqlTypeDescriptor {
      public static final PostgresUUIDSqlTypeDescriptor INSTANCE = new PostgresUUIDSqlTypeDescriptor();

      public int getSqlType() {
         return 1111;
      }

      public boolean canBeRemapped() {
         return true;
      }

      public ValueBinder getBinder(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicBinder(javaTypeDescriptor, this) {
            protected void doBind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
               st.setObject(index, javaTypeDescriptor.unwrap(value, UUID.class, options), PostgresUUIDSqlTypeDescriptor.this.getSqlType());
            }
         };
      }

      public ValueExtractor getExtractor(final JavaTypeDescriptor javaTypeDescriptor) {
         return new BasicExtractor(javaTypeDescriptor, this) {
            protected Object doExtract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
               return javaTypeDescriptor.wrap(rs.getObject(name), options);
            }
         };
      }
   }
}
