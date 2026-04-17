package org.hibernate.criterion;

import java.io.Serializable;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.type.Type;

public interface Projection extends Serializable {
   String toSqlString(Criteria var1, int var2, CriteriaQuery var3) throws HibernateException;

   String toGroupSqlString(Criteria var1, CriteriaQuery var2) throws HibernateException;

   Type[] getTypes(Criteria var1, CriteriaQuery var2) throws HibernateException;

   Type[] getTypes(String var1, Criteria var2, CriteriaQuery var3) throws HibernateException;

   String[] getColumnAliases(int var1);

   String[] getColumnAliases(String var1, int var2);

   String[] getAliases();

   boolean isGrouped();
}
