package org.hibernate.event.spi;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.HibernateException;

public class EventType {
   public static final EventType LOAD = new EventType("load", LoadEventListener.class);
   public static final EventType RESOLVE_NATURAL_ID = new EventType("resolve-natural-id", ResolveNaturalIdEventListener.class);
   public static final EventType INIT_COLLECTION = new EventType("load-collection", InitializeCollectionEventListener.class);
   public static final EventType SAVE_UPDATE = new EventType("save-update", SaveOrUpdateEventListener.class);
   public static final EventType UPDATE = new EventType("update", SaveOrUpdateEventListener.class);
   public static final EventType SAVE = new EventType("save", SaveOrUpdateEventListener.class);
   public static final EventType PERSIST = new EventType("create", PersistEventListener.class);
   public static final EventType PERSIST_ONFLUSH = new EventType("create-onflush", PersistEventListener.class);
   public static final EventType MERGE = new EventType("merge", MergeEventListener.class);
   public static final EventType DELETE = new EventType("delete", DeleteEventListener.class);
   public static final EventType REPLICATE = new EventType("replicate", ReplicateEventListener.class);
   public static final EventType FLUSH = new EventType("flush", FlushEventListener.class);
   public static final EventType AUTO_FLUSH = new EventType("auto-flush", AutoFlushEventListener.class);
   public static final EventType DIRTY_CHECK = new EventType("dirty-check", DirtyCheckEventListener.class);
   public static final EventType FLUSH_ENTITY = new EventType("flush-entity", FlushEntityEventListener.class);
   public static final EventType EVICT = new EventType("evict", EvictEventListener.class);
   public static final EventType LOCK = new EventType("lock", LockEventListener.class);
   public static final EventType REFRESH = new EventType("refresh", RefreshEventListener.class);
   public static final EventType PRE_LOAD = new EventType("pre-load", PreLoadEventListener.class);
   public static final EventType PRE_DELETE = new EventType("pre-delete", PreDeleteEventListener.class);
   public static final EventType PRE_UPDATE = new EventType("pre-update", PreUpdateEventListener.class);
   public static final EventType PRE_INSERT = new EventType("pre-insert", PreInsertEventListener.class);
   public static final EventType POST_LOAD = new EventType("post-load", PostLoadEventListener.class);
   public static final EventType POST_DELETE = new EventType("post-delete", PostDeleteEventListener.class);
   public static final EventType POST_UPDATE = new EventType("post-update", PostUpdateEventListener.class);
   public static final EventType POST_INSERT = new EventType("post-insert", PostInsertEventListener.class);
   public static final EventType POST_COMMIT_DELETE = new EventType("post-commit-delete", PostDeleteEventListener.class);
   public static final EventType POST_COMMIT_UPDATE = new EventType("post-commit-update", PostUpdateEventListener.class);
   public static final EventType POST_COMMIT_INSERT = new EventType("post-commit-insert", PostInsertEventListener.class);
   public static final EventType PRE_COLLECTION_RECREATE = new EventType("pre-collection-recreate", PreCollectionRecreateEventListener.class);
   public static final EventType PRE_COLLECTION_REMOVE = new EventType("pre-collection-remove", PreCollectionRemoveEventListener.class);
   public static final EventType PRE_COLLECTION_UPDATE = new EventType("pre-collection-update", PreCollectionUpdateEventListener.class);
   public static final EventType POST_COLLECTION_RECREATE = new EventType("post-collection-recreate", PostCollectionRecreateEventListener.class);
   public static final EventType POST_COLLECTION_REMOVE = new EventType("post-collection-remove", PostCollectionRemoveEventListener.class);
   public static final EventType POST_COLLECTION_UPDATE = new EventType("post-collection-update", PostCollectionUpdateEventListener.class);
   public static final Map eventTypeByNameMap = (Map)AccessController.doPrivileged(new PrivilegedAction() {
      public Map run() {
         Map<String, EventType> typeByNameMap = new HashMap();
         Field[] fields = EventType.class.getDeclaredFields();
         int i = 0;

         for(int max = fields.length; i < max; ++i) {
            if (EventType.class.isAssignableFrom(fields[i].getType())) {
               try {
                  EventType typeField = (EventType)fields[i].get((Object)null);
                  typeByNameMap.put(typeField.eventName(), typeField);
               } catch (Exception t) {
                  throw new HibernateException("Unable to initialize EventType map", t);
               }
            }
         }

         return typeByNameMap;
      }
   });
   private final String eventName;
   private final Class baseListenerInterface;

   public static EventType resolveEventTypeByName(String eventName) {
      if (eventName == null) {
         throw new HibernateException("event name to resolve cannot be null");
      } else {
         EventType eventType = (EventType)eventTypeByNameMap.get(eventName);
         if (eventType == null) {
            throw new HibernateException("Unable to locate proper event type for event name [" + eventName + "]");
         } else {
            return eventType;
         }
      }
   }

   public static Collection values() {
      return eventTypeByNameMap.values();
   }

   private EventType(String eventName, Class baseListenerInterface) {
      this.eventName = eventName;
      this.baseListenerInterface = baseListenerInterface;
   }

   public String eventName() {
      return this.eventName;
   }

   public Class baseListenerInterface() {
      return this.baseListenerInterface;
   }

   public String toString() {
      return this.eventName();
   }
}
