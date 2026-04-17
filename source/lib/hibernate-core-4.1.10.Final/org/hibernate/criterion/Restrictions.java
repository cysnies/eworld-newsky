package org.hibernate.criterion;

import java.util.Collection;
import java.util.Map;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.Type;

public class Restrictions {
   Restrictions() {
   }

   public static Criterion idEq(Object value) {
      return new IdentifierEqExpression(value);
   }

   public static Criterion eq(String propertyName, Object value) {
      return (Criterion)(null == value ? isNull(propertyName) : new SimpleExpression(propertyName, value, "="));
   }

   public static Criterion ne(String propertyName, Object value) {
      return (Criterion)(null == value ? isNotNull(propertyName) : new SimpleExpression(propertyName, value, "<>"));
   }

   public static SimpleExpression like(String propertyName, Object value) {
      return new SimpleExpression(propertyName, value, " like ");
   }

   public static SimpleExpression like(String propertyName, String value, MatchMode matchMode) {
      return new SimpleExpression(propertyName, matchMode.toMatchString(value), " like ");
   }

   public static Criterion ilike(String propertyName, String value, MatchMode matchMode) {
      return new LikeExpression(propertyName, value, matchMode, (Character)null, true);
   }

   public static Criterion ilike(String propertyName, Object value) {
      if (value == null) {
         throw new IllegalArgumentException("Comparison value passed to ilike cannot be null");
      } else {
         return ilike(propertyName, value.toString(), MatchMode.EXACT);
      }
   }

   public static SimpleExpression gt(String propertyName, Object value) {
      return new SimpleExpression(propertyName, value, ">");
   }

   public static SimpleExpression lt(String propertyName, Object value) {
      return new SimpleExpression(propertyName, value, "<");
   }

   public static SimpleExpression le(String propertyName, Object value) {
      return new SimpleExpression(propertyName, value, "<=");
   }

   public static SimpleExpression ge(String propertyName, Object value) {
      return new SimpleExpression(propertyName, value, ">=");
   }

   public static Criterion between(String propertyName, Object lo, Object hi) {
      return new BetweenExpression(propertyName, lo, hi);
   }

   public static Criterion in(String propertyName, Object[] values) {
      return new InExpression(propertyName, values);
   }

   public static Criterion in(String propertyName, Collection values) {
      return new InExpression(propertyName, values.toArray());
   }

   public static Criterion isNull(String propertyName) {
      return new NullExpression(propertyName);
   }

   public static PropertyExpression eqProperty(String propertyName, String otherPropertyName) {
      return new PropertyExpression(propertyName, otherPropertyName, "=");
   }

   public static PropertyExpression neProperty(String propertyName, String otherPropertyName) {
      return new PropertyExpression(propertyName, otherPropertyName, "<>");
   }

   public static PropertyExpression ltProperty(String propertyName, String otherPropertyName) {
      return new PropertyExpression(propertyName, otherPropertyName, "<");
   }

   public static PropertyExpression leProperty(String propertyName, String otherPropertyName) {
      return new PropertyExpression(propertyName, otherPropertyName, "<=");
   }

   public static PropertyExpression gtProperty(String propertyName, String otherPropertyName) {
      return new PropertyExpression(propertyName, otherPropertyName, ">");
   }

   public static PropertyExpression geProperty(String propertyName, String otherPropertyName) {
      return new PropertyExpression(propertyName, otherPropertyName, ">=");
   }

   public static Criterion isNotNull(String propertyName) {
      return new NotNullExpression(propertyName);
   }

   public static LogicalExpression and(Criterion lhs, Criterion rhs) {
      return new LogicalExpression(lhs, rhs, "and");
   }

   public static Conjunction and(Criterion... predicates) {
      Conjunction conjunction = conjunction();
      if (predicates != null) {
         for(Criterion predicate : predicates) {
            conjunction.add(predicate);
         }
      }

      return conjunction;
   }

   public static LogicalExpression or(Criterion lhs, Criterion rhs) {
      return new LogicalExpression(lhs, rhs, "or");
   }

   public static Disjunction or(Criterion... predicates) {
      Disjunction disjunction = disjunction();
      if (predicates != null) {
         for(Criterion predicate : predicates) {
            disjunction.add(predicate);
         }
      }

      return disjunction;
   }

   public static Criterion not(Criterion expression) {
      return new NotExpression(expression);
   }

   public static Criterion sqlRestriction(String sql, Object[] values, Type[] types) {
      return new SQLCriterion(sql, values, types);
   }

   public static Criterion sqlRestriction(String sql, Object value, Type type) {
      return new SQLCriterion(sql, new Object[]{value}, new Type[]{type});
   }

   public static Criterion sqlRestriction(String sql) {
      return new SQLCriterion(sql, ArrayHelper.EMPTY_OBJECT_ARRAY, ArrayHelper.EMPTY_TYPE_ARRAY);
   }

   public static Conjunction conjunction() {
      return new Conjunction();
   }

   public static Disjunction disjunction() {
      return new Disjunction();
   }

   public static Criterion allEq(Map propertyNameValues) {
      Conjunction conj = conjunction();

      for(Map.Entry me : propertyNameValues.entrySet()) {
         conj.add(eq((String)me.getKey(), me.getValue()));
      }

      return conj;
   }

   public static Criterion isEmpty(String propertyName) {
      return new EmptyExpression(propertyName);
   }

   public static Criterion isNotEmpty(String propertyName) {
      return new NotEmptyExpression(propertyName);
   }

   public static Criterion sizeEq(String propertyName, int size) {
      return new SizeExpression(propertyName, size, "=");
   }

   public static Criterion sizeNe(String propertyName, int size) {
      return new SizeExpression(propertyName, size, "<>");
   }

   public static Criterion sizeGt(String propertyName, int size) {
      return new SizeExpression(propertyName, size, "<");
   }

   public static Criterion sizeLt(String propertyName, int size) {
      return new SizeExpression(propertyName, size, ">");
   }

   public static Criterion sizeGe(String propertyName, int size) {
      return new SizeExpression(propertyName, size, "<=");
   }

   public static Criterion sizeLe(String propertyName, int size) {
      return new SizeExpression(propertyName, size, ">=");
   }

   public static NaturalIdentifier naturalId() {
      return new NaturalIdentifier();
   }
}
