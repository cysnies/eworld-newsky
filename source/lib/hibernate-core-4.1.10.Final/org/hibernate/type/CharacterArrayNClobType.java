package org.hibernate.type;

import org.hibernate.type.descriptor.java.CharacterArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.NClobTypeDescriptor;

public class CharacterArrayNClobType extends AbstractSingleColumnStandardBasicType {
   public static final CharacterArrayNClobType INSTANCE = new CharacterArrayNClobType();

   public CharacterArrayNClobType() {
      super(NClobTypeDescriptor.DEFAULT, CharacterArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return null;
   }
}
