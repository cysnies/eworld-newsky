package org.hibernate.engine.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;

public interface LobCreator {
   Blob wrap(Blob var1);

   Clob wrap(Clob var1);

   NClob wrap(NClob var1);

   Blob createBlob(byte[] var1);

   Blob createBlob(InputStream var1, long var2);

   Clob createClob(String var1);

   Clob createClob(Reader var1, long var2);

   NClob createNClob(String var1);

   NClob createNClob(Reader var1, long var2);
}
