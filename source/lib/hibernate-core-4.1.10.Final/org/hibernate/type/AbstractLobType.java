package org.hibernate.type;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metamodel.relational.Size;

/** @deprecated */
@Deprecated
public abstract class AbstractLobType extends AbstractType implements Serializable {
   public boolean isDirty(Object old, Object current, boolean[] checkable, SessionImplementor session) throws HibernateException {
      return checkable[0] ? !this.isEqual(old, current) : false;
   }

   public Size[] dictatedSizes(Mapping mapping) throws MappingException {
      return new Size[]{LEGACY_DICTATED_SIZE};
   }

   public Size[] defaultSizes(Mapping mapping) throws MappingException {
      return new Size[]{LEGACY_DEFAULT_SIZE};
   }

   public boolean isEqual(Object x, Object y) {
      return this.isEqual(x, y, (SessionFactoryImplementor)null);
   }

   public int getHashCode(Object x) {
      return this.getHashCode(x, (SessionFactoryImplementor)null);
   }

   public String getName() {
      return this.getClass().getName();
   }

   public int getColumnSpan(Mapping mapping) throws MappingException {
      return 1;
   }

   protected abstract Object get(ResultSet var1, String var2) throws SQLException;

   public Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.get(rs, names[0]);
   }

   public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.get(rs, name);
   }

   public void nullSafeSet(PreparedStatement st, Object value, int index, boolean[] settable, SessionImplementor session) throws HibernateException, SQLException {
      if (settable[0]) {
         this.set(st, value, index, session);
      }

   }

   protected abstract void set(PreparedStatement var1, Object var2, int var3, SessionImplementor var4) throws SQLException;

   public void nullSafeSet(PreparedStatement st, Object value, int index, SessionImplementor session) throws HibernateException, SQLException {
      this.set(st, value, index, session);
   }
}
