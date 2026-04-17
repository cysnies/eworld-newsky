package javax.persistence.criteria;

import java.util.Collection;
import java.util.Map;

public interface CriteriaBuilder {
   CriteriaQuery createQuery();

   CriteriaQuery createQuery(Class var1);

   CriteriaQuery createTupleQuery();

   CompoundSelection construct(Class var1, Selection... var2);

   CompoundSelection tuple(Selection... var1);

   CompoundSelection array(Selection... var1);

   Order asc(Expression var1);

   Order desc(Expression var1);

   Expression avg(Expression var1);

   Expression sum(Expression var1);

   Expression sumAsLong(Expression var1);

   Expression sumAsDouble(Expression var1);

   Expression max(Expression var1);

   Expression min(Expression var1);

   Expression greatest(Expression var1);

   Expression least(Expression var1);

   Expression count(Expression var1);

   Expression countDistinct(Expression var1);

   Predicate exists(Subquery var1);

   Expression all(Subquery var1);

   Expression some(Subquery var1);

   Expression any(Subquery var1);

   Predicate and(Expression var1, Expression var2);

   Predicate and(Predicate... var1);

   Predicate or(Expression var1, Expression var2);

   Predicate or(Predicate... var1);

   Predicate not(Expression var1);

   Predicate conjunction();

   Predicate disjunction();

   Predicate isTrue(Expression var1);

   Predicate isFalse(Expression var1);

   Predicate isNull(Expression var1);

   Predicate isNotNull(Expression var1);

   Predicate equal(Expression var1, Expression var2);

   Predicate equal(Expression var1, Object var2);

   Predicate notEqual(Expression var1, Expression var2);

   Predicate notEqual(Expression var1, Object var2);

   Predicate greaterThan(Expression var1, Expression var2);

   Predicate greaterThan(Expression var1, Comparable var2);

   Predicate greaterThanOrEqualTo(Expression var1, Expression var2);

   Predicate greaterThanOrEqualTo(Expression var1, Comparable var2);

   Predicate lessThan(Expression var1, Expression var2);

   Predicate lessThan(Expression var1, Comparable var2);

   Predicate lessThanOrEqualTo(Expression var1, Expression var2);

   Predicate lessThanOrEqualTo(Expression var1, Comparable var2);

   Predicate between(Expression var1, Expression var2, Expression var3);

   Predicate between(Expression var1, Comparable var2, Comparable var3);

   Predicate gt(Expression var1, Expression var2);

   Predicate gt(Expression var1, Number var2);

   Predicate ge(Expression var1, Expression var2);

   Predicate ge(Expression var1, Number var2);

   Predicate lt(Expression var1, Expression var2);

   Predicate lt(Expression var1, Number var2);

   Predicate le(Expression var1, Expression var2);

   Predicate le(Expression var1, Number var2);

   Expression neg(Expression var1);

   Expression abs(Expression var1);

   Expression sum(Expression var1, Expression var2);

   Expression sum(Expression var1, Number var2);

   Expression sum(Number var1, Expression var2);

   Expression prod(Expression var1, Expression var2);

   Expression prod(Expression var1, Number var2);

   Expression prod(Number var1, Expression var2);

   Expression diff(Expression var1, Expression var2);

   Expression diff(Expression var1, Number var2);

   Expression diff(Number var1, Expression var2);

   Expression quot(Expression var1, Expression var2);

   Expression quot(Expression var1, Number var2);

   Expression quot(Number var1, Expression var2);

   Expression mod(Expression var1, Expression var2);

   Expression mod(Expression var1, Integer var2);

   Expression mod(Integer var1, Expression var2);

   Expression sqrt(Expression var1);

   Expression toLong(Expression var1);

   Expression toInteger(Expression var1);

   Expression toFloat(Expression var1);

   Expression toDouble(Expression var1);

   Expression toBigDecimal(Expression var1);

   Expression toBigInteger(Expression var1);

   Expression toString(Expression var1);

   Expression literal(Object var1);

   Expression nullLiteral(Class var1);

   ParameterExpression parameter(Class var1);

   ParameterExpression parameter(Class var1, String var2);

   Predicate isEmpty(Expression var1);

   Predicate isNotEmpty(Expression var1);

   Expression size(Expression var1);

   Expression size(Collection var1);

   Predicate isMember(Expression var1, Expression var2);

   Predicate isMember(Object var1, Expression var2);

   Predicate isNotMember(Expression var1, Expression var2);

   Predicate isNotMember(Object var1, Expression var2);

   Expression values(Map var1);

   Expression keys(Map var1);

   Predicate like(Expression var1, Expression var2);

   Predicate like(Expression var1, String var2);

   Predicate like(Expression var1, Expression var2, Expression var3);

   Predicate like(Expression var1, Expression var2, char var3);

   Predicate like(Expression var1, String var2, Expression var3);

   Predicate like(Expression var1, String var2, char var3);

   Predicate notLike(Expression var1, Expression var2);

   Predicate notLike(Expression var1, String var2);

   Predicate notLike(Expression var1, Expression var2, Expression var3);

   Predicate notLike(Expression var1, Expression var2, char var3);

   Predicate notLike(Expression var1, String var2, Expression var3);

   Predicate notLike(Expression var1, String var2, char var3);

   Expression concat(Expression var1, Expression var2);

   Expression concat(Expression var1, String var2);

   Expression concat(String var1, Expression var2);

   Expression substring(Expression var1, Expression var2);

   Expression substring(Expression var1, int var2);

   Expression substring(Expression var1, Expression var2, Expression var3);

   Expression substring(Expression var1, int var2, int var3);

   Expression trim(Expression var1);

   Expression trim(Trimspec var1, Expression var2);

   Expression trim(Expression var1, Expression var2);

   Expression trim(Trimspec var1, Expression var2, Expression var3);

   Expression trim(char var1, Expression var2);

   Expression trim(Trimspec var1, char var2, Expression var3);

   Expression lower(Expression var1);

   Expression upper(Expression var1);

   Expression length(Expression var1);

   Expression locate(Expression var1, Expression var2);

   Expression locate(Expression var1, String var2);

   Expression locate(Expression var1, Expression var2, Expression var3);

   Expression locate(Expression var1, String var2, int var3);

   Expression currentDate();

   Expression currentTimestamp();

   Expression currentTime();

   In in(Expression var1);

   Expression coalesce(Expression var1, Expression var2);

   Expression coalesce(Expression var1, Object var2);

   Expression nullif(Expression var1, Expression var2);

   Expression nullif(Expression var1, Object var2);

   Coalesce coalesce();

   SimpleCase selectCase(Expression var1);

   Case selectCase();

   Expression function(String var1, Class var2, Expression... var3);

   public static enum Trimspec {
      LEADING,
      TRAILING,
      BOTH;
   }

   public interface Case extends Expression {
      Case when(Expression var1, Object var2);

      Case when(Expression var1, Expression var2);

      Expression otherwise(Object var1);

      Expression otherwise(Expression var1);
   }

   public interface Coalesce extends Expression {
      Coalesce value(Object var1);

      Coalesce value(Expression var1);
   }

   public interface In extends Predicate {
      Expression getExpression();

      In value(Object var1);

      In value(Expression var1);
   }

   public interface SimpleCase extends Expression {
      Expression getExpression();

      SimpleCase when(Object var1, Object var2);

      SimpleCase when(Object var1, Expression var2);

      Expression otherwise(Object var1);

      Expression otherwise(Expression var1);
   }
}
