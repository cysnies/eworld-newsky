package org.hibernate.type;

import org.hibernate.type.descriptor.java.CharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;

public class CharacterArrayClobType extends AbstractSingleColumnStandardBasicType {
   public static final CharacterArrayClobType INSTANCE = new CharacterArrayClobType();

   public CharacterArrayClobType() {
      super(ClobTypeDescriptor.DEFAULT, CharacterArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return null;
   }
}
