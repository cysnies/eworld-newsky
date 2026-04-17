package org.hibernate.dialect;

import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;
import org.hibernate.type.descriptor.sql.SqlTypeDescriptor;

public class SybaseDialect extends AbstractTransactSQLDialect {
   private static final int PARAM_LIST_SIZE_LIMIT = 250000;

   public int getInExpressionCountLimit() {
      return 250000;
   }

   protected SqlTypeDescriptor getSqlTypeDescriptorOverride(int sqlCode) {
      switch (sqlCode) {
         case 2004:
            return BlobTypeDescriptor.PRIMITIVE_ARRAY_BINDING;
         case 2005:
            return ClobTypeDescriptor.STREAM_BINDING_EXTRACTING;
         default:
            return super.getSqlTypeDescriptorOverride(sqlCode);
      }
   }

   public String getNullColumnString() {
      return " null";
   }
}
