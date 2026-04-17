package org.hibernate.proxy.dom4j;

import java.io.Serializable;
import org.dom4j.Element;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.AbstractLazyInitializer;

public class Dom4jLazyInitializer extends AbstractLazyInitializer implements Serializable {
   Dom4jLazyInitializer(String entityName, Serializable id, SessionImplementor session) {
      super(entityName, id, session);
   }

   public Element getElement() {
      return (Element)this.getImplementation();
   }

   public Class getPersistentClass() {
      throw new UnsupportedOperationException("dom4j entity representation");
   }
}
