package org.hibernate.type;

import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarbinaryTypeDescriptor;

public class ImageType extends AbstractSingleColumnStandardBasicType {
   public static final ImageType INSTANCE = new ImageType();

   public ImageType() {
      super(LongVarbinaryTypeDescriptor.INSTANCE, PrimitiveByteArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "image";
   }
}
