package org.hibernate.metamodel.relational;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import org.hibernate.dialect.Dialect;

public class Table extends AbstractTableSpecification implements Exportable {
   private final Schema database;
   private final Identifier tableName;
   private final ObjectName objectName;
   private final String qualifiedName;
   private final LinkedHashMap indexes;
   private final LinkedHashMap uniqueKeys;
   private final List checkConstraints;
   private final List comments;

   public Table(Schema database, String tableName) {
      this(database, Identifier.toIdentifier(tableName));
   }

   public Table(Schema database, Identifier tableName) {
      this.indexes = new LinkedHashMap();
      this.uniqueKeys = new LinkedHashMap();
      this.checkConstraints = new ArrayList();
      this.comments = new ArrayList();
      this.database = database;
      this.tableName = tableName;
      this.objectName = new ObjectName(database.getName().getSchema(), database.getName().getCatalog(), tableName);
      this.qualifiedName = this.objectName.toText();
   }

   public Schema getSchema() {
      return this.database;
   }

   public Identifier getTableName() {
      return this.tableName;
   }

   public String getLoggableValueQualifier() {
      return this.qualifiedName;
   }

   public String getExportIdentifier() {
      return this.qualifiedName;
   }

   public String toLoggableString() {
      return this.qualifiedName;
   }

   public Iterable getIndexes() {
      return this.indexes.values();
   }

   public Index getOrCreateIndex(String name) {
      if (this.indexes.containsKey(name)) {
         return (Index)this.indexes.get(name);
      } else {
         Index index = new Index(this, name);
         this.indexes.put(name, index);
         return index;
      }
   }

   public Iterable getUniqueKeys() {
      return this.uniqueKeys.values();
   }

   public UniqueKey getOrCreateUniqueKey(String name) {
      if (this.uniqueKeys.containsKey(name)) {
         return (UniqueKey)this.uniqueKeys.get(name);
      } else {
         UniqueKey uniqueKey = new UniqueKey(this, name);
         this.uniqueKeys.put(name, uniqueKey);
         return uniqueKey;
      }
   }

   public Iterable getCheckConstraints() {
      return this.checkConstraints;
   }

   public void addCheckConstraint(String checkCondition) {
      this.checkConstraints.add(new CheckConstraint(this, "", checkCondition));
   }

   public Iterable getComments() {
      return this.comments;
   }

   public void addComment(String comment) {
      this.comments.add(comment);
   }

   public String getQualifiedName(Dialect dialect) {
      return this.objectName.toText(dialect);
   }

   public String[] sqlCreateStrings(Dialect dialect) {
      boolean hasPrimaryKey = this.getPrimaryKey().getColumns().iterator().hasNext();
      StringBuilder buf = (new StringBuilder(hasPrimaryKey ? dialect.getCreateTableString() : dialect.getCreateMultisetTableString())).append(' ').append(this.objectName.toText(dialect)).append(" (");
      boolean isPrimaryKeyIdentity = false;
      String pkColName = null;
      if (hasPrimaryKey && isPrimaryKeyIdentity) {
         Column pkColumn = (Column)this.getPrimaryKey().getColumns().iterator().next();
         pkColName = pkColumn.getColumnName().encloseInQuotesIfQuoted(dialect);
      }

      boolean isFirst = true;

      for(SimpleValue simpleValue : this.values()) {
         if (Column.class.isInstance(simpleValue)) {
            if (isFirst) {
               isFirst = false;
            } else {
               buf.append(", ");
            }

            Column col = (Column)simpleValue;
            String colName = col.getColumnName().encloseInQuotesIfQuoted(dialect);
            buf.append(colName).append(' ');
            if (isPrimaryKeyIdentity && colName.equals(pkColName)) {
               if (dialect.hasDataTypeInIdentityColumn()) {
                  buf.append(getTypeString(col, dialect));
               }

               buf.append(' ').append(dialect.getIdentityColumnString(col.getDatatype().getTypeCode()));
            } else {
               buf.append(getTypeString(col, dialect));
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
               UniqueKey uk = this.getOrCreateUniqueKey(col.getColumnName().encloseInQuotesIfQuoted(dialect) + '_');
               uk.addColumn(col);
               buf.append(dialect.getUniqueDelegate().applyUniqueToColumn(col));
            }

            if (col.getCheckCondition() != null && dialect.supportsColumnCheck()) {
               buf.append(" check (").append(col.getCheckCondition()).append(")");
            }

            String columnComment = col.getComment();
            if (columnComment != null) {
               buf.append(dialect.getColumnComment(columnComment));
            }
         }
      }

      if (hasPrimaryKey) {
         buf.append(", ").append(this.getPrimaryKey().sqlConstraintStringInCreateTable(dialect));
      }

      buf.append(dialect.getUniqueDelegate().applyUniquesToTable(this));
      if (dialect.supportsTableCheck()) {
         for(CheckConstraint checkConstraint : this.checkConstraints) {
            buf.append(", check (").append(checkConstraint).append(')');
         }
      }

      buf.append(')');
      buf.append(dialect.getTableTypeString());
      String[] sqlStrings = new String[this.comments.size() + 1];
      sqlStrings[0] = buf.toString();

      for(int i = 0; i < this.comments.size(); ++i) {
         sqlStrings[i + 1] = dialect.getTableComment((String)this.comments.get(i));
      }

      return sqlStrings;
   }

   private static String getTypeString(Column col, Dialect dialect) {
      String typeString = null;
      if (col.getSqlType() != null) {
         typeString = col.getSqlType();
      } else {
         Size size = col.getSize() == null ? new Size() : col.getSize();
         typeString = dialect.getTypeName(col.getDatatype().getTypeCode(), size.getLength(), size.getPrecision(), size.getScale());
      }

      return typeString;
   }

   public String[] sqlDropStrings(Dialect dialect) {
      return new String[]{dialect.getDropTableString(this.getQualifiedName(dialect))};
   }

   public String toString() {
      return "Table{name=" + this.qualifiedName + '}';
   }
}
