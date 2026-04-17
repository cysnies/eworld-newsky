package org.hibernate;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.NClob;

public interface LobHelper {
   Blob createBlob(byte[] var1);

   Blob createBlob(InputStream var1, long var2);

   Clob createClob(String var1);

   Clob createClob(Reader var1, long var2);

   NClob createNClob(String var1);

   NClob createNClob(Reader var1, long var2);
}
