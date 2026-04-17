package org.hibernate.engine.jdbc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

public class StreamUtils {
   public static final int DEFAULT_CHUNK_SIZE = 1024;

   public static long copy(InputStream inputStream, OutputStream outputStream) throws IOException {
      return copy((InputStream)inputStream, (OutputStream)outputStream, 1024);
   }

   public static long copy(InputStream inputStream, OutputStream outputStream, int bufferSize) throws IOException {
      byte[] buffer = new byte[bufferSize];

      long count;
      int n;
      for(count = 0L; -1 != (n = inputStream.read(buffer)); count += (long)n) {
         outputStream.write(buffer, 0, n);
      }

      return count;
   }

   public static long copy(Reader reader, Writer writer) throws IOException {
      return copy((Reader)reader, (Writer)writer, 1024);
   }

   public static long copy(Reader reader, Writer writer, int bufferSize) throws IOException {
      char[] buffer = new char[bufferSize];

      long count;
      int n;
      for(count = 0L; -1 != (n = reader.read(buffer)); count += (long)n) {
         writer.write(buffer, 0, n);
      }

      return count;
   }
}
