package org.hibernate.cfg.annotations;

import javax.persistence.Column;
import org.hibernate.annotations.Columns;

public class CustomizableColumns implements Columns {
   private final Column[] columns;

   public CustomizableColumns(Column[] columns) {
      this.columns = columns;
   }

   public Column[] columns() {
      return this.columns;
   }

   public Class annotationType() {
      return Columns.class;
   }
}
