package org.hibernate.type;

import org.hibernate.HibernateException;

public interface StringRepresentableType {
   String toString(Object var1) throws HibernateException;

   Object fromStringValue(String var1) throws HibernateException;
}
