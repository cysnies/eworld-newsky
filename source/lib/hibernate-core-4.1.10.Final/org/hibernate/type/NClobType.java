package org.hibernate.type;

import java.sql.NClob;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.sql.NClobTypeDescriptor;

public class NClobType extends AbstractSingleColumnStandardBasicType {
   public static final NClobType INSTANCE = new NClobType();

   public NClobType() {
      super(NClobTypeDescriptor.DEFAULT, org.hibernate.type.descriptor.java.NClobTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "nclob";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   protected NClob getReplacement(NClob original, NClob target, SessionImplementor session) {
      return session.getFactory().getDialect().getLobMergeStrategy().mergeNClob(original, target, session);
   }
}
