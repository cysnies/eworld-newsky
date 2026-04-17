package org.hibernate.tool.hbm2ddl;

import java.io.FileWriter;
import java.io.IOException;

class FileExporter implements Exporter {
   private final FileWriter writer;

   public FileExporter(String outputFile) throws IOException {
      this.writer = new FileWriter(outputFile);
   }

   public boolean acceptsImportScripts() {
      return false;
   }

   public void export(String string) throws Exception {
      this.writer.write(string + '\n');
   }

   public void release() throws Exception {
      this.writer.flush();
      this.writer.close();
   }
}
