package org.hibernate.type;

import java.util.Comparator;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarbinaryTypeDescriptor;

public class BinaryType extends AbstractSingleColumnStandardBasicType implements VersionType {
   public static final BinaryType INSTANCE = new BinaryType();

   public String getName() {
      return "binary";
   }

   public BinaryType() {
      super(VarbinaryTypeDescriptor.INSTANCE, PrimitiveByteArrayTypeDescriptor.INSTANCE);
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), "byte[]", byte[].class.getName()};
   }

   public byte[] seed(SessionImplementor session) {
      return null;
   }

   public byte[] next(byte[] current, SessionImplementor session) {
      return current;
   }

   public Comparator getComparator() {
      return PrimitiveByteArrayTypeDescriptor.INSTANCE.getComparator();
   }
}
