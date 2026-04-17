package org.hibernate.type.descriptor.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.descriptor.JdbcTypeNameMapper;
import org.hibernate.type.descriptor.ValueBinder;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.jboss.logging.Logger;

public abstract class BasicBinder implements ValueBinder {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BasicBinder.class.getName());
   private static final String BIND_MSG_TEMPLATE = "binding parameter [%s] as [%s] - %s";
   private static final String NULL_BIND_MSG_TEMPLATE = "binding parameter [%s] as [%s] - <null>";
   private final JavaTypeDescriptor javaDescriptor;
   private final SqlTypeDescriptor sqlDescriptor;

   public JavaTypeDescriptor getJavaDescriptor() {
      return this.javaDescriptor;
   }

   public SqlTypeDescriptor getSqlDescriptor() {
      return this.sqlDescriptor;
   }

   public BasicBinder(JavaTypeDescriptor javaDescriptor, SqlTypeDescriptor sqlDescriptor) {
      this.javaDescriptor = javaDescriptor;
      this.sqlDescriptor = sqlDescriptor;
   }

   public final void bind(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
      if (value == null) {
         if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("binding parameter [%s] as [%s] - <null>", index, JdbcTypeNameMapper.getTypeName(this.sqlDescriptor.getSqlType())));
         }

         st.setNull(index, this.sqlDescriptor.getSqlType());
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.trace(String.format("binding parameter [%s] as [%s] - %s", index, JdbcTypeNameMapper.getTypeName(this.sqlDescriptor.getSqlType()), this.getJavaDescriptor().extractLoggableRepresentation(value)));
         }

         this.doBind(st, value, index, options);
      }

   }

   protected abstract void doBind(PreparedStatement var1, Object var2, int var3, WrapperOptions var4) throws SQLException;
}
