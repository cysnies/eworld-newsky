package org.hibernate.metamodel.source.annotations.attribute;

import org.hibernate.metamodel.relational.Datatype;
import org.hibernate.metamodel.relational.Size;
import org.hibernate.metamodel.source.binder.ColumnSource;

public class ColumnValuesSourceImpl implements ColumnSource {
   private ColumnValues columnValues;

   public ColumnValuesSourceImpl(ColumnValues columnValues) {
      this.columnValues = columnValues;
   }

   void setOverrideColumnValues(ColumnValues columnValues) {
      this.columnValues = columnValues;
   }

   public String getName() {
      return this.columnValues.getName();
   }

   public boolean isNullable() {
      return this.columnValues.isNullable();
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
      return new Size(this.columnValues.getPrecision(), this.columnValues.getScale(), (long)this.columnValues.getLength(), Size.LobMultiplier.NONE);
   }

   public boolean isUnique() {
      return this.columnValues.isUnique();
   }

   public String getComment() {
      return null;
   }

   public boolean isIncludedInInsert() {
      return this.columnValues.isInsertable();
   }

   public boolean isIncludedInUpdate() {
      return this.columnValues.isUpdatable();
   }

   public String getContainingTableName() {
      return this.columnValues.getTable();
   }

   public String getReadFragment() {
      return null;
   }

   public String getWriteFragment() {
      return null;
   }

   public String getCheckCondition() {
      return null;
   }
}
