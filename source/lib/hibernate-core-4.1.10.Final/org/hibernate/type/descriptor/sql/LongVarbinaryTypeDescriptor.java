package org.hibernate.type.descriptor.sql;

public class LongVarbinaryTypeDescriptor extends VarbinaryTypeDescriptor {
   public static final LongVarbinaryTypeDescriptor INSTANCE = new LongVarbinaryTypeDescriptor();

   public int getSqlType() {
      return -4;
   }
}
