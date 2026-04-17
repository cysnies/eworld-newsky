package org.hibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.LongTypeDescriptor;
import org.hibernate.type.descriptor.sql.BigIntTypeDescriptor;

public class LongType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType, VersionType {
   public static final LongType INSTANCE = new LongType();
   private static final Long ZERO = 0L;

   public LongType() {
      super(BigIntTypeDescriptor.INSTANCE, LongTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "long";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Long.TYPE.getName(), Long.class.getName()};
   }

   public Serializable getDefaultValue() {
      return ZERO;
   }

   public Class getPrimitiveClass() {
      return Long.TYPE;
   }

   public Long stringToObject(String xml) throws Exception {
      return Long.valueOf(xml);
   }

   public Long next(Long current, SessionImplementor session) {
      return current + 1L;
   }

   public Long seed(SessionImplementor session) {
      return ZERO;
   }

   public Comparator getComparator() {
      return this.getJavaTypeDescriptor().getComparator();
   }

   public String objectToSQLString(Long value, Dialect dialect) throws Exception {
      return value.toString();
   }
}
