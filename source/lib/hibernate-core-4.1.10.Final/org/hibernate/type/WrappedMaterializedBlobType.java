package org.hibernate.type;

import org.hibernate.type.descriptor.java.ByteArrayTypeDescriptor;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;

public class WrappedMaterializedBlobType extends AbstractSingleColumnStandardBasicType {
   public static final WrappedMaterializedBlobType INSTANCE = new WrappedMaterializedBlobType();

   public WrappedMaterializedBlobType() {
      super(BlobTypeDescriptor.DEFAULT, ByteArrayTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return null;
   }
}
