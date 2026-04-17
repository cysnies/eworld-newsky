package org.hibernate.type.descriptor.sql;

public class BinaryTypeDescriptor extends VarbinaryTypeDescriptor {
   public static final BinaryTypeDescriptor INSTANCE = new BinaryTypeDescriptor();

   public int getSqlType() {
      return -2;
   }
}
