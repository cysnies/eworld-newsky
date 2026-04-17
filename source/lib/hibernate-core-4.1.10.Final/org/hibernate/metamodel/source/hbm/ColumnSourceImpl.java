package org.hibernate.metamodel.source.hbm;

import org.hibernate.internal.jaxb.mapping.hbm.JaxbColumnElement;
import org.hibernate.metamodel.relational.Datatype;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.metamodel.source.binder.ColumnSource;

class ColumnSourceImpl implements ColumnSource {
   private final String tableName;
   private final JaxbColumnElement columnElement;
   private boolean includedInInsert;
   private boolean includedInUpdate;
   private final boolean isForceNotNull;

   ColumnSourceImpl(String tableName, JaxbColumnElement columnElement, boolean isIncludedInInsert, boolean isIncludedInUpdate) {
      this(tableName, columnElement, isIncludedInInsert, isIncludedInUpdate, false);
   }

   ColumnSourceImpl(String tableName, JaxbColumnElement columnElement, boolean isIncludedInInsert, boolean isIncludedInUpdate, boolean isForceNotNull) {
      this.tableName = tableName;
      this.columnElement = columnElement;
      this.isForceNotNull = isForceNotNull;
      this.includedInInsert = isIncludedInInsert;
      this.includedInUpdate = isIncludedInUpdate;
   }

   public String getName() {
      return this.columnElement.getName();
   }

   public boolean isNullable() {
      if (this.isForceNotNull) {
         return false;
      } else {
         return !this.columnElement.isNotNull();
      }
   }

   public String getDefaultValue() {
      return this.columnElement.getDefault();
   }

   public String getSqlType() {
      return this.columnElement.getSqlType();
   }

   public Datatype getDatatype() {
      return null;
   }

   public Size getSize() {
      return new Size(Helper.getIntValue(this.columnElement.getPrecision(), -1), Helper.getIntValue(this.columnElement.getScale(), -1), Helper.getLongValue(this.columnElement.getLength(), -1L), Size.LobMultiplier.NONE);
   }

   public String getReadFragment() {
      return this.columnElement.getRead();
   }

   public String getWriteFragment() {
      return this.columnElement.getWrite();
   }

   public boolean isUnique() {
      return this.columnElement.isUnique();
   }

   public String getCheckCondition() {
      return this.columnElement.getCheck();
   }

   public String getComment() {
      return this.columnElement.getComment();
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
}
