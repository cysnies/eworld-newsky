package org.hibernate.action.internal;

import java.io.Serializable;
import org.hibernate.LockMode;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.internal.NonNullableTransientDependencies;
import org.hibernate.engine.internal.Nullability;
import org.hibernate.engine.internal.Versioning;
import org.hibernate.engine.spi.CachedNaturalIdValueSource;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.persister.entity.EntityPersister;

public abstract class AbstractEntityInsertAction extends EntityAction {
   private transient Object[] state;
   private final boolean isVersionIncrementDisabled;
   private boolean isExecuted;
   private boolean areTransientReferencesNullified;

   protected AbstractEntityInsertAction(Serializable id, Object[] state, Object instance, boolean isVersionIncrementDisabled, EntityPersister persister, SessionImplementor session) {
      super(session, id, instance, persister);
      this.state = state;
      this.isVersionIncrementDisabled = isVersionIncrementDisabled;
      this.isExecuted = false;
      this.areTransientReferencesNullified = false;
      if (id != null) {
         this.handleNaturalIdPreSaveNotifications();
      }

   }

   public Object[] getState() {
      return this.state;
   }

   public abstract boolean isEarlyInsert();

   public NonNullableTransientDependencies findNonNullableTransientEntities() {
      return ForeignKeys.findNonNullableTransientEntities(this.getPersister().getEntityName(), this.getInstance(), this.getState(), this.isEarlyInsert(), this.getSession());
   }

   protected final void nullifyTransientReferencesIfNotAlready() {
      if (!this.areTransientReferencesNullified) {
         (new ForeignKeys.Nullifier(this.getInstance(), false, this.isEarlyInsert(), this.getSession())).nullifyTransientReferences(this.getState(), this.getPersister().getPropertyTypes());
         (new Nullability(this.getSession())).checkNullability(this.getState(), this.getPersister(), false);
         this.areTransientReferencesNullified = true;
      }

   }

   public final void makeEntityManaged() {
      this.nullifyTransientReferencesIfNotAlready();
      Object version = Versioning.getVersion(this.getState(), this.getPersister());
      this.getSession().getPersistenceContext().addEntity(this.getInstance(), this.getPersister().isMutable() ? Status.MANAGED : Status.READ_ONLY, this.getState(), this.getEntityKey(), version, LockMode.WRITE, this.isExecuted, this.getPersister(), this.isVersionIncrementDisabled, false);
   }

   protected void markExecuted() {
      this.isExecuted = true;
   }

   protected abstract EntityKey getEntityKey();

   public void afterDeserialize(SessionImplementor session) {
      super.afterDeserialize(session);
      if (session != null) {
         EntityEntry entityEntry = session.getPersistenceContext().getEntry(this.getInstance());
         this.state = entityEntry.getLoadedState();
      }

   }

   protected void handleNaturalIdPreSaveNotifications() {
      this.getSession().getPersistenceContext().getNaturalIdHelper().manageLocalNaturalIdCrossReference(this.getPersister(), this.getId(), this.state, (Object[])null, CachedNaturalIdValueSource.INSERT);
   }

   public void handleNaturalIdPostSaveNotifications(Serializable generatedId) {
      if (this.isEarlyInsert()) {
         this.getSession().getPersistenceContext().getNaturalIdHelper().manageLocalNaturalIdCrossReference(this.getPersister(), generatedId, this.state, (Object[])null, CachedNaturalIdValueSource.INSERT);
      }

      this.getSession().getPersistenceContext().getNaturalIdHelper().manageSharedNaturalIdCrossReference(this.getPersister(), this.getId(), this.state, (Object[])null, CachedNaturalIdValueSource.INSERT);
   }
}
