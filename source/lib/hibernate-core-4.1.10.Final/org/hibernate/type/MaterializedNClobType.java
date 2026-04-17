package org.hibernate.type;

import org.hibernate.type.descriptor.java.StringTypeDescriptor;
import org.hibernate.type.descriptor.sql.NClobTypeDescriptor;

public class MaterializedNClobType extends AbstractSingleColumnStandardBasicType {
   public static final MaterializedNClobType INSTANCE = new MaterializedNClobType();

   public MaterializedNClobType() {
      super(NClobTypeDescriptor.DEFAULT, StringTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "materialized_nclob";
   }
}
