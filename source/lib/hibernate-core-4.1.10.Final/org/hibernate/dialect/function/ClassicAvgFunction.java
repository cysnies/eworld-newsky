package org.hibernate.dialect.function;

import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class ClassicAvgFunction extends StandardSQLFunction {
   public ClassicAvgFunction() {
      super("avg");
   }

   public Type getReturnType(Type columnType, Mapping mapping) throws QueryException {
      int[] sqlTypes;
      try {
         sqlTypes = columnType.sqlTypes(mapping);
      } catch (MappingException me) {
         throw new QueryException(me);
      }

      if (sqlTypes.length != 1) {
         throw new QueryException("multi-column type in avg()");
      } else {
         int sqlType = sqlTypes[0];
         return (Type)(sqlType != 4 && sqlType != -5 && sqlType != -6 ? columnType : StandardBasicTypes.FLOAT);
      }
   }
}
