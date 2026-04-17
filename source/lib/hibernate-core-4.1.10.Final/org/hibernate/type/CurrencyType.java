package org.hibernate.type;

import java.util.Currency;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.CurrencyTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class CurrencyType extends AbstractSingleColumnStandardBasicType implements LiteralType {
   public static final CurrencyType INSTANCE = new CurrencyType();

   public CurrencyType() {
      super(VarcharTypeDescriptor.INSTANCE, CurrencyTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "currency";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   public String objectToSQLString(Currency value, Dialect dialect) throws Exception {
      return "'" + this.toString(value) + "'";
   }
}
