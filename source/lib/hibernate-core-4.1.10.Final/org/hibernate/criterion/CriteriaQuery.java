package org.hibernate.criterion;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.type.Type;

public interface CriteriaQuery {
   SessionFactoryImplementor getFactory();

   String getColumn(Criteria var1, String var2) throws HibernateException;

   String[] getColumns(String var1, Criteria var2) throws HibernateException;

   String[] findColumns(String var1, Criteria var2) throws HibernateException;

   Type getType(Criteria var1, String var2) throws HibernateException;

   String[] getColumnsUsingProjection(Criteria var1, String var2) throws HibernateException;

   Type getTypeUsingProjection(Criteria var1, String var2) throws HibernateException;

   TypedValue getTypedValue(Criteria var1, String var2, Object var3) throws HibernateException;

   String getEntityName(Criteria var1);

   String getEntityName(Criteria var1, String var2);

   String getSQLAlias(Criteria var1);

   String getSQLAlias(Criteria var1, String var2);

   String getPropertyName(String var1);

   String[] getIdentifierColumns(Criteria var1);

   Type getIdentifierType(Criteria var1);

   TypedValue getTypedIdentifierValue(Criteria var1, Object var2);

   String generateSQLAlias();
}
