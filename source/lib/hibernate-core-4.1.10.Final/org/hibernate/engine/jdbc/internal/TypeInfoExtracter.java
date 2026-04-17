package org.hibernate.engine.jdbc.internal;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.jboss.logging.Logger;

public class TypeInfoExtracter {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TypeInfoExtracter.class.getName());

   private TypeInfoExtracter() {
   }

   public static LinkedHashSet extractTypeInfo(DatabaseMetaData metaData) {
      LinkedHashSet<TypeInfo> typeInfoSet = new LinkedHashSet();

      try {
         ResultSet resultSet = metaData.getTypeInfo();

         try {
            while(resultSet.next()) {
               typeInfoSet.add(new TypeInfo(resultSet.getString("TYPE_NAME"), resultSet.getInt("DATA_TYPE"), interpretCreateParams(resultSet.getString("CREATE_PARAMS")), resultSet.getBoolean("UNSIGNED_ATTRIBUTE"), resultSet.getInt("PRECISION"), resultSet.getShort("MINIMUM_SCALE"), resultSet.getShort("MAXIMUM_SCALE"), resultSet.getBoolean("FIXED_PREC_SCALE"), resultSet.getString("LITERAL_PREFIX"), resultSet.getString("LITERAL_SUFFIX"), resultSet.getBoolean("CASE_SENSITIVE"), TypeSearchability.interpret(resultSet.getShort("SEARCHABLE")), TypeNullability.interpret(resultSet.getShort("NULLABLE"))));
            }
         } catch (SQLException e) {
            LOG.unableToAccessTypeInfoResultSet(e.toString());
         } finally {
            try {
               resultSet.close();
            } catch (SQLException var12) {
               LOG.unableToReleaseTypeInfoResultSet();
            }

         }
      } catch (SQLException e) {
         LOG.unableToRetrieveTypeInfoResultSet(e.toString());
      }

      return typeInfoSet;
   }

   private static String[] interpretCreateParams(String value) {
      return value != null && value.length() != 0 ? value.split(",") : ArrayHelper.EMPTY_STRING_ARRAY;
   }
}
