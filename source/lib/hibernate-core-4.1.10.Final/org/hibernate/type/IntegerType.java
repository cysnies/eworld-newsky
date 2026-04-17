package org.hibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.sql.IntegerTypeDescriptor;

public class IntegerType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType, VersionType {
   public static final IntegerType INSTANCE = new IntegerType();
   public static final Integer ZERO = 0;

   public IntegerType() {
      super(IntegerTypeDescriptor.INSTANCE, org.hibernate.type.descriptor.java.IntegerTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "integer";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Integer.TYPE.getName(), Integer.class.getName()};
   }

   public Serializable getDefaultValue() {
      return ZERO;
   }

   public Class getPrimitiveClass() {
      return Integer.TYPE;
   }

   public String objectToSQLString(Integer value, Dialect dialect) throws Exception {
      return this.toString(value);
   }

   public Integer stringToObject(String xml) {
      return (Integer)this.fromString(xml);
   }

   public Integer seed(SessionImplementor session) {
      return ZERO;
   }

   public Integer next(Integer current, SessionImplementor session) {
      return current + 1;
   }

   public Comparator getComparator() {
      return this.getJavaTypeDescriptor().getComparator();
   }
}
