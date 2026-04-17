package org.hibernate.bytecode.buildtime.spi;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import org.hibernate.bytecode.spi.ByteCodeHelper;
import org.hibernate.bytecode.spi.ClassTransformer;

public abstract class AbstractInstrumenter implements Instrumenter {
   private static final int ZIP_MAGIC = 1347093252;
   private static final int CLASS_MAGIC = -889275714;
   protected final Logger logger;
   protected final Instrumenter.Options options;

   public AbstractInstrumenter(Logger logger, Instrumenter.Options options) {
      this.logger = logger;
      this.options = options;
   }

   protected abstract ClassDescriptor getClassDescriptor(byte[] var1) throws Exception;

   protected abstract ClassTransformer getClassTransformer(ClassDescriptor var1, Set var2);

   public void execute(Set files) {
      Set<String> classNames = new HashSet();
      if (this.options.performExtendedInstrumentation()) {
         this.logger.debug("collecting class names for extended instrumentation determination");

         try {
            for(Object file1 : files) {
               File file = (File)file1;
               this.collectClassNames(file, classNames);
            }
         } catch (ExecutionException ee) {
            throw ee;
         } catch (Exception e) {
            throw new ExecutionException(e);
         }
      }

      this.logger.info("starting instrumentation");

      try {
         for(File file : files) {
            this.processFile(file, classNames);
         }

      } catch (ExecutionException ee) {
         throw ee;
      } catch (Exception e) {
         throw new ExecutionException(e);
      }
   }

   private void collectClassNames(File file, final Set classNames) throws Exception {
      if (this.isClassFile(file)) {
         byte[] bytes = ByteCodeHelper.readByteCode(file);
         ClassDescriptor descriptor = this.getClassDescriptor(bytes);
         classNames.add(descriptor.getName());
      } else if (this.isJarFile(file)) {
         ZipEntryHandler collector = new ZipEntryHandler() {
            public void handleEntry(ZipEntry entry, byte[] byteCode) throws Exception {
               if (!entry.isDirectory()) {
                  DataInputStream din = new DataInputStream(new ByteArrayInputStream(byteCode));
                  if (din.readInt() == -889275714) {
                     classNames.add(AbstractInstrumenter.this.getClassDescriptor(byteCode).getName());
                  }
               }

            }
         };
         ZipFileProcessor processor = new ZipFileProcessor(collector);
         processor.process(file);
      }

   }

   protected final boolean isClassFile(File file) throws IOException {
      return this.checkMagic(file, -889275714L);
   }

   protected final boolean isJarFile(File file) throws IOException {
      return this.checkMagic(file, 1347093252L);
   }

   protected final boolean checkMagic(File file, long magic) throws IOException {
      DataInputStream in = new DataInputStream(new FileInputStream(file));

      boolean var6;
      try {
         int m = in.readInt();
         var6 = magic == (long)m;
      } finally {
         in.close();
      }

      return var6;
   }

   protected void processFile(File file, Set classNames) throws Exception {
      if (this.isClassFile(file)) {
         this.logger.debug("processing class file : " + file.getAbsolutePath());
         this.processClassFile(file, classNames);
      } else if (this.isJarFile(file)) {
         this.logger.debug("processing jar file : " + file.getAbsolutePath());
         this.processJarFile(file, classNames);
      } else {
         this.logger.debug("ignoring file : " + file.getAbsolutePath());
      }

   }

   protected void processClassFile(File file, Set classNames) throws Exception {
      byte[] bytes = ByteCodeHelper.readByteCode(file);
      ClassDescriptor descriptor = this.getClassDescriptor(bytes);
      ClassTransformer transformer = this.getClassTransformer(descriptor, classNames);
      if (transformer == null) {
         this.logger.debug("no trasformer for class file : " + file.getAbsolutePath());
      } else {
         this.logger.info("processing class : " + descriptor.getName() + ";  file = " + file.getAbsolutePath());
         byte[] transformedBytes = transformer.transform(this.getClass().getClassLoader(), descriptor.getName(), (Class)null, (ProtectionDomain)null, descriptor.getBytes());
         OutputStream out = new FileOutputStream(file);

         try {
            out.write(transformedBytes);
            out.flush();
         } finally {
            try {
               out.close();
            } catch (IOException var14) {
            }

         }

      }
   }

