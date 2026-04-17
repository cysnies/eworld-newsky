package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.sql.ConditionFragment;
import org.hibernate.type.StandardBasicTypes;

public class SizeExpression implements Criterion {
   private final String propertyName;
   private final int size;
   private final String op;

   protected SizeExpression(String propertyName, int size, String op) {
      this.propertyName = propertyName;
      this.size = size;
      this.op = op;
   }

   public String toString() {
      return this.propertyName + ".size" + this.op + this.size;
   }

   public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String role = criteriaQuery.getEntityName(criteria, this.propertyName) + '.' + criteriaQuery.getPropertyName(this.propertyName);
      QueryableCollection cp = (QueryableCollection)criteriaQuery.getFactory().getCollectionPersister(role);
      String[] fk = cp.getKeyColumnNames();
      String[] pk = ((Loadable)cp.getOwnerEntityPersister()).getIdentifierColumnNames();
      return "? " + this.op + " (select count(*) from " + cp.getTableName() + " where " + (new ConditionFragment()).setTableAlias(criteriaQuery.getSQLAlias(criteria, this.propertyName)).setCondition(pk, fk).toFragmentString() + ")";
   }

   public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return new TypedValue[]{new TypedValue(StandardBasicTypes.INTEGER, this.size, EntityMode.POJO)};
   }
}
