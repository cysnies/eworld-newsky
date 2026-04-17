package org.hibernate.type.descriptor.sql;

public class FloatTypeDescriptor extends RealTypeDescriptor {
   public static final FloatTypeDescriptor INSTANCE = new FloatTypeDescriptor();

   public int getSqlType() {
      return 6;
   }
}
