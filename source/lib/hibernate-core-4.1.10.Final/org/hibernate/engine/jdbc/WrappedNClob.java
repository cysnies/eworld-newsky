package org.hibernate.engine.jdbc;

import java.sql.NClob;

public interface WrappedNClob extends WrappedClob {
   /** @deprecated */
   @Deprecated
   NClob getWrappedClob();

   NClob getWrappedNClob();
}
