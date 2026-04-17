package org.hibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.ByteTypeDescriptor;
import org.hibernate.type.descriptor.sql.TinyIntTypeDescriptor;

public class ByteType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType, VersionType {
   public static final ByteType INSTANCE = new ByteType();
   private static final Byte ZERO = 0;

   public ByteType() {
      super(TinyIntTypeDescriptor.INSTANCE, ByteTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "byte";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Byte.TYPE.getName(), Byte.class.getName()};
   }

   public Serializable getDefaultValue() {
      return ZERO;
   }

   public Class getPrimitiveClass() {
      return Byte.TYPE;
   }

   public String objectToSQLString(Byte value, Dialect dialect) {
      return this.toString(value);
   }

   public Byte stringToObject(String xml) {
      return (Byte)this.fromString(xml);
   }

   public Byte fromStringValue(String xml) {
      return (Byte)this.fromString(xml);
   }

   public Byte next(Byte current, SessionImplementor session) {
      return (byte)(current + 1);
   }

   public Byte seed(SessionImplementor session) {
      return ZERO;
   }

   public Comparator getComparator() {
      return this.getJavaTypeDescriptor().getComparator();
   }
}
