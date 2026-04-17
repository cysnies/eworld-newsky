package org.hibernate.criterion;

import java.io.Serializable;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.TypedValue;

public interface Criterion extends Serializable {
   String toSqlString(Criteria var1, CriteriaQuery var2) throws HibernateException;

   TypedValue[] getTypedValues(Criteria var1, CriteriaQuery var2) throws HibernateException;
}
