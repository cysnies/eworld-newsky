package org.hibernate.engine.jdbc;

import java.sql.Clob;

public interface WrappedClob {
   Clob getWrappedClob();
}
