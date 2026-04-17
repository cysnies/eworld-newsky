package org.hibernate.tool.hbm2ddl;

interface Exporter {
   boolean acceptsImportScripts();

   void export(String var1) throws Exception;

   void release() throws Exception;
}
