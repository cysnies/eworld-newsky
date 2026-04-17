package org.hibernate.event.internal;

import org.hibernate.event.spi.PreLoadEvent;
import org.hibernate.event.spi.PreLoadEventListener;
import org.hibernate.persister.entity.EntityPersister;

public class DefaultPreLoadEventListener implements PreLoadEventListener {
   public void onPreLoad(PreLoadEvent event) {
      EntityPersister persister = event.getPersister();
      event.getSession().getInterceptor().onLoad(event.getEntity(), event.getId(), event.getState(), persister.getPropertyNames(), persister.getPropertyTypes());
   }
}
