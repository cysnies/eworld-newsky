package org.hibernate.type.descriptor.java;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.NClob;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import org.hibernate.HibernateException;
import org.hibernate.engine.jdbc.BinaryStream;
import org.hibernate.engine.jdbc.internal.BinaryStreamImpl;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class DataHelper {
   private static final int BUFFER_SIZE = 4096;
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DataHelper.class.getName());

   public static boolean isNClob(Class type) {
      return NClob.class.isAssignableFrom(type);
   }

   public static String extractString(Reader reader) {
      return extractString(reader, 4096);
   }

   public static String extractString(Reader reader, int lengthHint) {
      int bufferSize = getSuggestedBufferSize(lengthHint);
      StringBuilder stringBuilder = new StringBuilder(bufferSize);

      try {
         char[] buffer = new char[bufferSize];

         while(true) {
            int amountRead = reader.read(buffer, 0, bufferSize);
            if (amountRead == -1) {
               return stringBuilder.toString();
            }

            stringBuilder.append(buffer, 0, amountRead);
         }
      } catch (IOException ioe) {
         throw new HibernateException("IOException occurred reading text", ioe);
      } finally {
         try {
            reader.close();
         } catch (IOException e) {
            LOG.unableToCloseStream(e);
         }

      }
   }

   private static String extractString(Reader characterStream, long start, int length) {
      if (length == 0) {
         return "";
      } else {
         StringBuilder stringBuilder = new StringBuilder(length);

         try {
            long skipped = characterStream.skip(start);
            if (skipped != start) {
               throw new HibernateException("Unable to skip needed bytes");
            }

            int bufferSize = getSuggestedBufferSize(length);
            char[] buffer = new char[bufferSize];
            int charsRead = 0;

            do {
               int amountRead = characterStream.read(buffer, 0, bufferSize);
               if (amountRead == -1) {
                  break;
               }

               stringBuilder.append(buffer, 0, amountRead);
               if (amountRead < bufferSize) {
                  break;
               }

               charsRead += amountRead;
            } while(charsRead < length);
         } catch (IOException ioe) {
            throw new HibernateException("IOException occurred reading a binary value", ioe);
         }

         return stringBuilder.toString();
      }
   }

   public static Object subStream(Reader characterStream, long start, int length) {
      return new StringReader(extractString(characterStream, start, length));
   }

   public static byte[] extractBytes(InputStream inputStream) {
      if (BinaryStream.class.isInstance(inputStream)) {
         return ((BinaryStream)inputStream).getBytes();
      } else {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(4096);

         try {
            byte[] buffer = new byte[4096];

            while(true) {
               int amountRead = inputStream.read(buffer);
               if (amountRead == -1) {
                  return outputStream.toByteArray();
               }

               outputStream.write(buffer, 0, amountRead);
            }
         } catch (IOException ioe) {
            throw new HibernateException("IOException occurred reading a binary value", ioe);
         } finally {
            try {
               inputStream.close();
            } catch (IOException e) {
               LOG.unableToCloseInputStream(e);
            }

            try {
               outputStream.close();
            } catch (IOException e) {
               LOG.unableToCloseOutputStream(e);
            }

         }
      }
   }

   public static byte[] extractBytes(InputStream inputStream, long start, int length) {
      if (BinaryStream.class.isInstance(inputStream) && 2147483647L > start) {
         byte[] data = ((BinaryStream)inputStream).getBytes();
         int size = Math.min(length, data.length);
         byte[] result = new byte[size];
         System.arraycopy(data, (int)start, result, 0, size);
         return result;
      } else {
         ByteArrayOutputStream outputStream = new ByteArrayOutputStream(length);

         try {
            long skipped = inputStream.skip(start);
            if (skipped != start) {
               throw new HibernateException("Unable to skip needed bytes");
            }

            byte[] buffer = new byte[4096];
            int bytesRead = 0;

            do {
               int amountRead = inputStream.read(buffer);
               if (amountRead == -1) {
                  break;
               }

               outputStream.write(buffer, 0, amountRead);
               if (amountRead < buffer.length) {
                  break;
               }

               bytesRead += amountRead;
            } while(bytesRead < length);
         } catch (IOException ioe) {
            throw new HibernateException("IOException occurred reading a binary value", ioe);
         }

         return outputStream.toByteArray();
      }
   }

   public static InputStream subStream(InputStream inputStream, long start, int length) {
      return new BinaryStreamImpl(extractBytes(inputStream, start, length));
   }

   public static String extractString(Clob value) {
      try {
         Reader characterStream = value.getCharacterStream();
         long length = determineLengthForBufferSizing(value);
         return length > 2147483647L ? extractString(characterStream, Integer.MAX_VALUE) : extractString(characterStream, (int)length);
      } catch (SQLException e) {
         throw new HibernateException("Unable to access lob stream", e);
      }
   }

   private static long determineLengthForBufferSizing(Clob value) throws SQLException {
      try {
         return value.length();
      } catch (SQLFeatureNotSupportedException var2) {
         return 4096L;
      }
   }

   private static int getSuggestedBufferSize(int lengthHint) {
      return Math.max(1, Math.min(lengthHint, 4096));
   }
}
