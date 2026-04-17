package org.hibernate.engine.jdbc;

import java.io.Reader;

public interface CharacterStream {
   Reader asReader();

   String asString();

   long getLength();

   void release();
}
