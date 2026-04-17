package org.hibernate.metamodel.relational;

import java.util.HashMap;
import java.util.Map;

public class Schema {
   private final Name name;
   private Map inLineViews;
   private Map tables;

   public Schema(Name name) {
      this.inLineViews = new HashMap();
      this.tables = new HashMap();
      this.name = name;
   }

   public Schema(Identifier schema, Identifier catalog) {
      this(new Name(schema, catalog));
   }

   public Name getName() {
      return this.name;
   }

   public Table locateTable(Identifier name) {
      return (Table)this.tables.get(name);
   }

   public Table createTable(Identifier name) {
      Table table = new Table(this, name);
      this.tables.put(name, table);
      return table;
   }

   public Table locateOrCreateTable(Identifier name) {
      Table existing = this.locateTable(name);
      return existing == null ? this.createTable(name) : existing;
   }

   public Iterable getTables() {
      return this.tables.values();
   }

   public InLineView getInLineView(String logicalName) {
      return (InLineView)this.inLineViews.get(logicalName);
   }

   public InLineView createInLineView(String logicalName, String subSelect) {
      InLineView inLineView = new InLineView(this, logicalName, subSelect);
      this.inLineViews.put(logicalName, inLineView);
      return inLineView;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Schema");
      sb.append("{name=").append(this.name);
      sb.append('}');
      return sb.toString();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Schema schema = (Schema)o;
         if (this.name != null) {
            if (!this.name.equals(schema.name)) {
               return false;
            }
         } else if (schema.name != null) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.name != null ? this.name.hashCode() : 0;
   }

   public static class Name {
      private final Identifier schema;
      private final Identifier catalog;

      public Name(Identifier schema, Identifier catalog) {
         this.schema = schema;
         this.catalog = catalog;
      }

      public Name(String schema, String catalog) {
         this(Identifier.toIdentifier(schema), Identifier.toIdentifier(catalog));
      }

      public Identifier getSchema() {
         return this.schema;
      }

      public Identifier getCatalog() {
         return this.catalog;
      }

      public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("Name");
         sb.append("{schema=").append(this.schema);
         sb.append(", catalog=").append(this.catalog);
         sb.append('}');
         return sb.toString();
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            Name name = (Name)o;
            if (this.catalog != null) {
               if (!this.catalog.equals(name.catalog)) {
                  return false;
               }
            } else if (name.catalog != null) {
               return false;
            }

            if (this.schema != null) {
               if (!this.schema.equals(name.schema)) {
                  return false;
               }
            } else if (name.schema != null) {
               return false;
            }

            return true;
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.schema != null ? this.schema.hashCode() : 0;
         result = 31 * result + (this.catalog != null ? this.catalog.hashCode() : 0);
         return result;
      }
   }
}
