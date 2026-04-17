package org.hibernate.event.internal;

import java.io.Serializable;
import org.hibernate.HibernateException;
import org.hibernate.action.internal.CollectionRemoveAction;
import org.hibernate.event.spi.EventSource;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class ReattachVisitor extends ProxyVisitor {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ReattachVisitor.class.getName());
   private final Serializable ownerIdentifier;
   private final Object owner;

   public ReattachVisitor(EventSource session, Serializable ownerIdentifier, Object owner) {
      super(session);
      this.ownerIdentifier = ownerIdentifier;
      this.owner = owner;
   }

   final Serializable getOwnerIdentifier() {
      return this.ownerIdentifier;
   }

   final Object getOwner() {
      return this.owner;
   }

   Object processComponent(Object component, CompositeType componentType) throws HibernateException {
      Type[] types = componentType.getSubtypes();
      if (component == null) {
         this.processValues(new Object[types.length], types);
      } else {
         super.processComponent(component, componentType);
      }

      return null;
   }

   void removeCollection(CollectionPersister role, Serializable collectionKey, EventSource source) throws HibernateException {
      if (LOG.isTraceEnabled()) {
         LOG.tracev("Collection dereferenced while transient {0}", MessageHelper.collectionInfoString(role, this.ownerIdentifier, source.getFactory()));
      }

      source.getActionQueue().addAction(new CollectionRemoveAction(this.owner, role, collectionKey, false, source));
   }

   final Serializable extractCollectionKeyFromOwner(CollectionPersister role) {
      return role.getCollectionType().useLHSPrimaryKey() ? this.ownerIdentifier : (Serializable)role.getOwnerEntityPersister().getPropertyValue(this.owner, role.getCollectionType().getLHSPropertyName());
   }
}
