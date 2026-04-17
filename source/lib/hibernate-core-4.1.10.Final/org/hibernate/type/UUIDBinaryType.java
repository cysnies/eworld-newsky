package org.hibernate.type;

import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.sql.BinaryTypeDescriptor;

public class UUIDBinaryType extends AbstractSingleColumnStandardBasicType {
   public static final UUIDBinaryType INSTANCE = new UUIDBinaryType();

   public UUIDBinaryType() {
      super(BinaryTypeDescriptor.INSTANCE, UUIDTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "uuid-binary";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }
}
