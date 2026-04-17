package org.hibernate.criterion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.hibernate.Criteria;

public class CountProjection extends AggregateProjection {
   private boolean distinct;

   protected CountProjection(String prop) {
      super("count", prop);
   }

   public String toString() {
      return this.distinct ? "distinct " + super.toString() : super.toString();
   }

   protected List buildFunctionParameterList(Criteria criteria, CriteriaQuery criteriaQuery) {
      String[] cols = criteriaQuery.getColumns(this.propertyName, criteria);
      return this.distinct ? this.buildCountDistinctParameterList(cols) : Arrays.asList(cols);
   }

   private List buildCountDistinctParameterList(String[] cols) {
      List params = new ArrayList(cols.length + 1);
      params.add("distinct");
      params.addAll(Arrays.asList(cols));
      return params;
   }

   public CountProjection setDistinct() {
      this.distinct = true;
      return this;
   }
}
