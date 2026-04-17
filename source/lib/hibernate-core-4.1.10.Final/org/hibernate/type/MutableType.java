package org.hibernate.type;

import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

/** @deprecated */
public abstract class MutableType extends NullableType {
   public final boolean isMutable() {
      return true;
   }

   protected abstract Object deepCopyNotNull(Object var1) throws HibernateException;

   public final Object deepCopy(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return value == null ? null : this.deepCopyNotNull(value);
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      return this.isEqual(original, target) ? original : this.deepCopy(original, session.getFactory());
   }
}
