package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.CharacterTypeDescriptor;
import org.hibernate.type.descriptor.sql.CharTypeDescriptor;

public class CharacterType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType {
   public static final CharacterType INSTANCE = new CharacterType();

   public CharacterType() {
      super(CharTypeDescriptor.INSTANCE, CharacterTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "character";
   }

   public String[] getRegistrationKeys() {
      return new String[]{this.getName(), Character.TYPE.getName(), Character.class.getName()};
   }

   public Serializable getDefaultValue() {
      throw new UnsupportedOperationException("not a valid id type");
   }

   public Class getPrimitiveClass() {
      return Character.TYPE;
   }

   public String objectToSQLString(Character value, Dialect dialect) {
      return '\'' + this.toString(value) + '\'';
   }

   public Character stringToObject(String xml) {
      return (Character)this.fromString(xml);
   }
}
