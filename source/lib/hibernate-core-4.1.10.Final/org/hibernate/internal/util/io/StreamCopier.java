package org.hibernate.internal.util.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.hibernate.HibernateException;

public class StreamCopier {
   public static final int BUFFER_SIZE = 4096;
   public static final byte[] BUFFER = new byte[4096];

   public static long copy(InputStream from, OutputStream into) {
      try {
         long totalRead = 0L;

         while(true) {
            synchronized(BUFFER) {
               int amountRead = from.read(BUFFER);
               if (amountRead == -1) {
                  break;
               }

               into.write(BUFFER, 0, amountRead);
               totalRead += (long)amountRead;
               if (amountRead < 4096) {
                  break;
               }
            }
         }

         return totalRead;
      } catch (IOException e) {
         throw new HibernateException("Unable to copy stream content", e);
      }
   }
}
