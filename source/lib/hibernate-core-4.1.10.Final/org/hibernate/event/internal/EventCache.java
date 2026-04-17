package org.hibernate.event.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.AssertionFailure;

class EventCache implements Map {
   private Map entityToCopyMap = new IdentityHashMap(10);
   private Map copyToEntityMap = new IdentityHashMap(10);
   private Map entityToOperatedOnFlagMap = new IdentityHashMap(10);

   public void clear() {
      this.entityToCopyMap.clear();
      this.copyToEntityMap.clear();
      this.entityToOperatedOnFlagMap.clear();
   }

   public boolean containsKey(Object entity) {
      if (entity == null) {
         throw new NullPointerException("null entities are not supported by " + this.getClass().getName());
      } else {
         return this.entityToCopyMap.containsKey(entity);
      }
   }

   public boolean containsValue(Object copy) {
      if (copy == null) {
         throw new NullPointerException("null copies are not supported by " + this.getClass().getName());
      } else {
         return this.copyToEntityMap.containsKey(copy);
      }
   }

   public Set entrySet() {
      return Collections.unmodifiableSet(this.entityToCopyMap.entrySet());
   }

   public Object get(Object entity) {
      if (entity == null) {
         throw new NullPointerException("null entities are not supported by " + this.getClass().getName());
      } else {
         return this.entityToCopyMap.get(entity);
      }
   }

   public boolean isEmpty() {
      return this.entityToCopyMap.isEmpty();
   }

   public Set keySet() {
      return Collections.unmodifiableSet(this.entityToCopyMap.keySet());
   }

   public Object put(Object entity, Object copy) {
      return this.put(entity, copy, Boolean.FALSE);
   }

   Object put(Object entity, Object copy, boolean isOperatedOn) {
      if (entity != null && copy != null) {
         Object oldCopy = this.entityToCopyMap.put(entity, copy);
         Boolean oldOperatedOn = (Boolean)this.entityToOperatedOnFlagMap.put(entity, isOperatedOn);
         Object oldEntity = this.copyToEntityMap.put(copy, entity);
         if (oldCopy == null) {
            if (oldEntity != null) {
               throw new IllegalStateException("An entity copy was already assigned to a different entity.");
            }

            if (oldOperatedOn != null) {
               throw new IllegalStateException("entityToOperatedOnFlagMap contains an entity, but entityToCopyMap does not.");
            }
         } else {
            if (oldCopy != copy) {
               Object removedEntity = this.copyToEntityMap.remove(oldCopy);
               if (removedEntity != entity) {
                  throw new IllegalStateException("An unexpected entity was associated with the old entity copy.");
               }

               if (oldEntity != null) {
                  throw new IllegalStateException("A new entity copy is already associated with a different entity.");
               }
            } else if (oldEntity != entity) {
               throw new IllegalStateException("An entity copy was associated with a different entity than provided.");
            }

            if (oldOperatedOn == null) {
               throw new IllegalStateException("entityToCopyMap contained an entity, but entityToOperatedOnFlagMap did not.");
            }
         }

         return oldCopy;
      } else {
         throw new NullPointerException("null entities and copies are not supported by " + this.getClass().getName());
      }
   }

   public void putAll(Map map) {
      for(Object o : map.entrySet()) {
         Map.Entry entry = (Map.Entry)o;
         this.put(entry.getKey(), entry.getValue());
      }

   }

   public Object remove(Object entity) {
      if (entity == null) {
         throw new NullPointerException("null entities are not supported by " + this.getClass().getName());
      } else {
         Boolean oldOperatedOn = (Boolean)this.entityToOperatedOnFlagMap.remove(entity);
         Object oldCopy = this.entityToCopyMap.remove(entity);
         Object oldEntity = oldCopy != null ? this.copyToEntityMap.remove(oldCopy) : null;
         if (oldCopy == null) {
            if (oldOperatedOn != null) {
               throw new IllegalStateException("Removed entity from entityToOperatedOnFlagMap, but entityToCopyMap did not contain the entity.");
            }
         } else {
            if (oldEntity == null) {
               throw new IllegalStateException("Removed entity from entityToCopyMap, but copyToEntityMap did not contain the entity.");
            }

            if (oldOperatedOn == null) {
               throw new IllegalStateException("entityToCopyMap contained an entity, but entityToOperatedOnFlagMap did not.");
            }

            if (oldEntity != entity) {
               throw new IllegalStateException("An entity copy was associated with a different entity than provided.");
            }
         }

         return oldCopy;
      }
   }

   public int size() {
      return this.entityToCopyMap.size();
   }

   public Collection values() {
      return Collections.unmodifiableCollection(this.entityToCopyMap.values());
   }

   public boolean isOperatedOn(Object entity) {
      if (entity == null) {
         throw new NullPointerException("null entities are not supported by " + this.getClass().getName());
      } else {
         return (Boolean)this.entityToOperatedOnFlagMap.get(entity);
      }
   }

   void setOperatedOn(Object entity, boolean isOperatedOn) {
      if (entity == null) {
         throw new NullPointerException("null entities are not supported by " + this.getClass().getName());
      } else if (this.entityToOperatedOnFlagMap.containsKey(entity) && this.entityToCopyMap.containsKey(entity)) {
         this.entityToOperatedOnFlagMap.put(entity, isOperatedOn);
      } else {
         throw new AssertionFailure("called EventCache.setOperatedOn() for entity not found in EventCache");
      }
   }

   public Map invertMap() {
      return Collections.unmodifiableMap(this.copyToEntityMap);
   }
}
