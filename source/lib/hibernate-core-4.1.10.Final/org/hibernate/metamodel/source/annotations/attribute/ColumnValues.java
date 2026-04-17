package org.hibernate.metamodel.source.annotations.attribute;

import org.hibernate.AssertionFailure;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;

public class ColumnValues {
   private String name;
   private boolean unique;
   private boolean nullable;
   private boolean insertable;
   private boolean updatable;
   private String columnDefinition;
   private String table;
   private int length;
   private int precision;
   private int scale;

   ColumnValues() {
      this((AnnotationInstance)null);
   }

   public ColumnValues(AnnotationInstance columnAnnotation) {
      this.name = "";
      this.unique = false;
      this.nullable = true;
      this.insertable = true;
      this.updatable = true;
      this.columnDefinition = "";
      this.table = null;
      this.length = 255;
      this.precision = 0;
      this.scale = 0;
      if (columnAnnotation != null && !JPADotNames.COLUMN.equals(columnAnnotation.name())) {
         throw new AssertionFailure("A @Column annotation needs to be passed to the constructor");
      } else {
         this.applyColumnValues(columnAnnotation);
      }
   }

   private void applyColumnValues(AnnotationInstance columnAnnotation) {
      if (columnAnnotation != null) {
         AnnotationValue nameValue = columnAnnotation.value("name");
         if (nameValue != null) {
            this.name = nameValue.asString();
         }

         AnnotationValue uniqueValue = columnAnnotation.value("unique");
         if (uniqueValue != null) {
            this.unique = nameValue.asBoolean();
         }

         AnnotationValue nullableValue = columnAnnotation.value("nullable");
         if (nullableValue != null) {
            this.nullable = nullableValue.asBoolean();
         }

         AnnotationValue insertableValue = columnAnnotation.value("insertable");
         if (insertableValue != null) {
            this.insertable = insertableValue.asBoolean();
         }

         AnnotationValue updatableValue = columnAnnotation.value("updatable");
         if (updatableValue != null) {
            this.updatable = updatableValue.asBoolean();
         }

         AnnotationValue columnDefinition = columnAnnotation.value("columnDefinition");
         if (columnDefinition != null) {
            this.columnDefinition = columnDefinition.asString();
         }

         AnnotationValue tableValue = columnAnnotation.value("table");
         if (tableValue != null) {
            this.table = tableValue.asString();
         }

         AnnotationValue lengthValue = columnAnnotation.value("length");
         if (lengthValue != null) {
            this.length = lengthValue.asInt();
         }

         AnnotationValue precisionValue = columnAnnotation.value("precision");
         if (precisionValue != null) {
            this.precision = precisionValue.asInt();
         }

         AnnotationValue scaleValue = columnAnnotation.value("scale");
         if (scaleValue != null) {
            this.scale = scaleValue.asInt();
         }

      }
   }

   public final String getName() {
      return this.name;
   }

   public final boolean isUnique() {
      return this.unique;
   }

   public final boolean isNullable() {
      return this.nullable;
   }

   public final boolean isInsertable() {
      return this.insertable;
   }

   public final boolean isUpdatable() {
      return this.updatable;
   }

   public final String getColumnDefinition() {
      return this.columnDefinition;
   }

   public final String getTable() {
      return this.table;
   }

   public final int getLength() {
      return this.length;
   }

   public final int getPrecision() {
      return this.precision;
   }

   public final int getScale() {
      return this.scale;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setUnique(boolean unique) {
      this.unique = unique;
   }

   public void setNullable(boolean nullable) {
      this.nullable = nullable;
   }

   public void setInsertable(boolean insertable) {
      this.insertable = insertable;
   }

   public void setUpdatable(boolean updatable) {
      this.updatable = updatable;
   }

   public void setColumnDefinition(String columnDefinition) {
      this.columnDefinition = columnDefinition;
   }

   public void setTable(String table) {
      this.table = table;
   }

   public void setLength(int length) {
      this.length = length;
   }

   public void setPrecision(int precision) {
      this.precision = precision;
   }

   public void setScale(int scale) {
      this.scale = scale;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("ColumnValues");
      sb.append("{name='").append(this.name).append('\'');
      sb.append(", unique=").append(this.unique);
      sb.append(", nullable=").append(this.nullable);
      sb.append(", insertable=").append(this.insertable);
      sb.append(", updatable=").append(this.updatable);
      sb.append(", columnDefinition='").append(this.columnDefinition).append('\'');
      sb.append(", table='").append(this.table).append('\'');
      sb.append(", length=").append(this.length);
      sb.append(", precision=").append(this.precision);
      sb.append(", scale=").append(this.scale);
      sb.append('}');
      return sb.toString();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         ColumnValues that = (ColumnValues)o;
         if (this.insertable != that.insertable) {
            return false;
         } else if (this.length != that.length) {
            return false;
         } else if (this.nullable != that.nullable) {
            return false;
         } else if (this.precision != that.precision) {
            return false;
         } else if (this.scale != that.scale) {
            return false;
         } else if (this.unique != that.unique) {
            return false;
         } else if (this.updatable != that.updatable) {
            return false;
         } else {
            if (this.columnDefinition != null) {
               if (!this.columnDefinition.equals(that.columnDefinition)) {
                  return false;
               }
            } else if (that.columnDefinition != null) {
               return false;
            }

            if (this.name != null) {
               if (!this.name.equals(that.name)) {
                  return false;
               }
            } else if (that.name != null) {
               return false;
            }

            if (this.table != null) {
               if (!this.table.equals(that.table)) {
                  return false;
               }
            } else if (that.table != null) {
               return false;
            }

            return true;
         }
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.name != null ? this.name.hashCode() : 0;
      result = 31 * result + (this.unique ? 1 : 0);
      result = 31 * result + (this.nullable ? 1 : 0);
      result = 31 * result + (this.insertable ? 1 : 0);
      result = 31 * result + (this.updatable ? 1 : 0);
      result = 31 * result + (this.columnDefinition != null ? this.columnDefinition.hashCode() : 0);
      result = 31 * result + (this.table != null ? this.table.hashCode() : 0);
      result = 31 * result + this.length;
      result = 31 * result + this.precision;
      result = 31 * result + this.scale;
      return result;
   }
}
