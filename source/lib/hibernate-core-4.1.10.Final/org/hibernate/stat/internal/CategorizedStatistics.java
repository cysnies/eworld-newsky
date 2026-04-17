package org.hibernate.stat.internal;

import java.io.Serializable;

public class CategorizedStatistics implements Serializable {
   private final String categoryName;

   CategorizedStatistics(String categoryName) {
      this.categoryName = categoryName;
   }

   public String getCategoryName() {
      return this.categoryName;
   }
}
