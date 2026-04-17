package org.hibernate.metamodel.source.annotations.attribute;

import org.hibernate.AssertionFailure;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.metamodel.source.annotations.JPADotNames;
import org.hibernate.metamodel.source.annotations.JandexHelper;
import org.jboss.jandex.AnnotationInstance;

public class AttributeOverride {
   private static final String PROPERTY_PATH_SEPARATOR = ".";
   private final ColumnValues columnValues;
   private final String attributePath;

   public AttributeOverride(AnnotationInstance attributeOverrideAnnotation) {
      this((String)null, attributeOverrideAnnotation);
   }

   public AttributeOverride(String prefix, AnnotationInstance attributeOverrideAnnotation) {
      if (attributeOverrideAnnotation == null) {
         throw new IllegalArgumentException("An AnnotationInstance needs to be passed");
      } else if (!JPADotNames.ATTRIBUTE_OVERRIDE.equals(attributeOverrideAnnotation.name())) {
         throw new AssertionFailure("A @AttributeOverride annotation needs to be passed to the constructor");
      } else {
         this.columnValues = new ColumnValues((AnnotationInstance)JandexHelper.getValue(attributeOverrideAnnotation, "column", AnnotationInstance.class));
         this.attributePath = this.createAttributePath(prefix, (String)JandexHelper.getValue(attributeOverrideAnnotation, "name", String.class));
      }
   }

   public ColumnValues getColumnValues() {
      return this.columnValues;
   }

   public String getAttributePath() {
      return this.attributePath;
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append("AttributeOverride");
      sb.append("{columnValues=").append(this.columnValues);
      sb.append(", attributePath='").append(this.attributePath).append('\'');
      sb.append('}');
      return sb.toString();
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         AttributeOverride that = (AttributeOverride)o;
         if (this.attributePath != null) {
            if (!this.attributePath.equals(that.attributePath)) {
               return false;
            }
         } else if (that.attributePath != null) {
            return false;
         }

         if (this.columnValues != null) {
            if (!this.columnValues.equals(that.columnValues)) {
               return false;
            }
         } else if (that.columnValues != null) {
            return false;
         }

         return true;
      } else {
         return false;
      }
   }

   public int hashCode() {
      int result = this.columnValues != null ? this.columnValues.hashCode() : 0;
      result = 31 * result + (this.attributePath != null ? this.attributePath.hashCode() : 0);
      return result;
   }

   private String createAttributePath(String prefix, String name) {
      String path = "";
      if (StringHelper.isNotEmpty(prefix)) {
         path = path + prefix;
      }

      if (StringHelper.isNotEmpty(path) && !path.endsWith(".")) {
         path = path + ".";
      }

      path = path + name;
      return path;
   }
}
