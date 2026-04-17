package org.hibernate.criterion;

import java.util.Collection;

public class Property extends PropertyProjection {
   protected Property(String propertyName) {
      super(propertyName);
   }

   public Criterion between(Object min, Object max) {
      return Restrictions.between(this.getPropertyName(), min, max);
   }

   public Criterion in(Collection values) {
      return Restrictions.in(this.getPropertyName(), values);
   }

   public Criterion in(Object[] values) {
      return Restrictions.in(this.getPropertyName(), values);
   }

   public SimpleExpression like(Object value) {
      return Restrictions.like(this.getPropertyName(), value);
   }

   public SimpleExpression like(String value, MatchMode matchMode) {
      return Restrictions.like(this.getPropertyName(), value, matchMode);
   }

   public Criterion eq(Object value) {
      return Restrictions.eq(this.getPropertyName(), value);
   }

   public Criterion ne(Object value) {
      return Restrictions.ne(this.getPropertyName(), value);
   }

   public SimpleExpression gt(Object value) {
      return Restrictions.gt(this.getPropertyName(), value);
   }

   public SimpleExpression lt(Object value) {
      return Restrictions.lt(this.getPropertyName(), value);
   }

   public SimpleExpression le(Object value) {
      return Restrictions.le(this.getPropertyName(), value);
   }

   public SimpleExpression ge(Object value) {
      return Restrictions.ge(this.getPropertyName(), value);
   }

   public PropertyExpression eqProperty(Property other) {
      return Restrictions.eqProperty(this.getPropertyName(), other.getPropertyName());
   }

   public PropertyExpression neProperty(Property other) {
      return Restrictions.neProperty(this.getPropertyName(), other.getPropertyName());
   }

   public PropertyExpression leProperty(Property other) {
      return Restrictions.leProperty(this.getPropertyName(), other.getPropertyName());
   }

   public PropertyExpression geProperty(Property other) {
      return Restrictions.geProperty(this.getPropertyName(), other.getPropertyName());
   }

   public PropertyExpression ltProperty(Property other) {
      return Restrictions.ltProperty(this.getPropertyName(), other.getPropertyName());
   }

   public PropertyExpression gtProperty(Property other) {
      return Restrictions.gtProperty(this.getPropertyName(), other.getPropertyName());
   }

   public PropertyExpression eqProperty(String other) {
      return Restrictions.eqProperty(this.getPropertyName(), other);
   }

   public PropertyExpression neProperty(String other) {
      return Restrictions.neProperty(this.getPropertyName(), other);
   }

   public PropertyExpression leProperty(String other) {
      return Restrictions.leProperty(this.getPropertyName(), other);
   }

   public PropertyExpression geProperty(String other) {
      return Restrictions.geProperty(this.getPropertyName(), other);
   }

   public PropertyExpression ltProperty(String other) {
      return Restrictions.ltProperty(this.getPropertyName(), other);
   }

   public PropertyExpression gtProperty(String other) {
      return Restrictions.gtProperty(this.getPropertyName(), other);
   }

   public Criterion isNull() {
      return Restrictions.isNull(this.getPropertyName());
   }

   public Criterion isNotNull() {
      return Restrictions.isNotNull(this.getPropertyName());
   }

   public Criterion isEmpty() {
      return Restrictions.isEmpty(this.getPropertyName());
   }

   public Criterion isNotEmpty() {
      return Restrictions.isNotEmpty(this.getPropertyName());
   }

   public CountProjection count() {
      return Projections.count(this.getPropertyName());
   }

   public AggregateProjection max() {
      return Projections.max(this.getPropertyName());
   }

   public AggregateProjection min() {
      return Projections.min(this.getPropertyName());
   }

   public AggregateProjection avg() {
      return Projections.avg(this.getPropertyName());
   }

   public PropertyProjection group() {
      return Projections.groupProperty(this.getPropertyName());
   }

   public Order asc() {
      return Order.asc(this.getPropertyName());
   }

   public Order desc() {
      return Order.desc(this.getPropertyName());
   }

   public static Property forName(String propertyName) {
      return new Property(propertyName);
   }

   public Property getProperty(String propertyName) {
      return forName(this.getPropertyName() + '.' + propertyName);
   }

   public Criterion eq(DetachedCriteria subselect) {
      return Subqueries.propertyEq(this.getPropertyName(), subselect);
   }

   public Criterion ne(DetachedCriteria subselect) {
      return Subqueries.propertyNe(this.getPropertyName(), subselect);
   }

   public Criterion lt(DetachedCriteria subselect) {
      return Subqueries.propertyLt(this.getPropertyName(), subselect);
   }

   public Criterion le(DetachedCriteria subselect) {
      return Subqueries.propertyLe(this.getPropertyName(), subselect);
   }

   public Criterion gt(DetachedCriteria subselect) {
      return Subqueries.propertyGt(this.getPropertyName(), subselect);
   }

   public Criterion ge(DetachedCriteria subselect) {
      return Subqueries.propertyGe(this.getPropertyName(), subselect);
   }

   public Criterion notIn(DetachedCriteria subselect) {
      return Subqueries.propertyNotIn(this.getPropertyName(), subselect);
   }

   public Criterion in(DetachedCriteria subselect) {
      return Subqueries.propertyIn(this.getPropertyName(), subselect);
   }

   public Criterion eqAll(DetachedCriteria subselect) {
      return Subqueries.propertyEqAll(this.getPropertyName(), subselect);
   }

   public Criterion gtAll(DetachedCriteria subselect) {
      return Subqueries.propertyGtAll(this.getPropertyName(), subselect);
   }

   public Criterion ltAll(DetachedCriteria subselect) {
      return Subqueries.propertyLtAll(this.getPropertyName(), subselect);
   }

   public Criterion leAll(DetachedCriteria subselect) {
      return Subqueries.propertyLeAll(this.getPropertyName(), subselect);
   }

   public Criterion geAll(DetachedCriteria subselect) {
      return Subqueries.propertyGeAll(this.getPropertyName(), subselect);
   }

   public Criterion gtSome(DetachedCriteria subselect) {
      return Subqueries.propertyGtSome(this.getPropertyName(), subselect);
   }

   public Criterion ltSome(DetachedCriteria subselect) {
      return Subqueries.propertyLtSome(this.getPropertyName(), subselect);
   }

   public Criterion leSome(DetachedCriteria subselect) {
      return Subqueries.propertyLeSome(this.getPropertyName(), subselect);
   }

   public Criterion geSome(DetachedCriteria subselect) {
      return Subqueries.propertyGeSome(this.getPropertyName(), subselect);
   }
}
