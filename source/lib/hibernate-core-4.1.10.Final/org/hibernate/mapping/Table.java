package org.hibernate.mapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.tool.hbm2ddl.ColumnMetadata;
import org.hibernate.tool.hbm2ddl.TableMetadata;

public class Table implements RelationalModel, Serializable {
   private String name;
   private String schema;
   private String catalog;
   private java.util.Map columns;
   private KeyValue idValue;
   private PrimaryKey primaryKey;
   private java.util.Map indexes;
   private java.util.Map foreignKeys;
   private java.util.Map uniqueKeys;
   private int uniqueInteger;
   private boolean quoted;
   private boolean schemaQuoted;
   private boolean catalogQuoted;
   private java.util.List checkConstraints;
   private String rowId;
   private String subselect;
   private boolean isAbstract;
   private boolean hasDenormalizedTables;
   private String comment;
   private int sizeOfUniqueKeyMapOnLastCleanse;

   public Table() {
      this.columns = new LinkedHashMap();
      this.indexes = new LinkedHashMap();
      this.foreignKeys = new LinkedHashMap();
      this.uniqueKeys = new LinkedHashMap();
      this.checkConstraints = new ArrayList();
      this.hasDenormalizedTables = false;
      this.sizeOfUniqueKeyMapOnLastCleanse = 0;
   }

   public Table(String name) {
      this();
      this.setName(name);
   }

   public String getQualifiedName(Dialect dialect, String defaultCatalog, String defaultSchema) {
      if (this.subselect != null) {
         return "( " + this.subselect + " )";
      } else {
         String quotedName = this.getQuotedName(dialect);
         String usedSchema = this.schema == null ? defaultSchema : this.getQuotedSchema(dialect);
         String usedCatalog = this.catalog == null ? defaultCatalog : this.getQuotedCatalog(dialect);
         return qualify(usedCatalog, usedSchema, quotedName);
      }
   }

   public static String qualify(String catalog, String schema, String table) {
      StringBuilder qualifiedName = new StringBuilder();
      if (catalog != null) {
         qualifiedName.append(catalog).append('.');
      }

      if (schema != null) {
         qualifiedName.append(schema).append('.');
      }

      return qualifiedName.append(table).toString();
   }

   public String getName() {
      return this.name;
   }

   public String getQuotedName() {
      return this.quoted ? "`" + this.name + "`" : this.name;
   }

   public String getQuotedName(Dialect dialect) {
      return this.quoted ? dialect.openQuote() + this.name + dialect.closeQuote() : this.name;
   }

   public String getQuotedSchema() {
      return this.schemaQuoted ? "`" + this.schema + "`" : this.schema;
   }

   public String getQuotedSchema(Dialect dialect) {
      return this.schemaQuoted ? dialect.openQuote() + this.schema + dialect.closeQuote() : this.schema;
   }

   public String getQuotedCatalog() {
      return this.catalogQuoted ? "`" + this.catalog + "`" : this.catalog;
   }

   public String getQuotedCatalog(Dialect dialect) {
      return this.catalogQuoted ? dialect.openQuote() + this.catalog + dialect.closeQuote() : this.catalog;
   }

   public void setName(String name) {
      if (name.charAt(0) == '`') {
         this.quoted = true;
         this.name = name.substring(1, name.length() - 1);
      } else {
         this.name = name;
      }

   }

   public Column getColumn(Column column) {
      if (column == null) {
         return null;
      } else {
         Column myColumn = (Column)this.columns.get(column.getCanonicalName());
         return column.equals(myColumn) ? myColumn : null;
      }
   }

   public Column getColumn(int n) {
      Iterator iter = this.columns.values().iterator();

      for(int i = 0; i < n - 1; ++i) {
         iter.next();
      }

      return (Column)iter.next();
   }

   public void addColumn(Column column) {
      Column old = this.getColumn(column);
      if (old == null) {
         this.columns.put(column.getCanonicalName(), column);
         column.uniqueInteger = this.columns.size();
      } else {
         column.uniqueInteger = old.uniqueInteger;
      }

   }

