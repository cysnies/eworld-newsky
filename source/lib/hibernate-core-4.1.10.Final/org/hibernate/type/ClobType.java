package org.hibernate.type;

import java.sql.Clob;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.type.descriptor.sql.ClobTypeDescriptor;

public class ClobType extends AbstractSingleColumnStandardBasicType {
   public static final ClobType INSTANCE = new ClobType();

   public ClobType() {
      super(ClobTypeDescriptor.DEFAULT, org.hibernate.type.descriptor.java.ClobTypeDescriptor.INSTANCE);
   }

   public String getName() {
      return "clob";
   }

   protected boolean registerUnderJavaType() {
      return true;
   }

   protected Clob getReplacement(Clob original, Clob target, SessionImplementor session) {
      return session.getFactory().getDialect().getLobMergeStrategy().mergeClob(original, target, session);
   }
}
