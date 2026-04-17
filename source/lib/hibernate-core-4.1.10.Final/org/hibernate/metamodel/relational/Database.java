package org.hibernate.metamodel.relational;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.metamodel.Metadata;

public class Database {
   private final Schema.Name implicitSchemaName;
   private final Map schemaMap = new HashMap();
   private final List auxiliaryDatabaseObjects = new ArrayList();

   public Database(Metadata.Options options) {
      String schemaName = options.getDefaultSchemaName();
      String catalogName = options.getDefaultCatalogName();
      if (options.isGloballyQuotedIdentifiers()) {
         schemaName = StringHelper.quote(schemaName);
         catalogName = StringHelper.quote(catalogName);
      }

      this.implicitSchemaName = new Schema.Name(schemaName, catalogName);
      this.makeSchema(this.implicitSchemaName);
   }

   public Schema getDefaultSchema() {
      return (Schema)this.schemaMap.get(this.implicitSchemaName);
   }

   public Schema locateSchema(Schema.Name name) {
      if (name.getSchema() == null && name.getCatalog() == null) {
         return this.getDefaultSchema();
      } else {
         Schema schema = (Schema)this.schemaMap.get(name);
         if (schema == null) {
            schema = this.makeSchema(name);
         }

         return schema;
      }
   }

   private Schema makeSchema(Schema.Name name) {
      Schema schema = new Schema(name);
      this.schemaMap.put(name, schema);
      return schema;
   }

   public Schema getSchema(Identifier schema, Identifier catalog) {
      return this.locateSchema(new Schema.Name(schema, catalog));
   }

   public Schema getSchema(String schema, String catalog) {
      return this.locateSchema(new Schema.Name(Identifier.toIdentifier(schema), Identifier.toIdentifier(catalog)));
   }

   public void addAuxiliaryDatabaseObject(AuxiliaryDatabaseObject auxiliaryDatabaseObject) {
      if (auxiliaryDatabaseObject == null) {
         throw new IllegalArgumentException("Auxiliary database object is null.");
      } else {
         this.auxiliaryDatabaseObjects.add(auxiliaryDatabaseObject);
      }
   }

   public Iterable getAuxiliaryDatabaseObjects() {
      return this.auxiliaryDatabaseObjects;
   }

   public String[] generateSchemaCreationScript(Dialect dialect) {
      Set<String> exportIdentifiers = new HashSet(50);
      List<String> script = new ArrayList(50);

      for(Schema schema : this.schemaMap.values()) {
         for(Table table : schema.getTables()) {
            addSqlCreateStrings(dialect, exportIdentifiers, script, table);
         }
      }

      for(Schema schema : this.schemaMap.values()) {
         for(Table table : schema.getTables()) {
            for(UniqueKey uniqueKey : table.getUniqueKeys()) {
               addSqlCreateStrings(dialect, exportIdentifiers, script, uniqueKey);
            }

            for(Index index : table.getIndexes()) {
               addSqlCreateStrings(dialect, exportIdentifiers, script, index);
            }

            if (dialect.hasAlterTable()) {
               for(ForeignKey foreignKey : table.getForeignKeys()) {
                  if (Table.class.isInstance(foreignKey.getTargetTable())) {
                     addSqlCreateStrings(dialect, exportIdentifiers, script, foreignKey);
                  }
               }
            }
         }
      }

      for(AuxiliaryDatabaseObject auxiliaryDatabaseObject : this.auxiliaryDatabaseObjects) {
         if (auxiliaryDatabaseObject.appliesToDialect(dialect)) {
            addSqlCreateStrings(dialect, exportIdentifiers, script, auxiliaryDatabaseObject);
         }
      }

      return ArrayHelper.toStringArray((Collection)script);
   }

   public String[] generateDropSchemaScript(Dialect dialect) {
      Set<String> exportIdentifiers = new HashSet(50);
      List<String> script = new ArrayList(50);

      for(int i = this.auxiliaryDatabaseObjects.size() - 1; i >= 0; --i) {
         AuxiliaryDatabaseObject object = (AuxiliaryDatabaseObject)this.auxiliaryDatabaseObjects.get(i);
         if (object.appliesToDialect(dialect)) {
            addSqlDropStrings(dialect, exportIdentifiers, script, object);
         }
      }

      if (dialect.dropConstraints()) {
         for(Schema schema : this.schemaMap.values()) {
            for(Table table : schema.getTables()) {
               for(ForeignKey foreignKey : table.getForeignKeys()) {
                  if (foreignKey.getTargetTable() instanceof Table) {
                     addSqlDropStrings(dialect, exportIdentifiers, script, foreignKey);
                  }
               }
            }
         }
      }

      for(Schema schema : this.schemaMap.values()) {
         for(Table table : schema.getTables()) {
            addSqlDropStrings(dialect, exportIdentifiers, script, table);
         }
      }

      return ArrayHelper.toStringArray((Collection)script);
   }

   private static void addSqlDropStrings(Dialect dialect, Set exportIdentifiers, List script, Exportable exportable) {
      addSqlStrings(exportIdentifiers, script, exportable.getExportIdentifier(), exportable.sqlDropStrings(dialect));
   }

   private static void addSqlCreateStrings(Dialect dialect, Set exportIdentifiers, List script, Exportable exportable) {
      addSqlStrings(exportIdentifiers, script, exportable.getExportIdentifier(), exportable.sqlCreateStrings(dialect));
   }

   private static void addSqlStrings(Set exportIdentifiers, List script, String exportIdentifier, String[] sqlStrings) {
      if (sqlStrings != null) {
         if (exportIdentifiers.contains(exportIdentifier)) {
            throw new MappingException("SQL strings added more than once for: " + exportIdentifier);
         } else {
            exportIdentifiers.add(exportIdentifier);
            script.addAll(Arrays.asList(sqlStrings));
         }
      }
   }
}
