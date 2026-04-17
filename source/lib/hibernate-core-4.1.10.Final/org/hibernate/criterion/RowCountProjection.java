package org.hibernate.criterion;

import java.util.Collections;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.dialect.function.SQLFunction;
import org.hibernate.type.Type;

public class RowCountProjection extends SimpleProjection {
   private static List ARGS = Collections.singletonList("*");

   public String toString() {
      return "count(*)";
   }

   public Type[] getTypes(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new Type[]{this.getFunction(criteriaQuery).getReturnType((Type)null, criteriaQuery.getFactory())};
   }

   public String toSqlString(Criteria criteria, int position, CriteriaQuery criteriaQuery) throws HibernateException {
      return this.getFunction(criteriaQuery).render((Type)null, ARGS, criteriaQuery.getFactory()) + " as y" + position + '_';
   }

   protected SQLFunction getFunction(CriteriaQuery criteriaQuery) {
      SQLFunction function = criteriaQuery.getFactory().getSqlFunctionRegistry().findSQLFunction("count");
      if (function == null) {
         throw new HibernateException("Unable to locate count function mapping");
      } else {
         return function;
      }
   }
}
