package org.hibernate.cfg;

import org.hibernate.AssertionFailure;
import org.hibernate.internal.util.StringHelper;

public class DefaultComponentSafeNamingStrategy extends EJB3NamingStrategy {
   public static final NamingStrategy INSTANCE = new DefaultComponentSafeNamingStrategy();

   protected static String addUnderscores(String name) {
      return name.replace('.', '_').toLowerCase();
   }

   public String propertyToColumnName(String propertyName) {
      return addUnderscores(propertyName);
   }

   public String collectionTableName(String ownerEntity, String ownerEntityTable, String associatedEntity, String associatedEntityTable, String propertyName) {
      return this.tableName(ownerEntityTable + "_" + (associatedEntityTable != null ? associatedEntityTable : addUnderscores(propertyName)));
   }

   public String foreignKeyColumnName(String propertyName, String propertyEntityName, String propertyTableName, String referencedColumnName) {
      String header = propertyName != null ? addUnderscores(propertyName) : propertyTableName;
      if (header == null) {
         throw new AssertionFailure("NamingStrategy not properly filled");
      } else {
         return this.columnName(header + "_" + referencedColumnName);
      }
   }

   public String logicalColumnName(String columnName, String propertyName) {
      return StringHelper.isNotEmpty(columnName) ? columnName : propertyName;
   }

   public String logicalCollectionTableName(String tableName, String ownerEntityTable, String associatedEntityTable, String propertyName) {
      return tableName != null ? tableName : ownerEntityTable + "_" + (associatedEntityTable != null ? associatedEntityTable : propertyName);
   }

   public String logicalCollectionColumnName(String columnName, String propertyName, String referencedColumn) {
      return StringHelper.isNotEmpty(columnName) ? columnName : propertyName + "_" + referencedColumn;
   }
}
