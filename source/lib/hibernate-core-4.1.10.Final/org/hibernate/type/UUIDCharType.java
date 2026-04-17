package org.hibernate.type;

import java.util.UUID;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.hibernate.type.descriptor.sql.VarcharTypeDescriptor;

public class UUIDCharType extends AbstractSingleColumnStandardBasicType implements LiteralType {
   public static final UUIDCharType INSTANCE = new UUIDCharType();

   public UUIDCharType() {
      super(VarcharTypeDescriptor.INSTANCE, UUIDTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "uuid-char";
   }

   public String objectToSQLString(UUID value, Dialect dialect) throws Exception {
      return StringType.INSTANCE.objectToSQLString(value.toString(), dialect);
   }
}
