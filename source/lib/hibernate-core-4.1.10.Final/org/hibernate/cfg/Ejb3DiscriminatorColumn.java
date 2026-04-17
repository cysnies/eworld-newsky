package org.hibernate.cfg;

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import org.hibernate.AssertionFailure;
import org.hibernate.annotations.DiscriminatorFormula;

public class Ejb3DiscriminatorColumn extends Ejb3Column {
   private static final String DEFAULT_DISCRIMINATOR_COLUMN_NAME = "DTYPE";
   private static final String DEFAULT_DISCRIMINATOR_TYPE = "string";
   private static final int DEFAULT_DISCRIMINATOR_LENGTH = 31;
   private String discriminatorTypeName;

   public Ejb3DiscriminatorColumn() {
      this.setLogicalColumnName("DTYPE");
      this.setNullable(false);
      this.setDiscriminatorTypeName("string");
      this.setLength(31);
   }

   public String getDiscriminatorTypeName() {
      return this.discriminatorTypeName;
   }

   public void setDiscriminatorTypeName(String discriminatorTypeName) {
      this.discriminatorTypeName = discriminatorTypeName;
   }

   public static Ejb3DiscriminatorColumn buildDiscriminatorColumn(DiscriminatorType type, DiscriminatorColumn discAnn, DiscriminatorFormula discFormulaAnn, Mappings mappings) {
      Ejb3DiscriminatorColumn discriminatorColumn = new Ejb3DiscriminatorColumn();
      discriminatorColumn.setMappings(mappings);
      discriminatorColumn.setImplicit(true);
      if (discFormulaAnn != null) {
         discriminatorColumn.setImplicit(false);
         discriminatorColumn.setFormula(discFormulaAnn.value());
      } else if (discAnn != null) {
         discriminatorColumn.setImplicit(false);
         if (!BinderHelper.isEmptyAnnotationValue(discAnn.columnDefinition())) {
            discriminatorColumn.setSqlType(discAnn.columnDefinition());
         }

         if (!BinderHelper.isEmptyAnnotationValue(discAnn.name())) {
            discriminatorColumn.setLogicalColumnName(discAnn.name());
         }

         discriminatorColumn.setNullable(false);
      }

      if (DiscriminatorType.CHAR.equals(type)) {
         discriminatorColumn.setDiscriminatorTypeName("character");
         discriminatorColumn.setImplicit(false);
      } else if (DiscriminatorType.INTEGER.equals(type)) {
         discriminatorColumn.setDiscriminatorTypeName("integer");
         discriminatorColumn.setImplicit(false);
      } else {
         if (!DiscriminatorType.STRING.equals(type) && type != null) {
            throw new AssertionFailure("Unknown discriminator type: " + type);
         }

         if (discAnn != null) {
            discriminatorColumn.setLength(discAnn.length());
         }

         discriminatorColumn.setDiscriminatorTypeName("string");
      }

      discriminatorColumn.bind();
      return discriminatorColumn;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("Ejb3DiscriminatorColumn");
      sb.append("{logicalColumnName'").append(this.getLogicalColumnName()).append('\'');
      sb.append(", discriminatorTypeName='").append(this.discriminatorTypeName).append('\'');
      sb.append('}');
      return sb.toString();
   }
}
