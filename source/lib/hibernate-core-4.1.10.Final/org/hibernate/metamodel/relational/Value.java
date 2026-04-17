package org.hibernate.metamodel.relational;

public interface Value {
   TableSpecification getTable();

   String toLoggableString();

   void validateJdbcTypes(JdbcCodes var1);

   public static class JdbcCodes {
      private final int[] typeCodes;
      private int index = 0;

      public JdbcCodes(int[] typeCodes) {
         this.typeCodes = typeCodes;
      }

      public int nextJdbcCde() {
         return this.typeCodes[this.index++];
      }

      public int getIndex() {
         return this.index;
      }
   }
}
