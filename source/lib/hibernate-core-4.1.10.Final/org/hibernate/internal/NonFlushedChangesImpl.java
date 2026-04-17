package org.hibernate.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.hibernate.engine.internal.StatefulPersistenceContext;
import org.hibernate.engine.spi.ActionQueue;
import org.hibernate.engine.spi.NonFlushedChanges;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.spi.EventSource;
import org.jboss.logging.Logger;

public final class NonFlushedChangesImpl implements NonFlushedChanges, Serializable {
   private static final Logger LOG = Logger.getLogger(NonFlushedChangesImpl.class.getName());
   private transient ActionQueue actionQueue;
   private transient StatefulPersistenceContext persistenceContext;

   public NonFlushedChangesImpl(EventSource session) {
      this.actionQueue = session.getActionQueue();
      this.persistenceContext = (StatefulPersistenceContext)session.getPersistenceContext();
   }

   ActionQueue getActionQueue() {
      return this.actionQueue;
   }

   StatefulPersistenceContext getPersistenceContext() {
      return this.persistenceContext;
   }

   public void clear() {
   }

   private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
      LOG.trace("Deserializing NonFlushedChangesImpl");
      ois.defaultReadObject();
      this.persistenceContext = StatefulPersistenceContext.deserialize(ois, (SessionImplementor)null);
      this.actionQueue = ActionQueue.deserialize(ois, (SessionImplementor)null);
   }

   private void writeObject(ObjectOutputStream oos) throws IOException {
      LOG.trace("Serializing NonFlushedChangesImpl");
      oos.defaultWriteObject();
      this.persistenceContext.serialize(oos);
      this.actionQueue.serialize(oos);
   }
}
