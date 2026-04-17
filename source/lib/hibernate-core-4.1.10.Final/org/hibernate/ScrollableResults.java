package org.hibernate;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.hibernate.type.Type;

public interface ScrollableResults {
   boolean next() throws HibernateException;

   boolean previous() throws HibernateException;

   boolean scroll(int var1) throws HibernateException;

   boolean last() throws HibernateException;

   boolean first() throws HibernateException;

   void beforeFirst() throws HibernateException;

   void afterLast() throws HibernateException;

   boolean isFirst() throws HibernateException;

   boolean isLast() throws HibernateException;

   void close() throws HibernateException;

   Object[] get() throws HibernateException;

   Object get(int var1) throws HibernateException;

   Type getType(int var1);

   Integer getInteger(int var1) throws HibernateException;

   Long getLong(int var1) throws HibernateException;

   Float getFloat(int var1) throws HibernateException;

   Boolean getBoolean(int var1) throws HibernateException;

   Double getDouble(int var1) throws HibernateException;

   Short getShort(int var1) throws HibernateException;

   Byte getByte(int var1) throws HibernateException;

   Character getCharacter(int var1) throws HibernateException;

   byte[] getBinary(int var1) throws HibernateException;

   String getText(int var1) throws HibernateException;

   Blob getBlob(int var1) throws HibernateException;

   Clob getClob(int var1) throws HibernateException;

   String getString(int var1) throws HibernateException;

   BigDecimal getBigDecimal(int var1) throws HibernateException;

   BigInteger getBigInteger(int var1) throws HibernateException;

   Date getDate(int var1) throws HibernateException;

   Locale getLocale(int var1) throws HibernateException;

   Calendar getCalendar(int var1) throws HibernateException;

   TimeZone getTimeZone(int var1) throws HibernateException;

   int getRowNumber() throws HibernateException;

   boolean setRowNumber(int var1) throws HibernateException;
}
