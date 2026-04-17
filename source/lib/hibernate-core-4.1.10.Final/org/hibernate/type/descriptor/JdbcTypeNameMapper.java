package org.hibernate.type.descriptor;

import java.lang.reflect.Field;
import java.sql.Types;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.internal.CoreMessageLogger;
import org.jboss.logging.Logger;

public class JdbcTypeNameMapper {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, JdbcTypeNameMapper.class.getName());
   private static Map JDBC_TYPE_MAP = buildJdbcTypeMap();

   private static Map buildJdbcTypeMap() {
      HashMap<Integer, String> map = new HashMap();
      Field[] fields = Types.class.getFields();
      if (fields == null) {
         throw new HibernateException("Unexpected problem extracting JDBC type mapping codes from java.sql.Types");
      } else {
         for(Field field : fields) {
            try {
               int code = field.getInt((Object)null);
               String old = (String)map.put(code, field.getName());
               if (old != null) {
                  LOG.JavaSqlTypesMappedSameCodeMultipleTimes(code, old, field.getName());
               }
            } catch (IllegalAccessException e) {
               throw new HibernateException("Unable to access JDBC type mapping [" + field.getName() + "]", e);
            }
         }

         return Collections.unmodifiableMap(map);
      }
   }

   public static String getTypeName(Integer code) {
      String name = (String)JDBC_TYPE_MAP.get(code);
      return name == null ? "UNKNOWN(" + code + ")" : name;
   }
}
