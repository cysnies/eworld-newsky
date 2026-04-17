package org.hibernate.proxy.map;

import java.io.Serializable;
import java.util.Map;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.proxy.AbstractLazyInitializer;

public class MapLazyInitializer extends AbstractLazyInitializer implements Serializable {
   MapLazyInitializer(String entityName, Serializable id, SessionImplementor session) {
      super(entityName, id, session);
   }

   public Map getMap() {
      return (Map)this.getImplementation();
   }

   public Class getPersistentClass() {
      throw new UnsupportedOperationException("dynamic-map entity representation");
   }
}
