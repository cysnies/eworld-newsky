package org.hibernate.type;

import org.hibernate.type.descriptor.java.ByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarbinaryTypeDescriptor;

public class WrapperBinaryType extends AbstractSingleColumnStandardBasicType {
   public static final WrapperBinaryType INSTANCE = new WrapperBinaryType();

   public WrapperBinaryType() {
      super(VarbinaryTypeDescriptor.INSTANCE, ByteArrayTypeDescriptor.INSTANCE);
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), "Byte[]", Byte[].class.getName()};
   }

   public String getName() {
      return "wrapper-binary";
   }
}
