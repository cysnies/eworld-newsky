package org.hibernate.criterion;

public class AvgProjection extends AggregateProjection {
   public AvgProjection(String propertyName) {
      super("avg", propertyName);
   }
}
