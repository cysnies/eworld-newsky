package org.hibernate.tool.hbm2ddl;

class ScriptExporter implements Exporter {
   public boolean acceptsImportScripts() {
      return false;
   }

   public void export(String string) throws Exception {
      System.out.println(string);
   }

   public void release() throws Exception {
   }
}
