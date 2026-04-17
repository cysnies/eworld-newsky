package org.hibernate.internal;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.ScrollableResults;
import org.hibernate.engine.spi.QueryParameters;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.hql.internal.HolderInstantiator;
import org.hibernate.loader.Loader;
import org.hibernate.type.StandardBasicTypes;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class AbstractScrollableResults implements ScrollableResults {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractScrollableResults.class.getName());
   private final ResultSet resultSet;
   private final PreparedStatement ps;
   private final SessionImplementor session;
   private final Loader loader;
   private final QueryParameters queryParameters;
   private final Type[] types;
   private HolderInstantiator holderInstantiator;

   public AbstractScrollableResults(ResultSet rs, PreparedStatement ps, SessionImplementor sess, Loader loader, QueryParameters queryParameters, Type[] types, HolderInstantiator holderInstantiator) throws MappingException {
      this.resultSet = rs;
      this.ps = ps;
      this.session = sess;
      this.loader = loader;
      this.queryParameters = queryParameters;
      this.types = types;
      this.holderInstantiator = holderInstantiator != null && holderInstantiator.isRequired() ? holderInstantiator : null;
   }

   protected abstract Object[] getCurrentRow();

   protected ResultSet getResultSet() {
      return this.resultSet;
   }

   protected PreparedStatement getPs() {
      return this.ps;
   }

   protected SessionImplementor getSession() {
      return this.session;
   }

   protected Loader getLoader() {
      return this.loader;
   }

   protected QueryParameters getQueryParameters() {
      return this.queryParameters;
   }

   protected Type[] getTypes() {
      return this.types;
   }

   protected HolderInstantiator getHolderInstantiator() {
      return this.holderInstantiator;
   }

   public final void close() throws HibernateException {
      try {
         this.ps.close();
      } catch (SQLException sqle) {
         throw this.session.getFactory().getSQLExceptionHelper().convert(sqle, "could not close results");
      } finally {
         try {
            this.session.getPersistenceContext().getLoadContexts().cleanup(this.resultSet);
         } catch (Throwable ignore) {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Exception trying to cleanup load context : {0}", ignore.getMessage());
            }
         }

      }

   }

   public final Object[] get() throws HibernateException {
      return this.getCurrentRow();
   }

   public final Object get(int col) throws HibernateException {
      return this.getCurrentRow()[col];
   }

   protected final Object getFinal(int col, Type returnType) throws HibernateException {
      if (this.holderInstantiator != null) {
         throw new HibernateException("query specifies a holder class");
      } else {
         return returnType.getReturnedClass() == this.types[col].getReturnedClass() ? this.get(col) : this.throwInvalidColumnTypeException(col, this.types[col], returnType);
      }
   }

   protected final Object getNonFinal(int col, Type returnType) throws HibernateException {
      if (this.holderInstantiator != null) {
         throw new HibernateException("query specifies a holder class");
      } else {
         return returnType.getReturnedClass().isAssignableFrom(this.types[col].getReturnedClass()) ? this.get(col) : this.throwInvalidColumnTypeException(col, this.types[col], returnType);
      }
   }

   public final BigDecimal getBigDecimal(int col) throws HibernateException {
      return (BigDecimal)this.getFinal(col, StandardBasicTypes.BIG_DECIMAL);
   }

   public final BigInteger getBigInteger(int col) throws HibernateException {
      return (BigInteger)this.getFinal(col, StandardBasicTypes.BIG_INTEGER);
   }

   public final byte[] getBinary(int col) throws HibernateException {
      return (byte[])this.getFinal(col, StandardBasicTypes.BINARY);
   }

   public final String getText(int col) throws HibernateException {
      return (String)this.getFinal(col, StandardBasicTypes.TEXT);
   }

   public final Blob getBlob(int col) throws HibernateException {
      return (Blob)this.getNonFinal(col, StandardBasicTypes.BLOB);
   }

   public final Clob getClob(int col) throws HibernateException {
      return (Clob)this.getNonFinal(col, StandardBasicTypes.CLOB);
   }

   public final Boolean getBoolean(int col) throws HibernateException {
      return (Boolean)this.getFinal(col, StandardBasicTypes.BOOLEAN);
   }

   public final Byte getByte(int col) throws HibernateException {
      return (Byte)this.getFinal(col, StandardBasicTypes.BYTE);
   }

   public final Character getCharacter(int col) throws HibernateException {
      return (Character)this.getFinal(col, StandardBasicTypes.CHARACTER);
   }

   public final Date getDate(int col) throws HibernateException {
      return (Date)this.getNonFinal(col, StandardBasicTypes.TIMESTAMP);
   }

   public final Calendar getCalendar(int col) throws HibernateException {
      return (Calendar)this.getNonFinal(col, StandardBasicTypes.CALENDAR);
   }

   public final Double getDouble(int col) throws HibernateException {
      return (Double)this.getFinal(col, StandardBasicTypes.DOUBLE);
   }

   public final Float getFloat(int col) throws HibernateException {
      return (Float)this.getFinal(col, StandardBasicTypes.FLOAT);
   }

   public final Integer getInteger(int col) throws HibernateException {
      return (Integer)this.getFinal(col, StandardBasicTypes.INTEGER);
   }

   public final Long getLong(int col) throws HibernateException {
      return (Long)this.getFinal(col, StandardBasicTypes.LONG);
   }

   public final Short getShort(int col) throws HibernateException {
      return (Short)this.getFinal(col, StandardBasicTypes.SHORT);
   }

   public final String getString(int col) throws HibernateException {
      return (String)this.getFinal(col, StandardBasicTypes.STRING);
   }

   public final Locale getLocale(int col) throws HibernateException {
      return (Locale)this.getFinal(col, StandardBasicTypes.LOCALE);
   }

   public final TimeZone getTimeZone(int col) throws HibernateException {
      return (TimeZone)this.getNonFinal(col, StandardBasicTypes.TIMEZONE);
   }

   public final Type getType(int i) {
      return this.types[i];
   }

   private Object throwInvalidColumnTypeException(int i, Type type, Type returnType) throws HibernateException {
      throw new HibernateException("incompatible column types: " + type.getName() + ", " + returnType.getName());
   }

   protected void afterScrollOperation() {
      this.session.afterScrollOperation();
   }
}
