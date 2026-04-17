package org.hibernate.type;

import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.LongVarcharTypeDescriptor;

public class TextType extends AbstractSingleColumnStandardBasicType {
   public static final TextType INSTANCE = new TextType();

   public TextType() {
      super(LongVarcharTypeDescriptor.INSTANCE, StringTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "text";
   }
}
