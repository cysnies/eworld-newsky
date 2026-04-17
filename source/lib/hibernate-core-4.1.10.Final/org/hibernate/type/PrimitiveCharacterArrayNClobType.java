package org.hibernate.type;

import org.hibernate.type.descriptor.java.PrimitiveCharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.NClobTypeDescriptor;

public class PrimitiveCharacterArrayNClobType extends AbstractSingleColumnStandardBasicType {
   public static final CharacterArrayClobType INSTANCE = new CharacterArrayClobType();

   public PrimitiveCharacterArrayNClobType() {
      super(NClobTypeDescriptor.DEFAULT, PrimitiveCharacterArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return null;
   }
}
