package org.hibernate.event.service.internal;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.event.internal.DefaultAutoFlushEventListener;
import org.hibernate.event.internal.DefaultDeleteEventListener;
import org.hibernate.event.internal.DefaultDirtyCheckEventListener;
import org.hibernate.event.internal.DefaultEvictEventListener;
import org.hibernate.event.internal.DefaultFlushEntityEventListener;
import org.hibernate.event.internal.DefaultFlushEventListener;
import org.hibernate.event.internal.DefaultInitializeCollectionEventListener;
import org.hibernate.event.internal.DefaultLoadEventListener;
import org.hibernate.event.internal.DefaultLockEventListener;
import org.hibernate.event.internal.DefaultMergeEventListener;
import org.hibernate.event.internal.DefaultPersistEventListener;
import org.hibernate.event.internal.DefaultPersistOnFlushEventListener;
import org.hibernate.event.internal.DefaultPostLoadEventListener;
import org.hibernate.event.internal.DefaultPreLoadEventListener;
import org.hibernate.event.internal.DefaultRefreshEventListener;
import org.hibernate.event.internal.DefaultReplicateEventListener;
import org.hibernate.event.internal.DefaultResolveNaturalIdEventListener;
import org.hibernate.event.internal.DefaultSaveEventListener;
import org.hibernate.event.internal.DefaultSaveOrUpdateEventListener;
import org.hibernate.event.internal.DefaultUpdateEventListener;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerRegistrationException;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;

public class EventListenerRegistryImpl implements EventListenerRegistry {
   private Map listenerClassToInstanceMap = new HashMap();
   private Map registeredEventListenersMap = prepareListenerMap();

   public EventListenerGroupImpl getEventListenerGroup(EventType eventType) {
      EventListenerGroupImpl<T> listeners = (EventListenerGroupImpl)this.registeredEventListenersMap.get(eventType);
      if (listeners == null) {
         throw new HibernateException("Unable to find listeners for type [" + eventType.eventName() + "]");
      } else {
         return listeners;
      }
   }

   public void addDuplicationStrategy(DuplicationStrategy strategy) {
      for(EventListenerGroupImpl group : this.registeredEventListenersMap.values()) {
         group.addDuplicationStrategy(strategy);
      }

   }

   public void setListeners(EventType type, Class... listenerClasses) {
      this.setListeners(type, this.resolveListenerInstances(type, listenerClasses));
   }

   private Object[] resolveListenerInstances(EventType type, Class... listenerClasses) {
      T[] listeners = (T[])((Object[])((Object[])Array.newInstance(type.baseListenerInterface(), listenerClasses.length)));

      for(int i = 0; i < listenerClasses.length; ++i) {
         listeners[i] = this.resolveListenerInstance(listenerClasses[i]);
      }

      return listeners;
   }

   private Object resolveListenerInstance(Class listenerClass) {
      T listenerInstance = (T)this.listenerClassToInstanceMap.get(listenerClass);
      if (listenerInstance == null) {
         listenerInstance = (T)this.instantiateListener(listenerClass);
         this.listenerClassToInstanceMap.put(listenerClass, listenerInstance);
      }

      return listenerInstance;
   }

   private Object instantiateListener(Class listenerClass) {
      try {
         return listenerClass.newInstance();
      } catch (Exception e) {
         throw new EventListenerRegistrationException("Unable to instantiate specified event listener class: " + listenerClass.getName(), e);
      }
   }

   public void setListeners(EventType type, Object... listeners) {
      EventListenerGroupImpl<T> registeredListeners = this.getEventListenerGroup(type);
      registeredListeners.clear();
      if (listeners != null) {
         int i = 0;

         for(int max = listeners.length; i < max; ++i) {
            registeredListeners.appendListener(listeners[i]);
         }
      }

   }

   public void appendListeners(EventType type, Class... listenerClasses) {
      this.appendListeners(type, this.resolveListenerInstances(type, listenerClasses));
   }

   public void appendListeners(EventType type, Object... listeners) {
      this.getEventListenerGroup(type).appendListeners(listeners);
   }

