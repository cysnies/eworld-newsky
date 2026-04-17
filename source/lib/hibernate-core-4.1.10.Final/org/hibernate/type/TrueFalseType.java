package org.hibernate.type;

import java.io.Serializable;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.BooleanTypeDescriptor;
import org.hibernate.type.descriptor.sql.CharTypeDescriptor;

public class TrueFalseType extends AbstractSingleColumnStandardBasicType implements PrimitiveType, DiscriminatorType {
   public static final TrueFalseType INSTANCE = new TrueFalseType();

   public TrueFalseType() {
      super(CharTypeDescriptor.INSTANCE, new BooleanTypeDescriptor('T', 'F'));
   }

   public String getName() {
      return "true_false";
   }

   public Class getPrimitiveClass() {
      return Boolean.TYPE;
   }

   public Boolean stringToObject(String xml) throws Exception {
      return (Boolean)this.fromString(xml);
   }

   public Serializable getDefaultValue() {
      return Boolean.FALSE;
   }

   public String objectToSQLString(Boolean value, Dialect dialect) throws Exception {
      return StringType.INSTANCE.objectToSQLString(value ? "T" : "F", dialect);
   }
}
