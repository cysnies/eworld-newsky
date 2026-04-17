package org.hibernate.type;

import org.hibernate.type.descriptor.java.PrimitiveCharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class CharArrayType extends AbstractSingleColumnStandardBasicType {
   public static final CharArrayType INSTANCE = new CharArrayType();

   public CharArrayType() {
      super(VarcharTypeDescriptor.INSTANCE, PrimitiveCharacterArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "characters";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), "char[]", char[].class.getName()};
   }
}
