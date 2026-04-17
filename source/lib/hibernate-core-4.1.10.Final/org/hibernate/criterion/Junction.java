package org.hibernate.criterion;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;

public class Junction implements Criterion {
   private final Nature nature;
   private final List conditions = new ArrayList();

   protected Junction(Nature nature) {
      this.nature = nature;
   }

   public Junction add(Criterion criterion) {
      this.conditions.add(criterion);
      return this;
   }

   public Nature getNature() {
      return this.nature;
   }

   public Iterable conditions() {
      return this.conditions;
   }

   public TypedValue[] getTypedValues(Criteria crit, CriteriaQuery criteriaQuery) throws HibernateException {
      ArrayList<TypedValue> typedValues = new ArrayList();

      for(Criterion condition : this.conditions) {
         TypedValue[] subValues = condition.getTypedValues(crit, criteriaQuery);
         Collections.addAll(typedValues, subValues);
      }

      return (TypedValue[])typedValues.toArray(new TypedValue[typedValues.size()]);
   }

   public String toSqlString(Criteria crit, CriteriaQuery criteriaQuery) throws HibernateException {
      if (this.conditions.size() == 0) {
         return "1=1";
      } else {
         StringBuilder buffer = (new StringBuilder()).append('(');
         Iterator itr = this.conditions.iterator();

         while(itr.hasNext()) {
            buffer.append(((Criterion)itr.next()).toSqlString(crit, criteriaQuery));
            if (itr.hasNext()) {
               buffer.append(' ').append(this.nature.getOperator()).append(' ');
            }
         }

         return buffer.append(')').toString();
      }
   }

   public String toString() {
      return '(' + StringHelper.join(' ' + this.nature.getOperator() + ' ', this.conditions.iterator()) + ')';
   }

   public static enum Nature {
      AND,
      OR;

      public String getOperator() {
         return this.name().toLowerCase();
      }
   }
}
