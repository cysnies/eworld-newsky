package org.hibernate.type;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.dom4j.Node;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.metamodel.relational.Size;
import org.jboss.logging.Logger;

/** @deprecated */
@Deprecated
public abstract class NullableType extends AbstractType implements StringRepresentableType, XmlRepresentableType {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, NullableType.class.getName());
   private final Size dictatedSize = new Size();

   public abstract int sqlType();

   public Size dictatedSize() {
      return this.dictatedSize;
   }

   public Size defaultSize() {
      return LEGACY_DEFAULT_SIZE;
   }

   public abstract Object get(ResultSet var1, String var2) throws HibernateException, SQLException;

   public abstract void set(PreparedStatement var1, Object var2, int var3) throws HibernateException, SQLException;

   public String nullSafeToString(Object value) throws HibernateException {
      return value == null ? null : this.toString(value);
   }

   public abstract String toString(Object var1) throws HibernateException;

   public abstract Object fromStringValue(String var1) throws HibernateException;

   public final void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      if (settable[0]) {
         this.nullSafeSet(st, value, index);
      }

   }

   public final void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.nullSafeSet(st, value, index);
   }

   public final void nullSafeSet(PreparedStatement st, Object value, int index) throws HibernateException, SQLException {
      try {
         if (value == null) {
            LOG.tracev("Binding null to parameter: {0}", index);
            st.setNull(index, this.sqlType());
         } else {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Binding '{0}' to parameter: {1}", this.toString(value), index);
            }

            this.set(st, value, index);
         }

      } catch (RuntimeException re) {
         LOG.unableToBindValueToParameter(this.nullSafeToString(value), index, re.getMessage());
         throw re;
      } catch (SQLException se) {
         LOG.unableToBindValueToParameter(this.nullSafeToString(value), index, se.getMessage());
         throw se;
      }
   }

   public final Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, names[0]);
   }

   public final Object nullSafeGet(ResultSet rs, String[] names) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, names[0]);
   }

   public final Object nullSafeGet(ResultSet rs, String name) throws HibernateException, SQLException {
      try {
         Object value = this.get(rs, name);
         if (value != null && !rs.wasNull()) {
            if (LOG.isTraceEnabled()) {
               LOG.trace("Returning '" + this.toString(value) + "' as column " + name);
            }

            return value;
         } else {
            LOG.tracev("Returning null as column {0}", name);
            return null;
         }
      } catch (RuntimeException re) {
         LOG.unableToReadColumnValueFromResultSet(name, re.getMessage());
         throw re;
      } catch (SQLException se) {
         LOG.unableToReadColumnValueFromResultSet(name, se.getMessage());
         throw se;
      }
   }

   public final Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, name);
   }

   public final String toXMLString(Object value, SessionFactoryImplementor pc) throws HibernateException {
      return this.toString(value);
   }

   public final Object fromXMLString(String xml, Mapping factory) throws HibernateException {
      return xml != null && xml.length() != 0 ? this.fromStringValue(xml) : null;
   }

   public final int getColumnSpan(Mapping session) {
      return 1;
   }

   public final int[] sqlTypes(Mapping session) {
      return new int[]{this.sqlType()};
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return new Size[]{this.dictatedSize()};
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return new Size[]{this.defaultSize()};
   }

   public boolean isEqual(Object x, Object y) {
      return EqualsHelper.equals(x, y);
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) {
      return value == null ? "null" : this.toString(value);
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      return this.fromXMLString(xml.getText(), factory);
   }

   public void setToXMLNode(Node xml, Object value, SessionFactoryImplementor factory) throws HibernateException {
      xml.setText(this.toXMLString(value, factory));
   }

   public boolean[] toColumnNullness(Object value, Mapping mapping) {
      return value == null ? ArrayHelper.FALSE : ArrayHelper.TRUE;
   }

   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return checkable[0] && this.isDirty(old, current, session);
   }
}
