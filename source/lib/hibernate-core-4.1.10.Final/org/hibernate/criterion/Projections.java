package org.hibernate.criterion;

import org.hibernate.type.Type;

public final class Projections {
   private Projections() {
   }

   public static Projection distinct(Projection proj) {
      return new Distinct(proj);
   }

   public static ProjectionList projectionList() {
      return new ProjectionList();
   }

   public static Projection rowCount() {
      return new RowCountProjection();
   }

   public static CountProjection count(String propertyName) {
      return new CountProjection(propertyName);
   }

   public static CountProjection countDistinct(String propertyName) {
      return (new CountProjection(propertyName)).setDistinct();
   }

   public static AggregateProjection max(String propertyName) {
      return new AggregateProjection("max", propertyName);
   }

   public static AggregateProjection min(String propertyName) {
      return new AggregateProjection("min", propertyName);
   }

   public static AggregateProjection avg(String propertyName) {
      return new AvgProjection(propertyName);
   }

   public static AggregateProjection sum(String propertyName) {
      return new AggregateProjection("sum", propertyName);
   }

   public static Projection sqlProjection(String sql, String[] columnAliases, Type[] types) {
      return new SQLProjection(sql, columnAliases, types);
   }

   public static Projection sqlGroupProjection(String sql, String groupBy, String[] columnAliases, Type[] types) {
      return new SQLProjection(sql, groupBy, columnAliases, types);
   }

   public static PropertyProjection groupProperty(String propertyName) {
      return new PropertyProjection(propertyName, true);
   }

   public static PropertyProjection property(String propertyName) {
      return new PropertyProjection(propertyName);
   }

   public static IdentifierProjection id() {
      return new IdentifierProjection();
   }

   public static Projection alias(Projection projection, String alias) {
      return new AliasedProjection(projection, alias);
   }
}
