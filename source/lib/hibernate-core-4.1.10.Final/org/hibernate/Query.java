package org.hibernate;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.hibernate.transform.ResultTransformer;
import org.hibernate.type.Type;

public interface Query {
   String getQueryString();

   Type[] getReturnTypes() throws HibernateException;

   String[] getReturnAliases() throws HibernateException;

   String[] getNamedParameters() throws HibernateException;

   Iterator iterate() throws HibernateException;

   ScrollableResults scroll() throws HibernateException;

   ScrollableResults scroll(ScrollMode var1) throws HibernateException;

   List list() throws HibernateException;

   Object uniqueResult() throws HibernateException;

   int executeUpdate() throws HibernateException;

   Query setMaxResults(int var1);

   Query setFirstResult(int var1);

   boolean isReadOnly();

   Query setReadOnly(boolean var1);

   Query setCacheable(boolean var1);

   Query setCacheRegion(String var1);

   Query setTimeout(int var1);

   Query setFetchSize(int var1);

   LockOptions getLockOptions();

   Query setLockOptions(LockOptions var1);

   Query setLockMode(String var1, LockMode var2);

   Query setComment(String var1);

   Query setFlushMode(FlushMode var1);

   Query setCacheMode(CacheMode var1);

   Query setParameter(int var1, Object var2, Type var3);

   Query setParameter(String var1, Object var2, Type var3);

   Query setParameter(int var1, Object var2) throws HibernateException;

   Query setParameter(String var1, Object var2) throws HibernateException;

   Query setParameters(Object[] var1, Type[] var2) throws HibernateException;

   Query setParameterList(String var1, Collection var2, Type var3) throws HibernateException;

   Query setParameterList(String var1, Collection var2) throws HibernateException;

   Query setParameterList(String var1, Object[] var2, Type var3) throws HibernateException;

   Query setParameterList(String var1, Object[] var2) throws HibernateException;

   Query setProperties(Object var1) throws HibernateException;

   Query setProperties(Map var1) throws HibernateException;

   Query setString(int var1, String var2);

   Query setCharacter(int var1, char var2);

   Query setBoolean(int var1, boolean var2);

   Query setByte(int var1, byte var2);

   Query setShort(int var1, short var2);

   Query setInteger(int var1, int var2);

   Query setLong(int var1, long var2);

   Query setFloat(int var1, float var2);

   Query setDouble(int var1, double var2);

   Query setBinary(int var1, byte[] var2);

   Query setText(int var1, String var2);

   Query setSerializable(int var1, Serializable var2);

   Query setLocale(int var1, Locale var2);

   Query setBigDecimal(int var1, BigDecimal var2);

   Query setBigInteger(int var1, BigInteger var2);

   Query setDate(int var1, Date var2);

   Query setTime(int var1, Date var2);

   Query setTimestamp(int var1, Date var2);

   Query setCalendar(int var1, Calendar var2);

   Query setCalendarDate(int var1, Calendar var2);

   Query setString(String var1, String var2);

   Query setCharacter(String var1, char var2);

   Query setBoolean(String var1, boolean var2);

   Query setByte(String var1, byte var2);

   Query setShort(String var1, short var2);

   Query setInteger(String var1, int var2);

   Query setLong(String var1, long var2);

   Query setFloat(String var1, float var2);

   Query setDouble(String var1, double var2);

   Query setBinary(String var1, byte[] var2);

   Query setText(String var1, String var2);

   Query setSerializable(String var1, Serializable var2);

   Query setLocale(String var1, Locale var2);

   Query setBigDecimal(String var1, BigDecimal var2);

   Query setBigInteger(String var1, BigInteger var2);

   Query setDate(String var1, Date var2);

   Query setTime(String var1, Date var2);

   Query setTimestamp(String var1, Date var2);

   Query setCalendar(String var1, Calendar var2);

   Query setCalendarDate(String var1, Calendar var2);

   Query setEntity(int var1, Object var2);

   Query setEntity(String var1, Object var2);

   Query setResultTransformer(ResultTransformer var1);
}
