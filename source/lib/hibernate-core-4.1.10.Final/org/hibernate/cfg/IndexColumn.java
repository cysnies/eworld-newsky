package org.hibernate.cfg;

import java.util.Map;
import javax.persistence.OrderColumn;
import org.hibernate.mapping.Join;

public class IndexColumn extends Ejb3Column {
   private int base;

   public IndexColumn(boolean isImplicit, String sqlType, int length, int precision, int scale, String name, boolean nullable, boolean unique, boolean insertable, boolean updatable, String secondaryTableName, Map joins, PropertyHolder propertyHolder, Mappings mappings) {
      this.setImplicit(isImplicit);
      this.setSqlType(sqlType);
      this.setLength(length);
      this.setPrecision(precision);
      this.setScale(scale);
      this.setLogicalColumnName(name);
      this.setNullable(nullable);
      this.setUnique(unique);
      this.setInsertable(insertable);
      this.setUpdatable(updatable);
      this.setSecondaryTableName(secondaryTableName);
      this.setPropertyHolder(propertyHolder);
      this.setJoins(joins);
      this.setMappings(mappings);
      this.bind();
   }

   public int getBase() {
      return this.base;
   }

   public void setBase(int base) {
      this.base = base;
   }

   public static IndexColumn buildColumnFromAnnotation(OrderColumn ann, PropertyHolder propertyHolder, PropertyData inferredData, Map secondaryTables, Mappings mappings) {
      IndexColumn column;
      if (ann != null) {
         String sqlType = BinderHelper.isEmptyAnnotationValue(ann.columnDefinition()) ? null : ann.columnDefinition();
         String name = BinderHelper.isEmptyAnnotationValue(ann.name()) ? inferredData.getPropertyName() + "_ORDER" : ann.name();
         column = new IndexColumn(false, sqlType, 0, 0, 0, name, ann.nullable(), false, ann.insertable(), ann.updatable(), (String)null, secondaryTables, propertyHolder, mappings);
      } else {
         column = new IndexColumn(true, (String)null, 0, 0, 0, (String)null, true, false, true, true, (String)null, (Map)null, propertyHolder, mappings);
      }

      return column;
   }

   public static IndexColumn buildColumnFromAnnotation(org.hibernate.annotations.IndexColumn ann, PropertyHolder propertyHolder, PropertyData inferredData, Mappings mappings) {
      IndexColumn column;
      if (ann != null) {
         String sqlType = BinderHelper.isEmptyAnnotationValue(ann.columnDefinition()) ? null : ann.columnDefinition();
         String name = BinderHelper.isEmptyAnnotationValue(ann.name()) ? inferredData.getPropertyName() : ann.name();
         column = new IndexColumn(false, sqlType, 0, 0, 0, name, ann.nullable(), false, true, true, (String)null, (Map)null, propertyHolder, mappings);
         column.setBase(ann.base());
      } else {
         column = new IndexColumn(true, (String)null, 0, 0, 0, (String)null, true, false, true, true, (String)null, (Map)null, propertyHolder, mappings);
      }

      return column;
   }
}
