package org.hibernate.dialect.function;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;

public class StandardAnsiSqlAggregationFunctions {
   public static void primeFunctionMap(Map functionMap) {
      functionMap.put(StandardAnsiSqlAggregationFunctions.AvgFunction.INSTANCE.getName(), StandardAnsiSqlAggregationFunctions.AvgFunction.INSTANCE);
      functionMap.put(StandardAnsiSqlAggregationFunctions.CountFunction.INSTANCE.getName(), StandardAnsiSqlAggregationFunctions.CountFunction.INSTANCE);
      functionMap.put(StandardAnsiSqlAggregationFunctions.MaxFunction.INSTANCE.getName(), StandardAnsiSqlAggregationFunctions.MaxFunction.INSTANCE);
      functionMap.put(StandardAnsiSqlAggregationFunctions.MinFunction.INSTANCE.getName(), StandardAnsiSqlAggregationFunctions.MinFunction.INSTANCE);
      functionMap.put(StandardAnsiSqlAggregationFunctions.SumFunction.INSTANCE.getName(), StandardAnsiSqlAggregationFunctions.SumFunction.INSTANCE);
   }

   public static class CountFunction extends StandardSQLFunction {
      public static final CountFunction INSTANCE = new CountFunction();

      public CountFunction() {
         super("count", StandardBasicTypes.LONG);
      }

      public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) {
         return arguments.size() > 1 && "distinct".equalsIgnoreCase(arguments.get(0).toString()) ? this.renderCountDistinct(arguments) : super.render(firstArgumentType, arguments, factory);
      }

      private String renderCountDistinct(List arguments) {
         StringBuilder buffer = new StringBuilder();
         buffer.append("count(distinct ");
         String sep = "";
         Iterator itr = arguments.iterator();
         itr.next();

         while(itr.hasNext()) {
            buffer.append(sep).append(itr.next());
            sep = ", ";
         }

         return buffer.append(")").toString();
      }
   }

   public static class AvgFunction extends StandardSQLFunction {
      public static final AvgFunction INSTANCE = new AvgFunction();

      public AvgFunction() {
         super("avg", StandardBasicTypes.DOUBLE);
      }

      public String render(Type firstArgumentType, List arguments, SessionFactoryImplementor factory) throws QueryException {
         int jdbcTypeCode = this.determineJdbcTypeCode(firstArgumentType, factory);
         return this.render(jdbcTypeCode, arguments.get(0).toString(), factory);
      }

      protected final int determineJdbcTypeCode(Type firstArgumentType, SessionFactoryImplementor factory) throws QueryException {
         try {
            int[] jdbcTypeCodes = firstArgumentType.sqlTypes(factory);
            if (jdbcTypeCodes.length != 1) {
               throw new QueryException("multiple-column type in avg()");
            } else {
               return jdbcTypeCodes[0];
            }
         } catch (MappingException me) {
            throw new QueryException(me);
         }
      }

      protected String render(int firstArgumentJdbcType, String argument, SessionFactoryImplementor factory) {
         return "avg(" + this.renderArgument(argument, firstArgumentJdbcType) + ")";
      }

      protected String renderArgument(String argument, int firstArgumentJdbcType) {
         return argument;
      }
   }

   public static class MaxFunction extends StandardSQLFunction {
      public static final MaxFunction INSTANCE = new MaxFunction();

      public MaxFunction() {
         super("max");
      }
   }

   public static class MinFunction extends StandardSQLFunction {
      public static final MinFunction INSTANCE = new MinFunction();

      public MinFunction() {
         super("min");
      }
   }

   public static class SumFunction extends StandardSQLFunction {
      public static final SumFunction INSTANCE = new SumFunction();

      public SumFunction() {
         super("sum");
      }

      protected final int determineJdbcTypeCode(Type type, Mapping mapping) throws QueryException {
         try {
            int[] jdbcTypeCodes = type.sqlTypes(mapping);
            if (jdbcTypeCodes.length != 1) {
               throw new QueryException("multiple-column type in sum()");
            } else {
               return jdbcTypeCodes[0];
            }
         } catch (MappingException me) {
            throw new QueryException(me);
         }
      }

      public Type getReturnType(Type firstArgumentType, Mapping mapping) {
         int jdbcType = this.determineJdbcTypeCode(firstArgumentType, mapping);
         if (firstArgumentType == StandardBasicTypes.BIG_INTEGER) {
            return StandardBasicTypes.BIG_INTEGER;
         } else if (firstArgumentType == StandardBasicTypes.BIG_DECIMAL) {
            return StandardBasicTypes.BIG_DECIMAL;
         } else if (firstArgumentType != StandardBasicTypes.LONG && firstArgumentType != StandardBasicTypes.SHORT && firstArgumentType != StandardBasicTypes.INTEGER) {
            if (firstArgumentType != StandardBasicTypes.FLOAT && firstArgumentType != StandardBasicTypes.DOUBLE) {
               if (jdbcType != 6 && jdbcType != 8 && jdbcType != 3 && jdbcType != 7) {
                  return (Type)(jdbcType != -5 && jdbcType != 4 && jdbcType != 5 && jdbcType != -6 ? firstArgumentType : StandardBasicTypes.LONG);
               } else {
                  return StandardBasicTypes.DOUBLE;
               }
            } else {
               return StandardBasicTypes.DOUBLE;
            }
         } else {
            return StandardBasicTypes.LONG;
         }
      }
   }
}
