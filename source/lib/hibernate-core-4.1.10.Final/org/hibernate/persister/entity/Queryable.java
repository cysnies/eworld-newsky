package org.hibernate.persister.entity;

import org.hibernate.sql.SelectFragment;

public interface Queryable extends Loadable, PropertyMapping, Joinable {
   boolean isAbstract();

   boolean isExplicitPolymorphism();

   String getMappedSuperclass();

   String getDiscriminatorSQLValue();

   String identifierSelectFragment(String var1, String var2);

   String propertySelectFragment(String var1, String var2, boolean var3);

   SelectFragment propertySelectFragmentFragment(String var1, String var2, boolean var3);

   String[] getIdentifierColumnNames();

   boolean isMultiTable();

   String[] getConstraintOrderedTableNameClosure();

   String[][] getContraintOrderedTableKeyColumnClosure();

   String getTemporaryIdTableName();

   String getTemporaryIdTableDDL();

   int getSubclassPropertyTableNumber(String var1);

   Declarer getSubclassPropertyDeclarer(String var1);

   String getSubclassTableName(int var1);

   boolean isVersionPropertyInsertable();

   String generateFilterConditionAlias(String var1);

   DiscriminatorMetadata getTypeDiscriminatorMetadata();

   String[][] getSubclassPropertyFormulaTemplateClosure();

   public static class Declarer {
      public static final Declarer CLASS = new Declarer("class");
      public static final Declarer SUBCLASS = new Declarer("subclass");
      public static final Declarer SUPERCLASS = new Declarer("superclass");
      private final String name;

      public Declarer(String name) {
         this.name = name;
      }

      public String toString() {
         return this.name;
      }
   }
}
