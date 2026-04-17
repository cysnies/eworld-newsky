package org.hibernate.metamodel.source.annotations.xml.mocker;

import org.hibernate.internal.jaxb.mapping.orm.JaxbCollectionTable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbJoinTable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbSecondaryTable;
import org.hibernate.internal.jaxb.mapping.orm.JaxbTable;

interface SchemaAware {
   String getSchema();

   void setSchema(String var1);

   String getCatalog();

   void setCatalog(String var1);

   public static class SecondaryTableSchemaAware implements SchemaAware {
      private JaxbSecondaryTable table;

      SecondaryTableSchemaAware(JaxbSecondaryTable table) {
         this.table = table;
      }

      public String getCatalog() {
         return this.table.getCatalog();
      }

      public String getSchema() {
         return this.table.getSchema();
      }

      public void setSchema(String schema) {
         this.table.setSchema(schema);
      }

      public void setCatalog(String catalog) {
         this.table.setCatalog(catalog);
      }
   }

   public static class TableSchemaAware implements SchemaAware {
      private JaxbTable table;

      public TableSchemaAware(JaxbTable table) {
         this.table = table;
      }

      public String getCatalog() {
         return this.table.getCatalog();
      }

      public String getSchema() {
         return this.table.getSchema();
      }

      public void setSchema(String schema) {
         this.table.setSchema(schema);
      }

      public void setCatalog(String catalog) {
         this.table.setCatalog(catalog);
      }
   }

   public static class JoinTableSchemaAware implements SchemaAware {
      private JaxbJoinTable table;

      public JoinTableSchemaAware(JaxbJoinTable table) {
         this.table = table;
      }

      public String getCatalog() {
         return this.table.getCatalog();
      }

      public String getSchema() {
         return this.table.getSchema();
      }

      public void setSchema(String schema) {
         this.table.setSchema(schema);
      }

      public void setCatalog(String catalog) {
         this.table.setCatalog(catalog);
      }
   }

   public static class CollectionTableSchemaAware implements SchemaAware {
      private JaxbCollectionTable table;

      public CollectionTableSchemaAware(JaxbCollectionTable table) {
         this.table = table;
      }

      public String getCatalog() {
         return this.table.getCatalog();
      }

      public String getSchema() {
         return this.table.getSchema();
      }

      public void setSchema(String schema) {
         this.table.setSchema(schema);
      }

      public void setCatalog(String catalog) {
         this.table.setCatalog(catalog);
      }
   }
}
