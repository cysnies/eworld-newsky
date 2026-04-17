package org.hibernate.criterion;

import java.util.Collections;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.type.Type;

public class AggregateProjection extends SimpleProjection {
   protected final String propertyName;
   private final String functionName;

   protected AggregateProjection(String functionName, String propertyName) {
      this.functionName = functionName;
      this.propertyName = propertyName;
   }

   public String getFunctionName() {
      return this.functionName;
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public String toString() {
      return this.functionName + "(" + this.propertyName + ')';
   }

   public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new Type[]{this.getFunction(criteriaQuery).getReturnType(criteriaQuery.getType(criteria, this.getPropertyName()), criteriaQuery.getFactory())};
   }

   public String toSqlString(Criteria criteria, int loc, CriteriaQuery criteriaQuery) throws HibernateException {
      String functionFragment = this.getFunction(criteriaQuery).render(criteriaQuery.getType(criteria, this.getPropertyName()), this.buildFunctionParameterList(criteria, criteriaQuery), criteriaQuery.getFactory());
      return functionFragment + " as y" + loc + '_';
   }

   protected SQLFunction getFunction(CriteriaQuery criteriaQuery) {
      return this.getFunction(this.getFunctionName(), criteriaQuery);
   }

   protected SQLFunction getFunction(String functionName, CriteriaQuery criteriaQuery) {
      SQLFunction function = criteriaQuery.getFactory().getSqlFunctionRegistry().findSQLFunction(functionName);
      if (function == null) {
         throw new HibernateException("Unable to locate mapping for function named [" + functionName + "]");
      } else {
         return function;
      }
   }

   protected List buildFunctionParameterList(Criteria criteria, CriteriaQuery criteriaQuery) {
      return this.buildFunctionParameterList(criteriaQuery.getColumn(criteria, this.getPropertyName()));
   }

   protected List buildFunctionParameterList(String column) {
      return Collections.singletonList(column);
   }
}
