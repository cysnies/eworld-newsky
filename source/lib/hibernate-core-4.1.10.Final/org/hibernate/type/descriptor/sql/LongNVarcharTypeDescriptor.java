package org.hibernate.type.descriptor.sql;

public class LongNVarcharTypeDescriptor extends NVarcharTypeDescriptor {
   public static final LongVarcharTypeDescriptor INSTANCE = new LongVarcharTypeDescriptor();

   public int getSqlType() {
      return -16;
   }
}
