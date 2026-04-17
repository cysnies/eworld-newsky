package org.hibernate.engine.internal;

import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.type.AssociationType;

public final class JoinHelper {
   private JoinHelper() {
   }

   public static String[] getAliasedLHSColumnNames(AssociationType type, String alias, int property, OuterJoinLoadable lhsPersister, Mapping mapping) {
      return getAliasedLHSColumnNames(type, alias, property, 0, lhsPersister, mapping);
   }

   public static String[] getLHSColumnNames(AssociationType type, int property, OuterJoinLoadable lhsPersister, Mapping mapping) {
      return getLHSColumnNames(type, property, 0, lhsPersister, mapping);
   }

   public static String[] getAliasedLHSColumnNames(AssociationType associationType, String columnQualifier, int propertyIndex, int begin, OuterJoinLoadable lhsPersister, Mapping mapping) {
      if (associationType.useLHSPrimaryKey()) {
         return StringHelper.qualify(columnQualifier, lhsPersister.getIdentifierColumnNames());
      } else {
         String propertyName = associationType.getLHSPropertyName();
         return propertyName == null ? ArrayHelper.slice(toColumns(lhsPersister, columnQualifier, propertyIndex), begin, associationType.getColumnSpan(mapping)) : ((PropertyMapping)lhsPersister).toColumns(columnQualifier, propertyName);
      }
   }

   private static String[] toColumns(OuterJoinLoadable persister, String columnQualifier, int propertyIndex) {
      if (propertyIndex >= 0) {
         return persister.toColumns(columnQualifier, propertyIndex);
      } else {
         String[] cols = persister.getIdentifierColumnNames();
         String[] result = new String[cols.length];

         for(int j = 0; j < cols.length; ++j) {
            result[j] = StringHelper.qualify(columnQualifier, cols[j]);
         }

         return result;
      }
   }

   public static String[] getLHSColumnNames(AssociationType type, int property, int begin, OuterJoinLoadable lhsPersister, Mapping mapping) {
      if (type.useLHSPrimaryKey()) {
         return lhsPersister.getIdentifierColumnNames();
      } else {
         String propertyName = type.getLHSPropertyName();
         return propertyName == null ? ArrayHelper.slice(property < 0 ? lhsPersister.getIdentifierColumnNames() : lhsPersister.getSubclassPropertyColumnNames(property), begin, type.getColumnSpan(mapping)) : lhsPersister.getPropertyColumnNames(propertyName);
      }
   }

   public static String getLHSTableName(AssociationType type, int propertyIndex, OuterJoinLoadable lhsPersister) {
      if (!type.useLHSPrimaryKey() && propertyIndex >= 0) {
         String propertyName = type.getLHSPropertyName();
         if (propertyName == null) {
            return lhsPersister.getSubclassPropertyTableName(propertyIndex);
         } else {
            String propertyRefTable = lhsPersister.getPropertyTableName(propertyName);
            if (propertyRefTable == null) {
               propertyRefTable = lhsPersister.getSubclassPropertyTableName(propertyIndex);
            }

            return propertyRefTable;
         }
      } else {
         return lhsPersister.getTableName();
      }
   }

   public static String[] getRHSColumnNames(AssociationType type, SessionFactoryImplementor factory) {
      String uniqueKeyPropertyName = type.getRHSUniqueKeyPropertyName();
      Joinable joinable = type.getAssociatedJoinable(factory);
      return uniqueKeyPropertyName == null ? joinable.getKeyColumnNames() : ((OuterJoinLoadable)joinable).getPropertyColumnNames(uniqueKeyPropertyName);
   }
}
