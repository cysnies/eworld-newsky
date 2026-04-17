package org.hibernate.type;

import org.hibernate.type.descriptor.java.CharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class CharacterArrayType extends AbstractSingleColumnStandardBasicType {
   public static final CharacterArrayType INSTANCE = new CharacterArrayType();

   public CharacterArrayType() {
      super(VarcharTypeDescriptor.INSTANCE, CharacterArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "wrapper-characters";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Character[].class.getName(), "Character[]"};
   }
}
