package javax.persistence.criteria;

import java.util.List;

public interface Predicate extends Expression {
   BooleanOperator getOperator();

   boolean isNegated();

   List getExpressions();

   Predicate not();

   public static enum BooleanOperator {
      AND,
      OR;
   }
}
