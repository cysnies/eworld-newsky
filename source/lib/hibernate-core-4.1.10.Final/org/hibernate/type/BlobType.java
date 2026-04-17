package org.hibernate.type;

import java.sql.Blob;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.sql.BlobTypeDescriptor;

public class BlobType extends AbstractSingleColumnStandardBasicType {
   public static final BlobType INSTANCE = new BlobType();

   public BlobType() {
      super(BlobTypeDescriptor.DEFAULT, org.hibernate.type.descriptor.java.BlobTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "blob";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   protected Blob getReplacement(Blob original, Blob target, SessionImplementor session) {
      return session.getFactory().getDialect().getLobMergeStrategy().mergeBlob(original, target, session);
   }
}
