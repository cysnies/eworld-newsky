package org.hibernate.type;

import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;

/** @deprecated */
public abstract class ImmutableType extends NullableType {
   public final Object deepCopy(Object value, SessionFactoryImplementor factory) {
      return value;
   }

   public final boolean isMutable() {
      return false;
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      return original;
   }
}
