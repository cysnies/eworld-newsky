package org.hibernate.type;

import org.hibernate.type.descriptor.java.PrimitiveCharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;

public class PrimitiveCharacterArrayClobType extends AbstractSingleColumnStandardBasicType {
   public static final CharacterArrayClobType INSTANCE = new CharacterArrayClobType();

   public PrimitiveCharacterArrayClobType() {
      super(ClobTypeDescriptor.DEFAULT, PrimitiveCharacterArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return null;
   }
}
