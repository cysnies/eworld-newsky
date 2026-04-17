package org.hibernate.bytecode.spi;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

public class ByteCodeHelper {
   private ByteCodeHelper() {
   }

   public static byte[] readByteCode(InputStream inputStream) throws IOException {
      if (inputStream == null) {
         throw new IOException("null input stream");
      } else {
         byte[] buffer = new byte[409600];
         byte[] classBytes = new byte[0];

         try {
            int r;
            for(r = inputStream.read(buffer); r >= buffer.length; r = inputStream.read(buffer)) {
               byte[] temp = new byte[classBytes.length + buffer.length];
               System.arraycopy(classBytes, 0, temp, 0, classBytes.length);
               System.arraycopy(buffer, 0, temp, classBytes.length, buffer.length);
               classBytes = temp;
            }

            if (r != -1) {
               byte[] temp = new byte[classBytes.length + r];
               System.arraycopy(classBytes, 0, temp, 0, classBytes.length);
               System.arraycopy(buffer, 0, temp, classBytes.length, r);
               classBytes = temp;
            }
         } finally {
            try {
               inputStream.close();
            } catch (IOException var10) {
            }

         }

         return classBytes;
      }
   }

   public static byte[] readByteCode(File file) throws IOException {
      return readByteCode((InputStream)(new FileInputStream(file)));
   }

   public static byte[] readByteCode(ZipInputStream zip) throws IOException {
      ByteArrayOutputStream bout = new ByteArrayOutputStream();
      InputStream in = new BufferedInputStream(zip);

      int b;
      while((b = in.read()) != -1) {
         bout.write(b);
      }

      return bout.toByteArray();
   }
}