   public void prependListeners(EventType type, Class... listenerClasses) {
      this.prependListeners(type, this.resolveListenerInstances(type, listenerClasses));
   }

   public void prependListeners(EventType type, Object... listeners) {
      this.getEventListenerGroup(type).prependListeners(listeners);
   }

   private static Map prepareListenerMap() {
      Map<EventType, EventListenerGroupImpl> workMap = new HashMap();
      prepareListeners(EventType.AUTO_FLUSH, new DefaultAutoFlushEventListener(), workMap);
      prepareListeners(EventType.PERSIST, new DefaultPersistEventListener(), workMap);
      prepareListeners(EventType.PERSIST_ONFLUSH, new DefaultPersistOnFlushEventListener(), workMap);
      prepareListeners(EventType.DELETE, new DefaultDeleteEventListener(), workMap);
      prepareListeners(EventType.DIRTY_CHECK, new DefaultDirtyCheckEventListener(), workMap);
      prepareListeners(EventType.EVICT, new DefaultEvictEventListener(), workMap);
      prepareListeners(EventType.FLUSH, new DefaultFlushEventListener(), workMap);
      prepareListeners(EventType.FLUSH_ENTITY, new DefaultFlushEntityEventListener(), workMap);
      prepareListeners(EventType.LOAD, new DefaultLoadEventListener(), workMap);
      prepareListeners(EventType.RESOLVE_NATURAL_ID, new DefaultResolveNaturalIdEventListener(), workMap);
      prepareListeners(EventType.INIT_COLLECTION, new DefaultInitializeCollectionEventListener(), workMap);
      prepareListeners(EventType.LOCK, new DefaultLockEventListener(), workMap);
      prepareListeners(EventType.MERGE, new DefaultMergeEventListener(), workMap);
      prepareListeners(EventType.PRE_COLLECTION_RECREATE, workMap);
      prepareListeners(EventType.PRE_COLLECTION_REMOVE, workMap);
      prepareListeners(EventType.PRE_COLLECTION_UPDATE, workMap);
      prepareListeners(EventType.PRE_DELETE, workMap);
      prepareListeners(EventType.PRE_INSERT, workMap);
      prepareListeners(EventType.PRE_LOAD, new DefaultPreLoadEventListener(), workMap);
      prepareListeners(EventType.PRE_UPDATE, workMap);
      prepareListeners(EventType.POST_COLLECTION_RECREATE, workMap);
      prepareListeners(EventType.POST_COLLECTION_REMOVE, workMap);
      prepareListeners(EventType.POST_COLLECTION_UPDATE, workMap);
      prepareListeners(EventType.POST_COMMIT_DELETE, workMap);
      prepareListeners(EventType.POST_COMMIT_INSERT, workMap);
      prepareListeners(EventType.POST_COMMIT_UPDATE, workMap);
      prepareListeners(EventType.POST_DELETE, workMap);
      prepareListeners(EventType.POST_INSERT, workMap);
      prepareListeners(EventType.POST_LOAD, new DefaultPostLoadEventListener(), workMap);
      prepareListeners(EventType.POST_UPDATE, workMap);
      prepareListeners(EventType.UPDATE, new DefaultUpdateEventListener(), workMap);
      prepareListeners(EventType.REFRESH, new DefaultRefreshEventListener(), workMap);
      prepareListeners(EventType.REPLICATE, new DefaultReplicateEventListener(), workMap);
      prepareListeners(EventType.SAVE, new DefaultSaveEventListener(), workMap);
      prepareListeners(EventType.SAVE_UPDATE, new DefaultSaveOrUpdateEventListener(), workMap);
      return Collections.unmodifiableMap(workMap);
   }

   private static void prepareListeners(EventType type, Map map) {
      prepareListeners(type, (Object)null, map);
   }

   private static void prepareListeners(EventType type, Object defaultListener, Map map) {
      EventListenerGroupImpl<T> listeners = new EventListenerGroupImpl(type);
      if (defaultListener != null) {
         listeners.appendListener(defaultListener);
      }

      map.put(type, listeners);
   }
}
