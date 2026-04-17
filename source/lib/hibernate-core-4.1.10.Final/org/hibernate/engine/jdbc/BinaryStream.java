package org.hibernate.engine.jdbc;

import java.io.InputStream;

public interface BinaryStream {
   InputStream getInputStream();

   byte[] getBytes();

   long getLength();

   void release();
}
