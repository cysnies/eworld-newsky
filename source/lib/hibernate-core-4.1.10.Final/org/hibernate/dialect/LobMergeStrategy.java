package org.hibernate.dialect;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;
import org.hibernate.engine.spi.SessionImplementor;

public interface LobMergeStrategy {
   Blob mergeBlob(Blob var1, Blob var2, SessionImplementor var3);

   Clob mergeClob(Clob var1, Clob var2, SessionImplementor var3);

   NClob mergeNClob(NClob var1, NClob var2, SessionImplementor var3);
}
