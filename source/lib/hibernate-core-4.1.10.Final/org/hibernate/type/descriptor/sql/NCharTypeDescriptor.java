package org.hibernate.type.descriptor.sql;

public class NCharTypeDescriptor extends NVarcharTypeDescriptor {
   public static final NCharTypeDescriptor INSTANCE = new NCharTypeDescriptor();

   public int getSqlType() {
      return -15;
   }
}
