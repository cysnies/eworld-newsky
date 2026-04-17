package org.hibernate.type;

import org.hibernate.type.descriptor.java.PrimitiveByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;

public class MaterializedBlobType extends AbstractSingleColumnStandardBasicType {
   public static final MaterializedBlobType INSTANCE = new MaterializedBlobType();

   public MaterializedBlobType() {
      super(BlobTypeDescriptor.DEFAULT, PrimitiveByteArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "materialized_blob";
   }
}
