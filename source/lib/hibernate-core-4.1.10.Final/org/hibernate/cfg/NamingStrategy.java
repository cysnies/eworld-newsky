package org.hibernate.cfg;

public interface NamingStrategy {
   String classToTableName(String var1);

   String propertyToColumnName(String var1);

   String tableName(String var1);

   String columnName(String var1);

   String collectionTableName(String var1, String var2, String var3, String var4, String var5);

   String joinKeyColumnName(String var1, String var2);

   String foreignKeyColumnName(String var1, String var2, String var3, String var4);

   String logicalColumnName(String var1, String var2);

   String logicalCollectionTableName(String var1, String var2, String var3, String var4);

   String logicalCollectionColumnName(String var1, String var2, String var3);
}
