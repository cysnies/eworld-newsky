package org.hibernate.collection.internal;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.naming.NamingException;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.LazyInitializationException;
import org.hibernate.Session;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.CollectionEntry;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.SessionFactoryRegistry;
import org.hibernate.internal.util.MarkerObject;
import org.hibernate.internal.util.collections.EmptyIterator;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class AbstractPersistentCollection implements Serializable, PersistentCollection {
   private static final Logger log = Logger.getLogger(AbstractPersistentCollection.class);
   private transient SessionImplementor session;
   private boolean initialized;
   private transient List operationQueue;
   private transient boolean directlyAccessible;
   private transient boolean initializing;
   private Object owner;
   private int cachedSize = -1;
   private String role;
   private Serializable key;
   private boolean dirty;
   private Serializable storedSnapshot;
   private String sessionFactoryUuid;
   private boolean specjLazyLoad = false;
   protected static final Object UNKNOWN = new MarkerObject("UNKNOWN");

   public final String getRole() {
      return this.role;
   }

   public final Serializable getKey() {
      return this.key;
   }

   public final boolean isUnreferenced() {
      return this.role == null;
   }

   public final boolean isDirty() {
      return this.dirty;
   }

   public final void clearDirty() {
      this.dirty = false;
   }

   public final void dirty() {
      this.dirty = true;
   }

   public final Serializable getStoredSnapshot() {
      return this.storedSnapshot;
   }

   public abstract boolean empty();

   protected final void read() {
      this.initialize(false);
   }

   protected boolean readSize() {
      if (!this.initialized) {
         if (this.cachedSize != -1 && !this.hasQueuedOperations()) {
            return true;
         }

         boolean isExtraLazy = (Boolean)this.withTemporarySessionIfNeeded(new LazyInitializationWork() {
            public Boolean doWork() {
               CollectionEntry entry = AbstractPersistentCollection.this.session.getPersistenceContext().getCollectionEntry(AbstractPersistentCollection.this);
               if (entry != null) {
                  CollectionPersister persister = entry.getLoadedPersister();
                  if (persister.isExtraLazy()) {
                     if (AbstractPersistentCollection.this.hasQueuedOperations()) {
                        AbstractPersistentCollection.this.session.flush();
                     }

                     AbstractPersistentCollection.this.cachedSize = persister.getSize(entry.getLoadedKey(), AbstractPersistentCollection.this.session);
                     return true;
                  }

                  AbstractPersistentCollection.this.read();
               } else {
                  AbstractPersistentCollection.this.throwLazyInitializationExceptionIfNotConnected();
               }

               return false;
            }
         });
         if (isExtraLazy) {
            return true;
         }
      }

      return false;
   }

   private Object withTemporarySessionIfNeeded(LazyInitializationWork lazyInitializationWork) {
      SessionImplementor originalSession = null;
      boolean isTempSession = false;
      boolean isJTA = false;
      if (this.session == null) {
         if (this.specjLazyLoad) {
            this.session = this.openTemporarySessionForLoading();
            isTempSession = true;
         } else {
            this.throwLazyInitializationException("could not initialize proxy - no Session");
         }
      } else if (!this.session.isOpen()) {
         if (this.specjLazyLoad) {
            originalSession = this.session;
            this.session = this.openTemporarySessionForLoading();
            isTempSession = true;
         } else {
            this.throwLazyInitializationException("could not initialize proxy - the owning Session was closed");
         }
      } else if (!this.session.isConnected()) {
         if (this.specjLazyLoad) {
            originalSession = this.session;
            this.session = this.openTemporarySessionForLoading();
            isTempSession = true;
         } else {
            this.throwLazyInitializationException("could not initialize proxy - the owning Session is disconnected");
         }
      }

      if (isTempSession) {
         isJTA = this.session.getTransactionCoordinator().getTransactionContext().getTransactionEnvironment().getTransactionFactory().compatibleWithJtaSynchronization();
         if (!isJTA) {
            ((Session)this.session).beginTransaction();
         }

         this.session.getPersistenceContext().addUninitializedDetachedCollection(this.session.getFactory().getCollectionPersister(this.getRole()), this);
      }

      Object var5;
      try {
         var5 = lazyInitializationWork.doWork();
      } finally {
         if (isTempSession) {
            try {
               if (!isJTA) {
                  ((Session)this.session).getTransaction().commit();
               }

               ((Session)this.session).close();
            } catch (Exception var12) {
               log.warn("Unable to close temporary session used to load lazy collection associated to no session");
            }

            this.session = originalSession;
         }

      }

      return var5;
   }

   private SessionImplementor openTemporarySessionForLoading() {
      if (this.sessionFactoryUuid == null) {
         this.throwLazyInitializationException("SessionFactory UUID not known to create temporary Session for loading");
      }

      SessionFactoryImplementor sf = (SessionFactoryImplementor)SessionFactoryRegistry.INSTANCE.getSessionFactory(this.sessionFactoryUuid);
      return (SessionImplementor)sf.openSession();
   }

   protected Boolean readIndexExistence(final Object index) {
      if (!this.initialized) {
         Boolean extraLazyExistenceCheck = (Boolean)this.withTemporarySessionIfNeeded(new LazyInitializationWork() {
            public Boolean doWork() {
               CollectionEntry entry = AbstractPersistentCollection.this.session.getPersistenceContext().getCollectionEntry(AbstractPersistentCollection.this);
               CollectionPersister persister = entry.getLoadedPersister();
               if (persister.isExtraLazy()) {
                  if (AbstractPersistentCollection.this.hasQueuedOperations()) {
                     AbstractPersistentCollection.this.session.flush();
                  }

                  return persister.indexExists(entry.getLoadedKey(), index, AbstractPersistentCollection.this.session);
               } else {
                  AbstractPersistentCollection.this.read();
                  return null;
               }
            }
         });
         if (extraLazyExistenceCheck != null) {
            return extraLazyExistenceCheck;
         }
      }

      return null;
   }

   protected Boolean readElementExistence(final Object element) {
      if (!this.initialized) {
         Boolean extraLazyExistenceCheck = (Boolean)this.withTemporarySessionIfNeeded(new LazyInitializationWork() {
            public Boolean doWork() {
               CollectionEntry entry = AbstractPersistentCollection.this.session.getPersistenceContext().getCollectionEntry(AbstractPersistentCollection.this);
               CollectionPersister persister = entry.getLoadedPersister();
               if (persister.isExtraLazy()) {
                  if (AbstractPersistentCollection.this.hasQueuedOperations()) {
                     AbstractPersistentCollection.this.session.flush();
                  }

                  return persister.elementExists(entry.getLoadedKey(), element, AbstractPersistentCollection.this.session);
               } else {
                  AbstractPersistentCollection.this.read();
                  return null;
               }
            }
         });
         if (extraLazyExistenceCheck != null) {
            return extraLazyExistenceCheck;
         }
      }

      return null;
   }

   protected Object readElementByIndex(final Object index) {
      if (!this.initialized) {
         class ExtraLazyElementByIndexReader implements LazyInitializationWork {
            private boolean isExtraLazy;
            private Object element;

            public Object doWork() {
               CollectionEntry entry = AbstractPersistentCollection.this.session.getPersistenceContext().getCollectionEntry(AbstractPersistentCollection.this);
               CollectionPersister persister = entry.getLoadedPersister();
               this.isExtraLazy = persister.isExtraLazy();
               if (this.isExtraLazy) {
                  if (AbstractPersistentCollection.this.hasQueuedOperations()) {
                     AbstractPersistentCollection.this.session.flush();
                  }

                  this.element = persister.getElementByIndex(entry.getLoadedKey(), index, AbstractPersistentCollection.this.session, AbstractPersistentCollection.this.owner);
               } else {
                  AbstractPersistentCollection.this.read();
               }

               return null;
            }
         }

         ExtraLazyElementByIndexReader reader = new ExtraLazyElementByIndexReader();
         this.withTemporarySessionIfNeeded(reader);
         if (reader.isExtraLazy) {
            return reader.element;
         }
      }

      return UNKNOWN;
   }

   protected int getCachedSize() {
      return this.cachedSize;
   }

   private boolean isConnectedToSession() {
      return this.session != null && this.session.isOpen() && this.session.getPersistenceContext().containsCollection(this);
   }

   protected final void write() {
      this.initialize(true);
      this.dirty();
   }

   protected boolean isOperationQueueEnabled() {
      return !this.initialized && this.isConnectedToSession() && this.isInverseCollection();
   }

   protected boolean isPutQueueEnabled() {
      return !this.initialized && this.isConnectedToSession() && this.isInverseOneToManyOrNoOrphanDelete();
   }

   protected boolean isClearQueueEnabled() {
      return !this.initialized && this.isConnectedToSession() && this.isInverseCollectionNoOrphanDelete();
   }

   private boolean isInverseCollection() {
      CollectionEntry ce = this.session.getPersistenceContext().getCollectionEntry(this);
      return ce != null && ce.getLoadedPersister().isInverse();
   }

   private boolean isInverseCollectionNoOrphanDelete() {
      CollectionEntry ce = this.session.getPersistenceContext().getCollectionEntry(this);
      return ce != null && ce.getLoadedPersister().isInverse() && !ce.getLoadedPersister().hasOrphanDelete();
   }

   private boolean isInverseOneToManyOrNoOrphanDelete() {
      CollectionEntry ce = this.session.getPersistenceContext().getCollectionEntry(this);
      return ce != null && ce.getLoadedPersister().isInverse() && (ce.getLoadedPersister().isOneToMany() || !ce.getLoadedPersister().hasOrphanDelete());
   }

   protected final void queueOperation(DelayedOperation operation) {
      if (this.operationQueue == null) {
         this.operationQueue = new ArrayList(10);
      }

      this.operationQueue.add(operation);
      this.dirty = true;
   }

   protected final void performQueuedOperations() {
      for(DelayedOperation operation : this.operationQueue) {
         operation.operate();
      }

   }

   public void setSnapshot(Serializable key, String role, Serializable snapshot) {
      this.key = key;
      this.role = role;
      this.storedSnapshot = snapshot;
   }

   public void postAction() {
      this.operationQueue = null;
      this.cachedSize = -1;
      this.clearDirty();
   }

   public AbstractPersistentCollection() {
   }

   protected AbstractPersistentCollection(SessionImplementor session) {
      this.session = session;
   }

   public Object getValue() {
      return this;
   }

   public void beginRead() {
      this.initializing = true;
   }

   public boolean endRead() {
      return this.afterInitialize();
   }

   public boolean afterInitialize() {
      this.setInitialized();
      if (this.operationQueue != null) {
         this.performQueuedOperations();
         this.operationQueue = null;
         this.cachedSize = -1;
         return false;
      } else {
         return true;
      }
   }

   protected final void initialize(final boolean writing) {
      if (!this.initialized) {
         this.withTemporarySessionIfNeeded(new LazyInitializationWork() {
            public Object doWork() {
               AbstractPersistentCollection.this.session.initializeCollection(AbstractPersistentCollection.this, writing);
               return null;
            }
         });
      }
   }

   private void throwLazyInitializationExceptionIfNotConnected() {
      if (!this.isConnectedToSession()) {
         this.throwLazyInitializationException("no session or session was closed");
      }

      if (!this.session.isConnected()) {
         this.throwLazyInitializationException("session is disconnected");
      }

   }

   private void throwLazyInitializationException(String message) {
      throw new LazyInitializationException("failed to lazily initialize a collection" + (this.role == null ? "" : " of role: " + this.role) + ", " + message);
   }

   protected final void setInitialized() {
      this.initializing = false;
      this.initialized = true;
   }

   protected final void setDirectlyAccessible(boolean directlyAccessible) {
      this.directlyAccessible = directlyAccessible;
   }

   public boolean isDirectlyAccessible() {
      return this.directlyAccessible;
   }

   public final boolean unsetSession(SessionImplementor currentSession) {
      this.prepareForPossibleSpecialSpecjInitialization();
      if (currentSession == this.session) {
         this.session = null;
         return true;
      } else {
         return false;
      }
   }

   protected void prepareForPossibleSpecialSpecjInitialization() {
      if (this.session != null) {
         this.specjLazyLoad = this.session.getFactory().getSettings().isInitializeLazyStateOutsideTransactionsEnabled();
         if (this.specjLazyLoad && this.sessionFactoryUuid == null) {
            try {
               this.sessionFactoryUuid = (String)this.session.getFactory().getReference().get("uuid").getContent();
            } catch (NamingException var2) {
            }
         }
      }

   }

   public final boolean setCurrentSession(SessionImplementor session) throws HibernateException {
      if (session == this.session) {
         return false;
      } else if (this.isConnectedToSession()) {
         CollectionEntry ce = session.getPersistenceContext().getCollectionEntry(this);
         if (ce == null) {
            throw new HibernateException("Illegal attempt to associate a collection with two open sessions");
         } else {
            throw new HibernateException("Illegal attempt to associate a collection with two open sessions: " + MessageHelper.collectionInfoString(ce.getLoadedPersister(), this, ce.getLoadedKey(), session));
         }
      } else {
         this.session = session;
         return true;
      }
   }

   public boolean needsRecreate(CollectionPersister persister) {
      return false;
   }

   public final void forceInitialization() throws HibernateException {
      if (!this.initialized) {
         if (this.initializing) {
            throw new AssertionFailure("force initialize loading collection");
         }

         if (this.session == null) {
            throw new HibernateException("collection is not associated with any session");
         }

         if (!this.session.isConnected()) {
            throw new HibernateException("disconnected session");
         }

         this.session.initializeCollection(this, false);
      }

   }

   protected final Serializable getSnapshot() {
      return this.session.getPersistenceContext().getSnapshot(this);
   }

   public final boolean wasInitialized() {
      return this.initialized;
   }

   public boolean isRowUpdatePossible() {
      return true;
   }

   public final boolean hasQueuedOperations() {
      return this.operationQueue != null;
   }

   public final Iterator queuedAdditionIterator() {
      return this.hasQueuedOperations() ? new Iterator() {
         int i = 0;

         public Object next() {
            return ((DelayedOperation)AbstractPersistentCollection.this.operationQueue.get(this.i++)).getAddedInstance();
         }

         public boolean hasNext() {
            return this.i < AbstractPersistentCollection.this.operationQueue.size();
         }

         public void remove() {
            throw new UnsupportedOperationException();
         }
      } : EmptyIterator.INSTANCE;
   }

   public final Collection getQueuedOrphans(String entityName) {
      if (!this.hasQueuedOperations()) {
         return Collections.EMPTY_LIST;
      } else {
         Collection additions = new ArrayList(this.operationQueue.size());
         Collection removals = new ArrayList(this.operationQueue.size());

         for(DelayedOperation operation : this.operationQueue) {
            additions.add(operation.getAddedInstance());
            removals.add(operation.getOrphan());
         }

         return getOrphans(removals, additions, entityName, this.session);
      }
   }

   public void preInsert(CollectionPersister persister) throws HibernateException {
   }

   public void afterRowInsert(CollectionPersister persister, Object entry, int i) throws HibernateException {
   }

   public abstract Collection getOrphans(Serializable var1, String var2) throws HibernateException;

   public final SessionImplementor getSession() {
      return this.session;
   }

   protected static Collection getOrphans(Collection oldElements, Collection currentElements, String entityName, SessionImplementor session) throws HibernateException {
      if (currentElements.size() == 0) {
         return oldElements;
      } else if (oldElements.size() == 0) {
         return oldElements;
      } else {
         EntityPersister entityPersister = session.getFactory().getEntityPersister(entityName);
         Type idType = entityPersister.getIdentifierType();
         Collection res = new ArrayList();
         Set currentIds = new HashSet();
         Set currentSaving = new IdentitySet();

         for(Object current : currentElements) {
            if (current != null && ForeignKeys.isNotTransient(entityName, current, (Boolean)null, session)) {
               EntityEntry ee = session.getPersistenceContext().getEntry(current);
               if (ee != null && ee.getStatus() == Status.SAVING) {
                  currentSaving.add(current);
               } else {
                  Serializable currentId = ForeignKeys.getEntityIdentifierIfNotUnsaved(entityName, current, session);
                  currentIds.add(new TypedValue(idType, currentId, entityPersister.getEntityMode()));
               }
            }
         }

         for(Object old : oldElements) {
            if (!currentSaving.contains(old)) {
               Serializable oldId = ForeignKeys.getEntityIdentifierIfNotUnsaved(entityName, old, session);
               if (!currentIds.contains(new TypedValue(idType, oldId, entityPersister.getEntityMode()))) {
                  res.add(old);
               }
            }
         }

         return res;
      }
   }

   public static void identityRemove(Collection list, Object object, String entityName, SessionImplementor session) throws HibernateException {
      if (object != null && ForeignKeys.isNotTransient(entityName, object, (Boolean)null, session)) {
         EntityPersister entityPersister = session.getFactory().getEntityPersister(entityName);
         Type idType = entityPersister.getIdentifierType();
         Serializable idOfCurrent = ForeignKeys.getEntityIdentifierIfNotUnsaved(entityName, object, session);
         Iterator itr = list.iterator();

         while(itr.hasNext()) {
            Serializable idOfOld = ForeignKeys.getEntityIdentifierIfNotUnsaved(entityName, itr.next(), session);
            if (idType.isEqual(idOfCurrent, idOfOld, session.getFactory())) {
               itr.remove();
               break;
            }
         }
      }

   }

   public Object getIdentifier(Object entry, int i) {
      throw new UnsupportedOperationException();
   }

   public Object getOwner() {
      return this.owner;
   }

   public void setOwner(Object owner) {
      this.owner = owner;
   }

   protected final class IteratorProxy implements Iterator {
      protected final Iterator itr;

      public IteratorProxy(Iterator itr) {
         this.itr = itr;
      }

      public boolean hasNext() {
         return this.itr.hasNext();
      }

      public Object next() {
         return this.itr.next();
      }

      public void remove() {
         AbstractPersistentCollection.this.write();
         this.itr.remove();
      }
   }

   protected final class ListIteratorProxy implements ListIterator {
      protected final ListIterator itr;

      public ListIteratorProxy(ListIterator itr) {
         this.itr = itr;
      }

      public void add(Object o) {
         AbstractPersistentCollection.this.write();
         this.itr.add(o);
      }

      public boolean hasNext() {
         return this.itr.hasNext();
      }

      public boolean hasPrevious() {
         return this.itr.hasPrevious();
      }

      public Object next() {
         return this.itr.next();
      }

      public int nextIndex() {
         return this.itr.nextIndex();
      }

      public Object previous() {
         return this.itr.previous();
      }

      public int previousIndex() {
         return this.itr.previousIndex();
      }

      public void remove() {
         AbstractPersistentCollection.this.write();
         this.itr.remove();
      }

      public void set(Object o) {
         AbstractPersistentCollection.this.write();
         this.itr.set(o);
      }
   }

   protected class SetProxy implements Set {
      protected final Collection set;

      public SetProxy(Collection set) {
         this.set = set;
      }

      public boolean add(Object o) {
         AbstractPersistentCollection.this.write();
         return this.set.add(o);
      }

      public boolean addAll(Collection c) {
         AbstractPersistentCollection.this.write();
         return this.set.addAll(c);
      }

      public void clear() {
         AbstractPersistentCollection.this.write();
         this.set.clear();
      }

      public boolean contains(Object o) {
         return this.set.contains(o);
      }

      public boolean containsAll(Collection c) {
         return this.set.containsAll(c);
      }

      public boolean isEmpty() {
         return this.set.isEmpty();
      }

      public Iterator iterator() {
         return AbstractPersistentCollection.this.new IteratorProxy(this.set.iterator());
      }

      public boolean remove(Object o) {
         AbstractPersistentCollection.this.write();
         return this.set.remove(o);
      }

      public boolean removeAll(Collection c) {
         AbstractPersistentCollection.this.write();
         return this.set.removeAll(c);
      }

      public boolean retainAll(Collection c) {
         AbstractPersistentCollection.this.write();
         return this.set.retainAll(c);
      }

      public int size() {
         return this.set.size();
      }

      public Object[] toArray() {
         return this.set.toArray();
      }

      public Object[] toArray(Object[] array) {
         return this.set.toArray(array);
      }
   }

   protected final class ListProxy implements List {
      protected final List list;

      public ListProxy(List list) {
         this.list = list;
      }

      public void add(int index, Object value) {
         AbstractPersistentCollection.this.write();
         this.list.add(index, value);
      }

      public boolean add(Object o) {
         AbstractPersistentCollection.this.write();
         return this.list.add(o);
      }

      public boolean addAll(Collection c) {
         AbstractPersistentCollection.this.write();
         return this.list.addAll(c);
      }

      public boolean addAll(int i, Collection c) {
         AbstractPersistentCollection.this.write();
         return this.list.addAll(i, c);
      }

      public void clear() {
         AbstractPersistentCollection.this.write();
         this.list.clear();
      }

      public boolean contains(Object o) {
         return this.list.contains(o);
      }

      public boolean containsAll(Collection c) {
         return this.list.containsAll(c);
      }

      public Object get(int i) {
         return this.list.get(i);
      }

      public int indexOf(Object o) {
         return this.list.indexOf(o);
      }

      public boolean isEmpty() {
         return this.list.isEmpty();
      }

      public Iterator iterator() {
         return AbstractPersistentCollection.this.new IteratorProxy(this.list.iterator());
      }

      public int lastIndexOf(Object o) {
         return this.list.lastIndexOf(o);
      }

      public ListIterator listIterator() {
         return AbstractPersistentCollection.this.new ListIteratorProxy(this.list.listIterator());
      }

      public ListIterator listIterator(int i) {
         return AbstractPersistentCollection.this.new ListIteratorProxy(this.list.listIterator(i));
      }

      public Object remove(int i) {
         AbstractPersistentCollection.this.write();
         return this.list.remove(i);
      }

      public boolean remove(Object o) {
         AbstractPersistentCollection.this.write();
         return this.list.remove(o);
      }

      public boolean removeAll(Collection c) {
         AbstractPersistentCollection.this.write();
         return this.list.removeAll(c);
      }

      public boolean retainAll(Collection c) {
         AbstractPersistentCollection.this.write();
         return this.list.retainAll(c);
      }

      public Object set(int i, Object o) {
         AbstractPersistentCollection.this.write();
         return this.list.set(i, o);
      }

      public int size() {
         return this.list.size();
      }

      public List subList(int i, int j) {
         return this.list.subList(i, j);
      }

      public Object[] toArray() {
         return this.list.toArray();
      }

      public Object[] toArray(Object[] array) {
         return this.list.toArray(array);
      }
   }

   protected interface DelayedOperation {
      void operate();

      Object getAddedInstance();

      Object getOrphan();
   }

   public interface LazyInitializationWork {
      Object doWork();
   }
}
