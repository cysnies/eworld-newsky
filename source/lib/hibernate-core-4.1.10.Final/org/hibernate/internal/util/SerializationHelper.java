package org.hibernate.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.OutputStream;
import java.io.Serializable;
import org.hibernate.Hibernate;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.SerializationException;
import org.jboss.logging.Logger;

public final class SerializationHelper {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, SerializationHelper.class.getName());

   private SerializationHelper() {
   }

   public static Object clone(Serializable object) throws SerializationException {
      LOG.trace("Starting clone through serialization");
      return object == null ? null : deserialize(serialize(object), object.getClass().getClassLoader());
   }

   public static void serialize(Serializable obj, OutputStream outputStream) throws SerializationException {
      if (outputStream == null) {
         throw new IllegalArgumentException("The OutputStream must not be null");
      } else {
         if (LOG.isTraceEnabled()) {
            if (Hibernate.isInitialized(obj)) {
               LOG.tracev("Starting serialization of object [{0}]", obj);
            } else {
               LOG.trace("Starting serialization of [uninitialized proxy]");
            }
         }

         ObjectOutputStream out = null;

         try {
            out = new ObjectOutputStream(outputStream);
            out.writeObject(obj);
         } catch (IOException ex) {
            throw new SerializationException("could not serialize", ex);
         } finally {
            try {
               if (out != null) {
                  out.close();
               }
            } catch (IOException var10) {
            }

         }

      }
   }

   public static byte[] serialize(Serializable obj) throws SerializationException {
      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
      serialize(obj, byteArrayOutputStream);
      return byteArrayOutputStream.toByteArray();
   }

   public static Object deserialize(InputStream inputStream) throws SerializationException {
      return doDeserialize(inputStream, defaultClassLoader(), hibernateClassLoader(), (ClassLoader)null);
   }

   public static ClassLoader defaultClassLoader() {
      return Thread.currentThread().getContextClassLoader();
   }

   public static ClassLoader hibernateClassLoader() {
      return SerializationHelper.class.getClassLoader();
   }

   public static Object deserialize(InputStream inputStream, ClassLoader loader) throws SerializationException {
      return doDeserialize(inputStream, loader, defaultClassLoader(), hibernateClassLoader());
   }

   public static Object doDeserialize(InputStream inputStream, ClassLoader loader, ClassLoader fallbackLoader1, ClassLoader fallbackLoader2) throws SerializationException {
      if (inputStream == null) {
         throw new IllegalArgumentException("The InputStream must not be null");
      } else {
         LOG.trace("Starting deserialization of object");

         try {
            CustomObjectInputStream in = new CustomObjectInputStream(inputStream, loader, fallbackLoader1, fallbackLoader2);

            Object var5;
            try {
               var5 = in.readObject();
            } catch (ClassNotFoundException e) {
               throw new SerializationException("could not deserialize", e);
            } catch (IOException e) {
               throw new SerializationException("could not deserialize", e);
            } finally {
               try {
                  in.close();
               } catch (IOException var15) {
               }

            }

            return var5;
         } catch (IOException e) {
            throw new SerializationException("could not deserialize", e);
         }
      }
   }

   public static Object deserialize(byte[] objectData) throws SerializationException {
      return doDeserialize(wrap(objectData), defaultClassLoader(), hibernateClassLoader(), (ClassLoader)null);
   }

   private static InputStream wrap(byte[] objectData) {
      if (objectData == null) {
         throw new IllegalArgumentException("The byte[] must not be null");
      } else {
         return new ByteArrayInputStream(objectData);
      }
   }

   public static Object deserialize(byte[] objectData, ClassLoader loader) throws SerializationException {
      return doDeserialize(wrap(objectData), loader, defaultClassLoader(), hibernateClassLoader());
   }

   private static final class CustomObjectInputStream extends ObjectInputStream {
      private final ClassLoader loader1;
      private final ClassLoader loader2;
      private final ClassLoader loader3;

      private CustomObjectInputStream(InputStream in, ClassLoader loader1, ClassLoader loader2, ClassLoader loader3) throws IOException {
         super(in);
         this.loader1 = loader1;
         this.loader2 = loader2;
         this.loader3 = loader3;
      }

      protected Class resolveClass(ObjectStreamClass v) throws IOException, ClassNotFoundException {
         String className = v.getName();
         SerializationHelper.LOG.tracev("Attempting to locate class [{0}]", className);

         try {
            return Class.forName(className, false, this.loader1);
         } catch (ClassNotFoundException var6) {
            SerializationHelper.LOG.trace("Unable to locate class using given classloader");
            if (this.different(this.loader1, this.loader2)) {
               try {
                  return Class.forName(className, false, this.loader2);
               } catch (ClassNotFoundException var5) {
                  SerializationHelper.LOG.trace("Unable to locate class using given classloader");
               }
            }

            if (this.different(this.loader1, this.loader3) && this.different(this.loader2, this.loader3)) {
               try {
                  return Class.forName(className, false, this.loader3);
               } catch (ClassNotFoundException var4) {
                  SerializationHelper.LOG.trace("Unable to locate class using given classloader");
               }
            }

            return super.resolveClass(v);
         }
      }

      private boolean different(ClassLoader one, ClassLoader other) {
         if (one == null) {
            return other != null;
         } else {
            return !one.equals(other);
         }
      }
   }
}
