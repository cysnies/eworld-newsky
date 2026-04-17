package org.hibernate.tuple.component;

import java.io.Serializable;
import java.lang.reflect.Method;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.tuple.Tuplizer;

public interface ComponentTuplizer extends Tuplizer, Serializable {
   Object getParent(Object var1);

   void setParent(Object var1, Object var2, SessionFactoryImplementor var3);

   boolean hasParentProperty();

   boolean isMethodOf(Method var1);
}
