package org.hibernate.type;

import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongNVarcharTypeDescriptor;

public class NTextType extends AbstractSingleColumnStandardBasicType {
   public static final NTextType INSTANCE = new NTextType();

   public NTextType() {
      super(LongNVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "ntext";
   }
}
