package org.hibernate.type;

import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;

public class MaterializedClobType extends AbstractSingleColumnStandardBasicType {
   public static final MaterializedClobType INSTANCE = new MaterializedClobType();

   public MaterializedClobType() {
      super(ClobTypeDescriptor.DEFAULT, StringTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "materialized_clob";
   }
}
