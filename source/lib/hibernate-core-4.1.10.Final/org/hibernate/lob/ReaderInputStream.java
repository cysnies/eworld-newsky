package org.hibernate.lob;

import java.io.IOException;
import java.io.Reader;

/** @deprecated */
public class ReaderInputStream extends org.hibernate.engine.jdbc.ReaderInputStream {
   public ReaderInputStream(Reader reader) {
      super(reader);
   }

   public int read() throws IOException {
      return super.read();
   }
}
