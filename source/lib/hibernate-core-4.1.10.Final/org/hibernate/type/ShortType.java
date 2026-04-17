package org.hibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.ShortTypeDescriptor;
import org.hibernate.type.descriptor.sql.SmallIntTypeDescriptor;

public class ShortType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType, VersionType {
   public static final ShortType INSTANCE = new ShortType();
   private static final Short ZERO = Short.valueOf((short)0);

   public ShortType() {
      super(SmallIntTypeDescriptor.INSTANCE, ShortTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "short";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Short.TYPE.getName(), Short.class.getName()};
   }

   public Serializable getDefaultValue() {
      return ZERO;
   }

   public Class getPrimitiveClass() {
      return Short.TYPE;
   }

   public String objectToSQLString(Short value, Dialect dialect) throws Exception {
      return value.toString();
   }

   public Short stringToObject(String xml) throws Exception {
      return Short.valueOf(xml);
   }

   public Short next(Short current, SessionImplementor session) {
      return (short)(current + 1);
   }

   public Short seed(SessionImplementor session) {
      return ZERO;
   }

   public Comparator getComparator() {
      return this.getJavaTypeDescriptor().getComparator();
   }
}
