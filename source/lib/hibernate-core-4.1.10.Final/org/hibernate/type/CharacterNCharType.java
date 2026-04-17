package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.CharacterTypeDescriptor;
import org.hibernate.type.descriptor.sql.NCharTypeDescriptor;

public class CharacterNCharType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType {
   public static final CharacterNCharType INSTANCE = new CharacterNCharType();

   public CharacterNCharType() {
      super(NCharTypeDescriptor.INSTANCE, CharacterTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "ncharacter";
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
