package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.LockMode;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.event.spi.AbstractEvent;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.TypeHelper;
import org.jboss.logging.Logger;

public class AbstractReassociateEventListener implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractReassociateEventListener.class.getName());

   protected final EntityEntry reassociate(AbstractEvent event, Object object, Serializable id, EntityPersister persister) {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Reassociating transient instance: {0}", MessageHelper.infoString((EntityPersister)persister, (Object)id, (SessionFactoryImplementor)event.getSession().getFactory()));
      }

      EventSource source = event.getSession();
      EntityKey key = source.generateEntityKey(id, persister);
      source.getPersistenceContext().checkUniqueness(key, object);
      Object[] values = persister.getPropertyValues(object);
      TypeHelper.deepCopy(values, persister.getPropertyTypes(), persister.getPropertyUpdateability(), values, source);
      Object version = Versioning.getVersion(values, persister);
      EntityEntry newEntry = source.getPersistenceContext().addEntity(object, persister.isMutable() ? Status.MANAGED : Status.READ_ONLY, values, key, version, LockMode.NONE, true, persister, false, true);
      (new OnLockVisitor(source, id, object)).process(object, persister);
      persister.afterReassociate(object, source);
      return newEntry;
   }
}