   protected void processJarFile(final File file, final Set classNames) throws Exception {
      File tempFile = File.createTempFile(file.getName(), (String)null, new File(file.getAbsoluteFile().getParent()));

      try {
         FileOutputStream fout = new FileOutputStream(tempFile, false);

         try {
            final ZipOutputStream out = new ZipOutputStream(fout);
            ZipEntryHandler transformer = new ZipEntryHandler() {
               public void handleEntry(ZipEntry entry, byte[] byteCode) throws Exception {
                  AbstractInstrumenter.this.logger.debug("starting zip entry : " + entry.toString());
                  if (!entry.isDirectory()) {
                     DataInputStream din = new DataInputStream(new ByteArrayInputStream(byteCode));
                     if (din.readInt() == -889275714) {
                        ClassDescriptor descriptor = AbstractInstrumenter.this.getClassDescriptor(byteCode);
                        ClassTransformer transformer = AbstractInstrumenter.this.getClassTransformer(descriptor, classNames);
                        if (transformer == null) {
                           AbstractInstrumenter.this.logger.debug("no transformer for zip entry :  " + entry.toString());
                        } else {
                           AbstractInstrumenter.this.logger.info("processing class : " + descriptor.getName() + ";  entry = " + file.getAbsolutePath());
                           byteCode = transformer.transform(this.getClass().getClassLoader(), descriptor.getName(), (Class)null, (ProtectionDomain)null, descriptor.getBytes());
                        }
                     } else {
                        AbstractInstrumenter.this.logger.debug("ignoring zip entry : " + entry.toString());
                     }
                  }

                  ZipEntry outEntry = new ZipEntry(entry.getName());
                  outEntry.setMethod(entry.getMethod());
                  outEntry.setComment(entry.getComment());
                  outEntry.setSize((long)byteCode.length);
                  if (outEntry.getMethod() == 0) {
                     CRC32 crc = new CRC32();
                     crc.update(byteCode);
                     outEntry.setCrc(crc.getValue());
                     outEntry.setCompressedSize((long)byteCode.length);
                  }

                  out.putNextEntry(outEntry);
                  out.write(byteCode);
                  out.closeEntry();
               }
            };
            ZipFileProcessor processor = new ZipFileProcessor(transformer);
            processor.process(file);
            out.close();
         } finally {
            fout.close();
         }

         if (!file.delete()) {
            throw new IOException("can not delete " + file);
         }

         File newFile = new File(tempFile.getAbsolutePath());
         if (!newFile.renameTo(file)) {
            throw new IOException("can not rename " + tempFile + " to " + file);
         }
      } finally {
         if (!tempFile.delete()) {
            this.logger.info("Unable to cleanup temporary jar file : " + tempFile.getAbsolutePath());
         }

      }

   }

   protected class CustomFieldFilter implements FieldFilter {
      private final ClassDescriptor descriptor;
      private final Set classNames;

      public CustomFieldFilter(ClassDescriptor descriptor, Set classNames) {
         this.descriptor = descriptor;
         this.classNames = classNames;
      }

      public boolean shouldInstrumentField(String className, String fieldName) {
         if (this.descriptor.getName().equals(className)) {
            AbstractInstrumenter.this.logger.trace("accepting transformation of field [" + className + "." + fieldName + "]");
            return true;
         } else {
            AbstractInstrumenter.this.logger.trace("rejecting transformation of field [" + className + "." + fieldName + "]");
            return false;
         }
      }

      public boolean shouldTransformFieldAccess(String transformingClassName, String fieldOwnerClassName, String fieldName) {
         if (this.descriptor.getName().equals(fieldOwnerClassName)) {
            AbstractInstrumenter.this.logger.trace("accepting transformation of field access [" + fieldOwnerClassName + "." + fieldName + "]");
            return true;
         } else if (AbstractInstrumenter.this.options.performExtendedInstrumentation() && this.classNames.contains(fieldOwnerClassName)) {
            AbstractInstrumenter.this.logger.trace("accepting extended transformation of field access [" + fieldOwnerClassName + "." + fieldName + "]");
            return true;
         } else {
            AbstractInstrumenter.this.logger.trace("rejecting transformation of field access [" + fieldOwnerClassName + "." + fieldName + "]; caller = " + transformingClassName);
            return false;
         }
      }
   }

   private static class ZipFileProcessor {
      private final ZipEntryHandler entryHandler;

      public ZipFileProcessor(ZipEntryHandler entryHandler) {
         this.entryHandler = entryHandler;
      }

      public void process(File file) throws Exception {
         ZipInputStream zip = new ZipInputStream(new FileInputStream(file));

         ZipEntry entry;
         try {
            while((entry = zip.getNextEntry()) != null) {
               byte[] bytes = ByteCodeHelper.readByteCode(zip);
               this.entryHandler.handleEntry(entry, bytes);
               zip.closeEntry();
            }
         } finally {
            zip.close();
         }

      }
   }

   private interface ZipEntryHandler {
      void handleEntry(ZipEntry var1, byte[] var2) throws Exception;
   }
}