   public int getColumnSpan() {
      return this.columns.size();
   }

   public Iterator getColumnIterator() {
      return this.columns.values().iterator();
   }

   public Iterator getIndexIterator() {
      return this.indexes.values().iterator();
   }

   public Iterator getForeignKeyIterator() {
      return this.foreignKeys.values().iterator();
   }

   public Iterator getUniqueKeyIterator() {
      return this.getUniqueKeys().values().iterator();
   }

   java.util.Map getUniqueKeys() {
      this.cleanseUniqueKeyMapIfNeeded();
      return this.uniqueKeys;
   }

   private void cleanseUniqueKeyMapIfNeeded() {
      if (this.uniqueKeys.size() != this.sizeOfUniqueKeyMapOnLastCleanse) {
         this.cleanseUniqueKeyMap();
         this.sizeOfUniqueKeyMapOnLastCleanse = this.uniqueKeys.size();
      }
   }

   private void cleanseUniqueKeyMap() {
      if (!this.uniqueKeys.isEmpty()) {
         if (this.uniqueKeys.size() == 1) {
            java.util.Map.Entry<String, UniqueKey> uniqueKeyEntry = (java.util.Map.Entry)this.uniqueKeys.entrySet().iterator().next();
            if (this.isSameAsPrimaryKeyColumns((UniqueKey)uniqueKeyEntry.getValue())) {
               this.uniqueKeys.remove(uniqueKeyEntry.getKey());
            }
         } else {
            Iterator<java.util.Map.Entry<String, UniqueKey>> uniqueKeyEntries = this.uniqueKeys.entrySet().iterator();

            while(uniqueKeyEntries.hasNext()) {
               java.util.Map.Entry<String, UniqueKey> uniqueKeyEntry = (java.util.Map.Entry)uniqueKeyEntries.next();
               UniqueKey uniqueKey = (UniqueKey)uniqueKeyEntry.getValue();
               boolean removeIt = false;

               for(UniqueKey otherUniqueKey : this.uniqueKeys.values()) {
                  if (uniqueKeyEntry.getValue() != otherUniqueKey && otherUniqueKey.getColumns().containsAll(uniqueKey.getColumns()) && uniqueKey.getColumns().containsAll(otherUniqueKey.getColumns())) {
                     removeIt = true;
                     break;
                  }
               }

               if (this.isSameAsPrimaryKeyColumns((UniqueKey)uniqueKeyEntry.getValue())) {
                  removeIt = true;
               }

               if (removeIt) {
                  uniqueKeyEntries.remove();
               }
            }
         }

      }
   }

   private boolean isSameAsPrimaryKeyColumns(UniqueKey uniqueKey) {
      if (this.primaryKey != null && this.primaryKey.columnIterator().hasNext()) {
         return this.primaryKey.getColumns().containsAll(uniqueKey.getColumns()) && uniqueKey.getColumns().containsAll(this.primaryKey.getColumns());
      } else {
         return false;
      }
   }

