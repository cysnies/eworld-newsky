package org.hibernate.dialect.function;

public class AvgWithArgumentCastFunction extends StandardAnsiSqlAggregationFunctions.AvgFunction {
   private final String castType;

   public AvgWithArgumentCastFunction(String castType) {
      this.castType = castType;
   }

   protected String renderArgument(String argument, int firstArgumentJdbcType) {
      return firstArgumentJdbcType != 8 && firstArgumentJdbcType != 6 ? "cast(" + argument + " as " + this.castType + ")" : argument;
   }
}
