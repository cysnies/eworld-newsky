package org.hibernate;

import java.io.Serializable;

public interface SharedSessionContract extends Serializable {
   String getTenantIdentifier();

   Transaction beginTransaction();

   Transaction getTransaction();

   Query getNamedQuery(String var1);

   Query createQuery(String var1);

   SQLQuery createSQLQuery(String var1);

   Criteria createCriteria(Class var1);

   Criteria createCriteria(Class var1, String var2);

   Criteria createCriteria(String var1);

   Criteria createCriteria(String var1, String var2);
}
