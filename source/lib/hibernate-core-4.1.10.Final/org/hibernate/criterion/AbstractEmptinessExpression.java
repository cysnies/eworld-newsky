package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.QueryException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.sql.ConditionFragment;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;

public abstract class AbstractEmptinessExpression implements Criterion {
   private static final TypedValue[] NO_VALUES = new TypedValue[0];
   protected final String propertyName;

   protected AbstractEmptinessExpression(String propertyName) {
      this.propertyName = propertyName;
   }

   protected abstract boolean excludeEmpty();

   public final String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      String entityName = criteriaQuery.getEntityName(criteria, this.propertyName);
      String actualPropertyName = criteriaQuery.getPropertyName(this.propertyName);
      String sqlAlias = criteriaQuery.getSQLAlias(criteria, this.propertyName);
      SessionFactoryImplementor factory = criteriaQuery.getFactory();
      QueryableCollection collectionPersister = this.getQueryableCollection(entityName, actualPropertyName, factory);
      String[] collectionKeys = collectionPersister.getKeyColumnNames();
      String[] ownerKeys = ((Loadable)factory.getEntityPersister(entityName)).getIdentifierColumnNames();
      String innerSelect = "(select 1 from " + collectionPersister.getTableName() + " where " + (new ConditionFragment()).setTableAlias(sqlAlias).setCondition(ownerKeys, collectionKeys).toFragmentString() + ")";
      return this.excludeEmpty() ? "exists " + innerSelect : "not exists " + innerSelect;
   }

   protected QueryableCollection getQueryableCollection(String entityName, String propertyName, SessionFactoryImplementor factory) throws HibernateException {
      PropertyMapping ownerMapping = (PropertyMapping)factory.getEntityPersister(entityName);
      Type type = ownerMapping.toType(propertyName);
      if (!type.isCollectionType()) {
         throw new MappingException("Property path [" + entityName + "." + propertyName + "] does not reference a collection");
      } else {
         String role = ((CollectionType)type).getRole();

         try {
            return (QueryableCollection)factory.getCollectionPersister(role);
         } catch (ClassCastException var8) {
            throw new QueryException("collection role is not queryable: " + role);
         } catch (Exception var9) {
            throw new QueryException("collection role not found: " + role);
         }
      }
   }

   public final TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
      return NO_VALUES;
   }

   public final String toString() {
      return this.propertyName + (this.excludeEmpty() ? " is not empty" : " is empty");
   }
}
