package org.hibernate.event.spi;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.persister.entity.EntityPersister;

public class ResolveNaturalIdEvent extends AbstractEvent {
   public static final LockMode DEFAULT_LOCK_MODE;
   private final EntityPersister entityPersister;
   private final Map naturalIdValues;
   private final Object[] orderedNaturalIdValues;
   private final LockOptions lockOptions;
   private Serializable entityId;

   public ResolveNaturalIdEvent(Map naturalIdValues, EntityPersister entityPersister, EventSource source) {
      this(naturalIdValues, entityPersister, new LockOptions(), source);
   }

   public ResolveNaturalIdEvent(Map naturalIdValues, EntityPersister entityPersister, LockOptions lockOptions, EventSource source) {
      super(source);
      if (entityPersister == null) {
         throw new IllegalArgumentException("EntityPersister is required for loading");
      } else if (!entityPersister.hasNaturalIdentifier()) {
         throw new HibernateException("Entity did not define a natural-id");
      } else if (naturalIdValues != null && !naturalIdValues.isEmpty()) {
         if (entityPersister.getNaturalIdentifierProperties().length != naturalIdValues.size()) {
            throw new HibernateException(String.format("Entity [%s] defines its natural-id with %d properties but only %d were specified", entityPersister.getEntityName(), entityPersister.getNaturalIdentifierProperties().length, naturalIdValues.size()));
         } else if (lockOptions.getLockMode() == LockMode.WRITE) {
            throw new IllegalArgumentException("Invalid lock mode for loading");
         } else {
            if (lockOptions.getLockMode() == null) {
               lockOptions.setLockMode(DEFAULT_LOCK_MODE);
            }

            this.entityPersister = entityPersister;
            this.naturalIdValues = naturalIdValues;
            this.lockOptions = lockOptions;
            int[] naturalIdPropertyPositions = entityPersister.getNaturalIdentifierProperties();
            this.orderedNaturalIdValues = new Object[naturalIdPropertyPositions.length];
            int i = 0;

            for(int position : naturalIdPropertyPositions) {
               String propertyName = entityPersister.getPropertyNames()[position];
               if (!naturalIdValues.containsKey(propertyName)) {
                  throw new HibernateException(String.format("No value specified for natural-id property %s#%s", this.getEntityName(), propertyName));
               }

               this.orderedNaturalIdValues[i++] = naturalIdValues.get(entityPersister.getPropertyNames()[position]);
            }

         }
      } else {
         throw new IllegalArgumentException("natural-id to load is required");
      }
   }

   public Map getNaturalIdValues() {
      return Collections.unmodifiableMap(this.naturalIdValues);
   }

   public Object[] getOrderedNaturalIdValues() {
      return this.orderedNaturalIdValues;
   }

   public EntityPersister getEntityPersister() {
      return this.entityPersister;
   }

   public String getEntityName() {
      return this.getEntityPersister().getEntityName();
   }

   public LockOptions getLockOptions() {
      return this.lockOptions;
   }

   public Serializable getEntityId() {
      return this.entityId;
   }

   public void setEntityId(Serializable entityId) {
      this.entityId = entityId;
   }

   static {
      DEFAULT_LOCK_MODE = LockMode.NONE;
   }
}
