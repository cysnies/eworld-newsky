package org.hibernate.cfg;

import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.internal.util.StringHelper;

public class EJB3NamingStrategy implements NamingStrategy, Serializable {
   public static final NamingStrategy INSTANCE = new EJB3NamingStrategy();

   public String classToTableName(String className) {
      return StringHelper.unqualify(className);
   }

   public String propertyToColumnName(String propertyName) {
      return StringHelper.unqualify(propertyName);
   }

   public String tableName(String tableName) {
      return tableName;
   }

   public String columnName(String columnName) {
      return columnName;
   }

   public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
      return this.tableName(ownerEntityTable + "_" + (associatedEntityTable != null ? associatedEntityTable : StringHelper.unqualify(propertyName)));
   }

   public String joinKeyColumnName(String joinedColumn, String joinedTable) {
      return this.columnName(joinedColumn);
   }

   public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
      String header = propertyName != null ? StringHelper.unqualify(propertyName) : propertyTableName;
      if (header == null) {
         throw new AssertionFailure("NamingStrategy not properly filled");
      } else {
         return this.columnName(header + "_" + referencedColumnName);
      }
   }

   public String logicalColumnName(String columnName, String propertyName) {
      return StringHelper.isNotEmpty(columnName) ? columnName : StringHelper.unqualify(propertyName);
   }

   public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
      return tableName != null ? tableName : ownerEntityTable + "_" + (associatedEntityTable != null ? associatedEntityTable : StringHelper.unqualify(propertyName));
   }

   public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
      return StringHelper.isNotEmpty(columnName) ? columnName : StringHelper.unqualify(propertyName) + "_" + referencedColumn;
   }
}