   public int hashCode() {
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.catalog == null ? 0 : (this.isCatalogQuoted() ? this.catalog.hashCode() : this.catalog.toLowerCase().hashCode()));
      result = 31 * result + (this.name == null ? 0 : (this.isQuoted() ? this.name.hashCode() : this.name.toLowerCase().hashCode()));
      result = 31 * result + (this.schema == null ? 0 : (this.isSchemaQuoted() ? this.schema.hashCode() : this.schema.toLowerCase().hashCode()));
      return result;
   }

   public boolean equals(Object object) {
      return object instanceof Table && this.equals((Table)object);
   }

   public boolean equals(Table table) {
      if (null == table) {
         return false;
      } else if (this == table) {
         return true;
      } else {
         boolean var10000;
         if (this.isQuoted()) {
            var10000 = this.name.equals(table.getName());
         } else {
            label72: {
               if (this.name.equalsIgnoreCase(table.getName()) && (this.schema != null || table.getSchema() == null)) {
                  label67: {
                     if (this.schema != null) {
                        if (this.isSchemaQuoted()) {
                           if (!this.schema.equals(table.getSchema())) {
                              break label67;
                           }
                        } else if (!this.schema.equalsIgnoreCase(table.getSchema())) {
                           break label67;
                        }
                     }

                     if (this.catalog != null || table.getCatalog() == null) {
                        if (this.catalog == null) {
                           break label72;
                        }

                        if (this.isCatalogQuoted()) {
                           if (this.catalog.equals(table.getCatalog())) {
                              break label72;
                           }
                        } else if (this.catalog.equalsIgnoreCase(table.getCatalog())) {
                           break label72;
                        }
                     }
                  }
               }

               var10000 = false;
               return var10000;
            }

            var10000 = true;
         }

         return var10000;
      }
   }

   public void validateColumns(Dialect dialect, Mapping mapping, TableMetadata tableInfo) {
      Iterator iter = this.getColumnIterator();

      while(iter.hasNext()) {
         Column col = (Column)iter.next();
         ColumnMetadata columnInfo = tableInfo.getColumnMetadata(col.getName());
         if (columnInfo == null) {
            throw new HibernateException("Missing column: " + col.getName() + " in " + qualify(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName()));
         }

         boolean typesMatch = col.getSqlType(dialect, mapping).toLowerCase().startsWith(columnInfo.getTypeName().toLowerCase()) || columnInfo.getTypeCode() == col.getSqlTypeCode(mapping);
         if (!typesMatch) {
            throw new HibernateException("Wrong column type in " + qualify(tableInfo.getCatalog(), tableInfo.getSchema(), tableInfo.getName()) + " for column " + col.getName() + ". Found: " + columnInfo.getTypeName().toLowerCase() + ", expected: " + col.getSqlType(dialect, mapping));
         }
      }

   }

   public Iterator sqlAlterStrings(Dialect dialect, Mapping p, TableMetadata tableInfo, String defaultCatalog, String defaultSchema) throws HibernateException {
      StringBuilder root = (new StringBuilder("alter table ")).append(this.getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(' ').append(dialect.getAddColumnString());
      Iterator iter = this.getColumnIterator();
      java.util.List results = new ArrayList();

      while(iter.hasNext()) {
         Column column = (Column)iter.next();
         ColumnMetadata columnInfo = tableInfo.getColumnMetadata(column.getName());
         if (columnInfo == null) {
            StringBuilder alter = (new StringBuilder(root.toString())).append(' ').append(column.getQuotedName(dialect)).append(' ').append(column.getSqlType(dialect, p));
            String defaultValue = column.getDefaultValue();
            if (defaultValue != null) {
               alter.append(" default ").append(defaultValue);
            }

            if (column.isNullable()) {
               alter.append(dialect.getNullColumnString());
            } else {
               alter.append(" not null");
            }

            if (column.isUnique()) {
               UniqueKey uk = this.getOrCreateUniqueKey(column.getQuotedName(dialect) + '_');
               uk.addColumn(column);
               alter.append(dialect.getUniqueDelegate().applyUniqueToColumn(column));
            }

            if (column.hasCheckConstraint() && dialect.supportsColumnCheck()) {
               alter.append(" check(").append(column.getCheckConstraint()).append(")");
            }

            String columnComment = column.getComment();
            if (columnComment != null) {
               alter.append(dialect.getColumnComment(columnComment));
            }

            results.add(alter.toString());
         }
      }

      return results.iterator();
   }

   public boolean hasPrimaryKey() {
      return this.getPrimaryKey() != null;
   }

   public String sqlTemporaryTableCreateString(Dialect dialect, Mapping mapping) throws HibernateException {
      StringBuilder buffer = (new StringBuilder(dialect.getCreateTemporaryTableString())).append(' ').append(this.name).append(" (");
      Iterator itr = this.getColumnIterator();

      while(itr.hasNext()) {
         Column column = (Column)itr.next();
         buffer.append(column.getQuotedName(dialect)).append(' ');
         buffer.append(column.getSqlType(dialect, mapping));
         if (column.isNullable()) {
            buffer.append(dialect.getNullColumnString());
         } else {
            buffer.append(" not null");
         }

         if (itr.hasNext()) {
            buffer.append(", ");
         }
      }

      buffer.append(") ");
      buffer.append(dialect.getCreateTemporaryTablePostfix());
      return buffer.toString();
   }

   public String sqlCreateString(Dialect dialect, Mapping p, String defaultCatalog, String defaultSchema) {
      StringBuilder buf = (new StringBuilder(this.hasPrimaryKey() ? dialect.getCreateTableString() : dialect.getCreateMultisetTableString())).append(' ').append(this.getQualifiedName(dialect, defaultCatalog, defaultSchema)).append(" (");
      boolean identityColumn = this.idValue != null && this.idValue.isIdentityColumn(p.getIdentifierGeneratorFactory(), dialect);
      String pkname = null;
      if (this.hasPrimaryKey() && identityColumn) {
         pkname = ((Column)this.getPrimaryKey().getColumnIterator().next()).getQuotedName(dialect);
      }

      Iterator iter = this.getColumnIterator();

      while(iter.hasNext()) {
         Column col = (Column)iter.next();
         buf.append(col.getQuotedName(dialect)).append(' ');
         if (identityColumn && col.getQuotedName(dialect).equals(pkname)) {
            if (dialect.hasDataTypeInIdentityColumn()) {
               buf.append(col.getSqlType(dialect, p));
            }

            buf.append(' ').append(dialect.getIdentityColumnString(col.getSqlTypeCode(p)));
         } else {
            buf.append(col.getSqlType(dialect, p));
            String defaultValue = col.getDefaultValue();
            if (defaultValue != null) {
               buf.append(" default ").append(defaultValue);
            }

            if (col.isNullable()) {
               buf.append(dialect.getNullColumnString());
            } else {
               buf.append(" not null");
            }
         }

         if (col.isUnique()) {
            UniqueKey uk = this.getOrCreateUniqueKey(col.getQuotedName(dialect) + '_');
            uk.addColumn(col);
            buf.append(dialect.getUniqueDelegate().applyUniqueToColumn(col));
         }

         if (col.hasCheckConstraint() && dialect.supportsColumnCheck()) {
            buf.append(" check (").append(col.getCheckConstraint()).append(")");
         }

         String columnComment = col.getComment();
         if (columnComment != null) {
            buf.append(dialect.getColumnComment(columnComment));
         }

         if (iter.hasNext()) {
            buf.append(", ");
         }
      }

      if (this.hasPrimaryKey()) {
         buf.append(", ").append(this.getPrimaryKey().sqlConstraintString(dialect));
      }

      buf.append(dialect.getUniqueDelegate().applyUniquesToTable(this));
      if (dialect.supportsTableCheck()) {
         Iterator chiter = this.checkConstraints.iterator();

         while(chiter.hasNext()) {
            buf.append(", check (").append(chiter.next()).append(')');
         }
      }

      buf.append(')');
      if (this.comment != null) {
         buf.append(dialect.getTableComment(this.comment));
      }

      return buf.append(dialect.getTableTypeString()).toString();
   }

   public String sqlDropString(Dialect dialect, String defaultCatalog, String defaultSchema) {
      return dialect.getDropTableString(this.getQualifiedName(dialect, defaultCatalog, defaultSchema));
   }

   public PrimaryKey getPrimaryKey() {
      return this.primaryKey;
   }

   public void setPrimaryKey(PrimaryKey primaryKey) {
      this.primaryKey = primaryKey;
   }

   public Index getOrCreateIndex(String indexName) {
      Index index = (Index)this.indexes.get(indexName);
      if (index == null) {
         index = new Index();
         index.setName(indexName);
         index.setTable(this);
         this.indexes.put(indexName, index);
      }

      return index;
   }

   public Index getIndex(String indexName) {
      return (Index)this.indexes.get(indexName);
   }

   public Index addIndex(Index index) {
      Index current = (Index)this.indexes.get(index.getName());
      if (current != null) {
         throw new MappingException("Index " + index.getName() + " already exists!");
      } else {
         this.indexes.put(index.getName(), index);
         return index;
      }
   }

   public UniqueKey addUniqueKey(UniqueKey uniqueKey) {
      UniqueKey current = (UniqueKey)this.uniqueKeys.get(uniqueKey.getName());
      if (current != null) {
         throw new MappingException("UniqueKey " + uniqueKey.getName() + " already exists!");
      } else {
         this.uniqueKeys.put(uniqueKey.getName(), uniqueKey);
         return uniqueKey;
      }
   }

   public UniqueKey createUniqueKey(java.util.List keyColumns) {
      String keyName = "UK" + this.uniqueColumnString(keyColumns.iterator());
      UniqueKey uk = this.getOrCreateUniqueKey(keyName);
      uk.addColumns(keyColumns.iterator());
      return uk;
   }

   public UniqueKey getUniqueKey(String keyName) {
      return (UniqueKey)this.uniqueKeys.get(keyName);
   }

   public UniqueKey getOrCreateUniqueKey(String keyName) {
      UniqueKey uk = (UniqueKey)this.uniqueKeys.get(keyName);
      if (uk == null) {
         uk = new UniqueKey();
         uk.setName(keyName);
         uk.setTable(this);
         this.uniqueKeys.put(keyName, uk);
      }

      return uk;
   }

   public void createForeignKeys() {
   }

   public ForeignKey createForeignKey(String keyName, java.util.List keyColumns, String referencedEntityName) {
      return this.createForeignKey(keyName, keyColumns, referencedEntityName, (java.util.List)null);
   }

   public ForeignKey createForeignKey(String keyName, java.util.List keyColumns, String referencedEntityName, java.util.List referencedColumns) {
      Object key = new ForeignKeyKey(keyColumns, referencedEntityName, referencedColumns);
      ForeignKey fk = (ForeignKey)this.foreignKeys.get(key);
      if (fk == null) {
         fk = new ForeignKey();
         if (keyName != null) {
            fk.setName(keyName);
         } else {
            fk.setName("FK" + this.uniqueColumnString(keyColumns.iterator(), referencedEntityName));
         }

         fk.setTable(this);
         this.foreignKeys.put(key, fk);
         fk.setReferencedEntityName(referencedEntityName);
         fk.addColumns(keyColumns.iterator());
         if (referencedColumns != null) {
            fk.addReferencedColumns(referencedColumns.iterator());
         }
      }

      if (keyName != null) {
         fk.setName(keyName);
      }

      return fk;
   }

   public String uniqueColumnString(Iterator iterator) {
      return this.uniqueColumnString(iterator, (String)null);
   }

   public String uniqueColumnString(Iterator iterator, String referencedEntityName) {
      int result = 0;
      if (referencedEntityName != null) {
         result += referencedEntityName.hashCode();
      }

      while(iterator.hasNext()) {
         result += iterator.next().hashCode();
      }

      return (Integer.toHexString(this.name.hashCode()) + Integer.toHexString(result)).toUpperCase();
   }

   public String getSchema() {
      return this.schema;
   }

   public void setSchema(String schema) {
      if (schema != null && schema.charAt(0) == '`') {
         this.schemaQuoted = true;
         this.schema = schema.substring(1, schema.length() - 1);
      } else {
         this.schema = schema;
      }

   }

   public String getCatalog() {
      return this.catalog;
   }

   public void setCatalog(String catalog) {
      if (catalog != null && catalog.charAt(0) == '`') {
         this.catalogQuoted = true;
         this.catalog = catalog.substring(1, catalog.length() - 1);
      } else {
         this.catalog = catalog;
      }

   }

   public void setUniqueInteger(int uniqueInteger) {
      this.uniqueInteger = uniqueInteger;
   }

   public int getUniqueInteger() {
      return this.uniqueInteger;
   }

   public void setIdentifierValue(KeyValue idValue) {
      this.idValue = idValue;
   }

   public KeyValue getIdentifierValue() {
      return this.idValue;
   }

   public boolean isSchemaQuoted() {
      return this.schemaQuoted;
   }

   public boolean isCatalogQuoted() {
      return this.catalogQuoted;
   }

   public boolean isQuoted() {
      return this.quoted;
   }

   public void setQuoted(boolean quoted) {
      this.quoted = quoted;
   }

   public void addCheckConstraint(String constraint) {
      this.checkConstraints.add(constraint);
   }

   public boolean containsColumn(Column column) {
      return this.columns.containsValue(column);
   }

   public String getRowId() {
      return this.rowId;
   }

   public void setRowId(String rowId) {
      this.rowId = rowId;
   }

   public String toString() {
      StringBuilder buf = (new StringBuilder()).append(this.getClass().getName()).append('(');
      if (this.getCatalog() != null) {
         buf.append(this.getCatalog() + ".");
      }

      if (this.getSchema() != null) {
         buf.append(this.getSchema() + ".");
      }

      buf.append(this.getName()).append(')');
      return buf.toString();
   }

   public String getSubselect() {
      return this.subselect;
   }

   public void setSubselect(String subselect) {
      this.subselect = subselect;
   }

   public boolean isSubselect() {
      return this.subselect != null;
   }

   public boolean isAbstractUnionTable() {
      return this.hasDenormalizedTables() && this.isAbstract;
   }

   public boolean hasDenormalizedTables() {
      return this.hasDenormalizedTables;
   }

   void setHasDenormalizedTables() {
      this.hasDenormalizedTables = true;
   }

   public void setAbstract(boolean isAbstract) {
      this.isAbstract = isAbstract;
   }

   public boolean isAbstract() {
      return this.isAbstract;
   }

   public boolean isPhysicalTable() {
      return !this.isSubselect() && !this.isAbstractUnionTable();
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public Iterator getCheckConstraintsIterator() {
      return this.checkConstraints.iterator();
   }

   public Iterator sqlCommentStrings(Dialect dialect, String defaultCatalog, String defaultSchema) {
      java.util.List comments = new ArrayList();
      if (dialect.supportsCommentOn()) {
         String tableName = this.getQualifiedName(dialect, defaultCatalog, defaultSchema);
         if (this.comment != null) {
            StringBuilder buf = (new StringBuilder()).append("comment on table ").append(tableName).append(" is '").append(this.comment).append("'");
            comments.add(buf.toString());
         }

         Iterator iter = this.getColumnIterator();

         while(iter.hasNext()) {
            Column column = (Column)iter.next();
            String columnComment = column.getComment();
            if (columnComment != null) {
               StringBuilder buf = (new StringBuilder()).append("comment on column ").append(tableName).append('.').append(column.getQuotedName(dialect)).append(" is '").append(columnComment).append("'");
               comments.add(buf.toString());
            }
         }
      }

      return comments.iterator();
   }

   static class ForeignKeyKey implements Serializable {
      String referencedClassName;
      java.util.List columns;
      java.util.List referencedColumns;

      ForeignKeyKey(java.util.List columns, String referencedClassName, java.util.List referencedColumns) {
         this.referencedClassName = referencedClassName;
         this.columns = new ArrayList();
         this.columns.addAll(columns);
         if (referencedColumns != null) {
            this.referencedColumns = new ArrayList();
            this.referencedColumns.addAll(referencedColumns);
         } else {
            this.referencedColumns = Collections.EMPTY_LIST;
         }

      }

      public int hashCode() {
         return this.columns.hashCode() + this.referencedColumns.hashCode();
      }

      public boolean equals(Object other) {
         ForeignKeyKey fkk = (ForeignKeyKey)other;
         return fkk.columns.equals(this.columns) && fkk.referencedClassName.equals(this.referencedClassName) && fkk.referencedColumns.equals(this.referencedColumns);
      }
   }
}
