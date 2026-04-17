package org.hibernate.metamodel.relational;

import org.hibernate.HibernateException;
import org.hibernate.dialect.Dialect;

public class ObjectName {
   private static String SEPARATOR = ".";
   private final Identifier schema;
   private final Identifier catalog;
   private final Identifier name;
   private final String identifier;
   private final int hashCode;

   public ObjectName(String objectName) {
      this(extractSchema(objectName), extractCatalog(objectName), extractName(objectName));
   }

   public ObjectName(Identifier name) {
      this((Identifier)null, (Identifier)null, (Identifier)name);
   }

   public ObjectName(Schema schema, String name) {
      this(schema.getName().getSchema(), schema.getName().getCatalog(), Identifier.toIdentifier(name));
   }

   public ObjectName(Schema schema, Identifier name) {
      this(schema.getName().getSchema(), schema.getName().getCatalog(), name);
   }

   public ObjectName(String schemaName, String catalogName, String name) {
      this(Identifier.toIdentifier(schemaName), Identifier.toIdentifier(catalogName), Identifier.toIdentifier(name));
   }

   public ObjectName(Identifier schema, Identifier catalog, Identifier name) {
      if (name == null) {
         throw new IllegalIdentifierException("Object name must be specified");
      } else {
         this.name = name;
         this.schema = schema;
         this.catalog = catalog;
         this.identifier = qualify(schema == null ? null : schema.toString(), catalog == null ? null : catalog.toString(), name.toString());
         int tmpHashCode = schema != null ? schema.hashCode() : 0;
         tmpHashCode = 31 * tmpHashCode + (catalog != null ? catalog.hashCode() : 0);
         tmpHashCode = 31 * tmpHashCode + name.hashCode();
         this.hashCode = tmpHashCode;
      }
   }

   public Identifier getSchema() {
      return this.schema;
   }

   public Identifier getCatalog() {
      return this.catalog;
   }

   public Identifier getName() {
      return this.name;
   }

   public String toText() {
      return this.identifier;
   }

   public String toText(Dialect dialect) {
      if (dialect == null) {
         throw new IllegalArgumentException("dialect must be non-null.");
      } else {
         return qualify(encloseInQuotesIfQuoted(this.schema, dialect), encloseInQuotesIfQuoted(this.catalog, dialect), encloseInQuotesIfQuoted(this.name, dialect));
      }
   }

   private static String encloseInQuotesIfQuoted(Identifier identifier, Dialect dialect) {
      return identifier == null ? null : identifier.encloseInQuotesIfQuoted(dialect);
   }

   private static String qualify(String schema, String catalog, String name) {
      StringBuilder buff = new StringBuilder(name);
      if (catalog != null) {
         buff.insert(0, catalog + '.');
      }

      if (schema != null) {
         buff.insert(0, schema + '.');
      }

      return buff.toString();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ObjectName that = (ObjectName)o;
         return this.name.equals(that.name) && this.areEqual(this.catalog, that.catalog) && this.areEqual(this.schema, that.schema);
      } else {
         return false;
      }
   }

   public int hashCode() {
      return this.hashCode;
   }

   public String toString() {
      return "ObjectName{name='" + this.name + '\'' + ", schema='" + this.schema + '\'' + ", catalog='" + this.catalog + '\'' + '}';
   }

   private boolean areEqual(Identifier one, Identifier other) {
      return one == null ? other == null : one.equals(other);
   }

   private static String extractSchema(String qualifiedName) {
      if (qualifiedName == null) {
         return null;
      } else {
         String[] tokens = qualifiedName.split(SEPARATOR);
         if (tokens.length != 0 && tokens.length != 1) {
            if (tokens.length == 2) {
               return null;
            } else if (tokens.length == 3) {
               return tokens[0];
            } else {
               throw new HibernateException("Unable to parse object name: " + qualifiedName);
            }
         } else {
            return null;
         }
      }
   }

   private static String extractCatalog(String qualifiedName) {
      if (qualifiedName == null) {
         return null;
      } else {
         String[] tokens = qualifiedName.split(SEPARATOR);
         if (tokens.length != 0 && tokens.length != 1) {
            if (tokens.length == 2) {
               return null;
            } else if (tokens.length == 3) {
               return tokens[1];
            } else {
               throw new HibernateException("Unable to parse object name: " + qualifiedName);
            }
         } else {
            return null;
         }
      }
   }

   private static String extractName(String qualifiedName) {
      if (qualifiedName == null) {
         return null;
      } else {
         String[] tokens = qualifiedName.split(SEPARATOR);
         return tokens.length == 0 ? qualifiedName : tokens[tokens.length - 1];
      }
   }
}
