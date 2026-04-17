package org.hibernate.type.descriptor.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.descriptor.ValueExtractor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.jboss.logging.Logger;

public abstract class BasicExtractor implements ValueExtractor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BasicExtractor.class.getName());
   private final JavaTypeDescriptor javaDescriptor;
   private final SqlTypeDescriptor sqlDescriptor;

   public BasicExtractor(JavaTypeDescriptor javaDescriptor, SqlTypeDescriptor sqlDescriptor) {
      this.javaDescriptor = javaDescriptor;
      this.sqlDescriptor = sqlDescriptor;
   }

   public JavaTypeDescriptor getJavaDescriptor() {
      return this.javaDescriptor;
   }

   public SqlTypeDescriptor getSqlDescriptor() {
      return this.sqlDescriptor;
   }

   public Object extract(ResultSet rs, String name, WrapperOptions options) throws SQLException {
      J value = (J)this.doExtract(rs, name, options);
      if (value != null && !rs.wasNull()) {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Found [{0}] as column [{1}]", this.getJavaDescriptor().extractLoggableRepresentation(value), name);
         }

         return value;
      } else {
         LOG.tracev("Found [null] as column [{0}]", name);
         return null;
      }
   }

   protected abstract Object doExtract(ResultSet var1, String var2, WrapperOptions var3) throws SQLException;
}
