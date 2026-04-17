package org.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.JavaTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public abstract class AbstractSingleColumnStandardBasicType extends AbstractStandardBasicType implements SingleColumnType {
   public AbstractSingleColumnStandardBasicType(SqlTypeDescriptor sqlTypeDescriptor, JavaTypeDescriptor javaTypeDescriptor) {
      super(sqlTypeDescriptor, javaTypeDescriptor);
   }

   public final int sqlType() {
      return this.getSqlTypeDescriptor().getSqlType();
   }

   public final void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      if (settable[0]) {
         this.nullSafeSet(st, value, index, session);
      }

   }
}
