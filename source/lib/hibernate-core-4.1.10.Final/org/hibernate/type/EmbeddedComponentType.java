package org.hibernate.type;

import java.lang.reflect.Method;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.tuple.component.ComponentMetamodel;

public class EmbeddedComponentType extends ComponentType {
   public EmbeddedComponentType(TypeFactory.TypeScope typeScope, ComponentMetamodel metamodel) {
      super(typeScope, metamodel);
   }

   public boolean isEmbedded() {
      return true;
   }

   public boolean isMethodOf(Method method) {
      return this.componentTuplizer.isMethodOf(method);
   }

   public Object instantiate(Object parent, SessionImplementor session) throws HibernateException {
      boolean useParent = parent != null && super.getReturnedClass().isInstance(parent);
      return useParent ? parent : super.instantiate(parent, session);
   }
}
