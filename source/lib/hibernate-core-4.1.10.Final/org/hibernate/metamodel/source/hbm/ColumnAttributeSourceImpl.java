package org.hibernate.metamodel.source.hbm;

import org.hibernate.metamodel.relational.Datatype;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.metamodel.source.binder.ColumnSource;

class ColumnAttributeSourceImpl implements ColumnSource {
   private final String tableName;
   private final String columnName;
   private boolean includedInInsert;
   private boolean includedInUpdate;
   private boolean isForceNotNull;

   ColumnAttributeSourceImpl(String tableName, String columnName, boolean includedInInsert, boolean includedInUpdate) {
      this(tableName, columnName, includedInInsert, includedInUpdate, false);
   }

   ColumnAttributeSourceImpl(String tableName, String columnName, boolean includedInInsert, boolean includedInUpdate, boolean isForceNotNull) {
      this.tableName = tableName;
      this.columnName = columnName;
      this.includedInInsert = includedInInsert;
      this.includedInUpdate = includedInUpdate;
      this.isForceNotNull = isForceNotNull;
   }

   public boolean isIncludedInInsert() {
      return this.includedInInsert;
   }

   public boolean isIncludedInUpdate() {
      return this.includedInUpdate;
   }

   public String getContainingTableName() {
      return this.tableName;
   }

   public String getName() {
      return this.columnName;
   }

   public boolean isNullable() {
      return !this.isForceNotNull;
   }

   public String getDefaultValue() {
      return null;
   }

   public String getSqlType() {
      return null;
   }

   public Datatype getDatatype() {
      return null;
   }

   public Size getSize() {
      return null;
   }

   public String getReadFragment() {
      return null;
   }

   public String getWriteFragment() {
      return null;
   }

   public boolean isUnique() {
      return false;
   }

   public String getCheckCondition() {
      return null;
   }

   public String getComment() {
      return null;
   }
}
