package org.hibernate.criterion;

import java.io.Serializable;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.Type;

public class Order implements Serializable {
   private boolean ascending;
   private boolean ignoreCase;
   private String propertyName;

   public String toString() {
      return this.propertyName + ' ' + (this.ascending ? "asc" : "desc");
   }

   public Order ignoreCase() {
      this.ignoreCase = true;
      return this;
   }

   protected Order(String propertyName, boolean ascending) {
      this.propertyName = propertyName;
      this.ascending = ascending;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String[] columns = criteriaQuery.getColumnsUsingProjection(criteria, this.propertyName);
      Type type = criteriaQuery.getTypeUsingProjection(criteria, this.propertyName);
      StringBuilder fragment = new StringBuilder();

      for(int i = 0; i < columns.length; ++i) {
         SessionFactoryImplementor factory = criteriaQuery.getFactory();
         boolean lower = false;
         if (this.ignoreCase) {
            int sqlType = type.sqlTypes(factory)[i];
            lower = sqlType == 12 || sqlType == 1 || sqlType == -1;
         }

         if (lower) {
            fragment.append(factory.getDialect().getLowercaseFunction()).append('(');
         }

         fragment.append(columns[i]);
         if (lower) {
            fragment.append(')');
         }

         fragment.append(this.ascending ? " asc" : " desc");
         if (i < columns.length - 1) {
            fragment.append(", ");
         }
      }

      return fragment.toString();
   }

   public String getPropertyName() {
      return this.propertyName;
   }

   public boolean isAscending() {
      return this.ascending;
   }

   public boolean isIgnoreCase() {
      return this.ignoreCase;
   }

   public static Order asc(String propertyName) {
      return new Order(propertyName, true);
   }

   public static Order desc(String propertyName) {
      return new Order(propertyName, false);
   }
}
