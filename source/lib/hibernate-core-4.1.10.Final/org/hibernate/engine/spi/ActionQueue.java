package org.hibernate.engine.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.hibernate.action.internal.AbstractEntityInsertAction;
import org.hibernate.action.internal.BulkOperationCleanupAction;
import org.hibernate.action.internal.CollectionAction;
import org.hibernate.action.internal.CollectionRecreateAction;
import org.hibernate.action.internal.CollectionRemoveAction;
import org.hibernate.action.internal.CollectionUpdateAction;
import org.hibernate.action.internal.EntityAction;
import org.hibernate.action.internal.EntityDeleteAction;
import org.hibernate.action.internal.EntityIdentityInsertAction;
import org.hibernate.action.internal.EntityInsertAction;
import org.hibernate.action.internal.EntityUpdateAction;
import org.hibernate.action.internal.UnresolvedEntityInsertActions;
import org.hibernate.action.spi.AfterTransactionCompletionProcess;
import org.hibernate.action.spi.BeforeTransactionCompletionProcess;
import org.hibernate.action.spi.Executable;
import org.hibernate.cache.CacheException;
import org.hibernate.engine.internal.NonNullableTransientDependencies;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public class ActionQueue {
   static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, ActionQueue.class.getName());
   private static final int INIT_QUEUE_LIST_SIZE = 5;
   private SessionImplementor session;
   private UnresolvedEntityInsertActions unresolvedInsertions;
   private ArrayList insertions;
   private ArrayList deletions;
   private ArrayList updates;
   private ArrayList collectionCreations;
   private ArrayList collectionUpdates;
   private ArrayList collectionRemovals;
   private AfterTransactionCompletionProcessQueue afterTransactionProcesses;
   private BeforeTransactionCompletionProcessQueue beforeTransactionProcesses;

   public ActionQueue(SessionImplementor session) {
      this.session = session;
      this.init();
   }

   private void init() {
      this.unresolvedInsertions = new UnresolvedEntityInsertActions();
      this.insertions = new ArrayList(5);
      this.deletions = new ArrayList(5);
      this.updates = new ArrayList(5);
      this.collectionCreations = new ArrayList(5);
      this.collectionRemovals = new ArrayList(5);
      this.collectionUpdates = new ArrayList(5);
      this.afterTransactionProcesses = new AfterTransactionCompletionProcessQueue(this.session);
      this.beforeTransactionProcesses = new BeforeTransactionCompletionProcessQueue(this.session);
   }

   public void clear() {
      this.updates.clear();
      this.insertions.clear();
      this.deletions.clear();
      this.collectionCreations.clear();
      this.collectionRemovals.clear();
      this.collectionUpdates.clear();
      this.unresolvedInsertions.clear();
   }

   public void addAction(EntityInsertAction action) {
      LOG.tracev("Adding an EntityInsertAction for [{0}] object", action.getEntityName());
      this.addInsertAction(action);
   }

   public void addAction(EntityDeleteAction action) {
      this.deletions.add(action);
   }

   public void addAction(EntityUpdateAction action) {
      this.updates.add(action);
   }

   public void addAction(CollectionRecreateAction action) {
      this.collectionCreations.add(action);
   }

   public void addAction(CollectionRemoveAction action) {
      this.collectionRemovals.add(action);
   }

   public void addAction(CollectionUpdateAction action) {
      this.collectionUpdates.add(action);
   }

   public void addAction(EntityIdentityInsertAction insert) {
      LOG.tracev("Adding an EntityIdentityInsertAction for [{0}] object", insert.getEntityName());
      this.addInsertAction(insert);
   }

   private void addInsertAction(AbstractEntityInsertAction insert) {
      if (insert.isEarlyInsert()) {
         LOG.tracev("Executing inserts before finding non-nullable transient entities for early insert: [{0}]", insert);
         this.executeInserts();
      }

      NonNullableTransientDependencies nonNullableTransientDependencies = insert.findNonNullableTransientEntities();
      if (nonNullableTransientDependencies == null) {
         LOG.tracev("Adding insert with no non-nullable, transient entities: [{0}]", insert);
         this.addResolvedEntityInsertAction(insert);
      } else {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Adding insert with non-nullable, transient entities; insert=[{0}], dependencies=[{1}]", insert, nonNullableTransientDependencies.toLoggableString(insert.getSession()));
         }

         this.unresolvedInsertions.addUnresolvedEntityInsertAction(insert, nonNullableTransientDependencies);
      }

   }

   private void addResolvedEntityInsertAction(AbstractEntityInsertAction insert) {
      if (insert.isEarlyInsert()) {
         LOG.trace("Executing insertions before resolved early-insert");
         this.executeInserts();
         LOG.debug("Executing identity-insert immediately");
         this.execute(insert);
      } else {
         LOG.trace("Adding resolved non-early insert action.");
         this.insertions.add(insert);
      }

      insert.makeEntityManaged();

      for(AbstractEntityInsertAction resolvedAction : this.unresolvedInsertions.resolveDependentActions(insert.getInstance(), this.session)) {
         this.addResolvedEntityInsertAction(resolvedAction);
      }

   }

   public boolean hasUnresolvedEntityInsertActions() {
      return !this.unresolvedInsertions.isEmpty();
   }

   public void checkNoUnresolvedActionsAfterOperation() throws PropertyValueException {
      this.unresolvedInsertions.checkNoUnresolvedActionsAfterOperation();
   }

   public void addAction(BulkOperationCleanupAction cleanupAction) {
      this.registerCleanupActions(cleanupAction);
   }

   public void registerProcess(AfterTransactionCompletionProcess process) {
      this.afterTransactionProcesses.register(process);
   }

   public void registerProcess(BeforeTransactionCompletionProcess process) {
      this.beforeTransactionProcesses.register(process);
   }

   public void executeInserts() throws HibernateException {
      this.executeActions(this.insertions);
   }

   public void executeActions() throws HibernateException {
      if (!this.unresolvedInsertions.isEmpty()) {
         throw new IllegalStateException("About to execute actions, but there are unresolved entity insert actions.");
      } else {
         this.executeActions(this.insertions);
         this.executeActions(this.updates);
         this.executeActions(this.collectionRemovals);
         this.executeActions(this.collectionUpdates);
         this.executeActions(this.collectionCreations);
         this.executeActions(this.deletions);
      }
   }

   public void prepareActions() throws HibernateException {
      this.prepareActions(this.collectionRemovals);
      this.prepareActions(this.collectionUpdates);
      this.prepareActions(this.collectionCreations);
   }

   public void afterTransactionCompletion(boolean success) {
      this.afterTransactionProcesses.afterTransactionCompletion(success);
   }

   public void beforeTransactionCompletion() {
      this.beforeTransactionProcesses.beforeTransactionCompletion();
   }

   public boolean areTablesToBeUpdated(Set tables) {
      return areTablesToUpdated(this.updates, tables) || areTablesToUpdated(this.insertions, tables) || areTablesToUpdated(this.unresolvedInsertions.getDependentEntityInsertActions(), tables) || areTablesToUpdated(this.deletions, tables) || areTablesToUpdated(this.collectionUpdates, tables) || areTablesToUpdated(this.collectionCreations, tables) || areTablesToUpdated(this.collectionRemovals, tables);
   }

   public boolean areInsertionsOrDeletionsQueued() {
      return this.insertions.size() > 0 || !this.unresolvedInsertions.isEmpty() || this.deletions.size() > 0;
   }

   private static boolean areTablesToUpdated(Iterable actions, Set tableSpaces) {
      for(Executable action : actions) {
         Serializable[] spaces = action.getPropertySpaces();

         for(Serializable space : spaces) {
            if (tableSpaces.contains(space)) {
               LOG.debugf("Changes must be flushed to space: %s", space);
               return true;
            }
         }
      }

      return false;
   }

   private void executeActions(List list) throws HibernateException {
      for(Object aList : list) {
         this.execute((Executable)aList);
      }

      list.clear();
      this.session.getTransactionCoordinator().getJdbcCoordinator().executeBatch();
   }

   public void execute(Executable executable) {
      try {
         executable.execute();
      } finally {
         this.registerCleanupActions(executable);
      }

   }

   private void registerCleanupActions(Executable executable) {
      this.beforeTransactionProcesses.register(executable.getBeforeTransactionCompletionProcess());
      if (this.session.getFactory().getSettings().isQueryCacheEnabled()) {
         String[] spaces = (String[])executable.getPropertySpaces();
         if (spaces != null && spaces.length > 0) {
            this.afterTransactionProcesses.addSpacesToInvalidate(spaces);
            this.session.getFactory().getUpdateTimestampsCache().preinvalidate(spaces);
         }
      }

      this.afterTransactionProcesses.register(executable.getAfterTransactionCompletionProcess());
   }

   private void prepareActions(List queue) throws HibernateException {
      for(Executable executable : queue) {
         executable.beforeExecutions();
      }

   }

   public String toString() {
      return "ActionQueue[insertions=" + this.insertions + " updates=" + this.updates + " deletions=" + this.deletions + " collectionCreations=" + this.collectionCreations + " collectionRemovals=" + this.collectionRemovals + " collectionUpdates=" + this.collectionUpdates + " unresolvedInsertDependencies=" + this.unresolvedInsertions + "]";
   }

   public int numberOfCollectionRemovals() {
      return this.collectionRemovals.size();
   }

   public int numberOfCollectionUpdates() {
      return this.collectionUpdates.size();
   }

   public int numberOfCollectionCreations() {
      return this.collectionCreations.size();
   }

   public int numberOfDeletions() {
      return this.deletions.size();
   }

   public int numberOfUpdates() {
      return this.updates.size();
   }

   public int numberOfInsertions() {
      return this.insertions.size();
   }

   public void sortCollectionActions() {
      if (this.session.getFactory().getSettings().isOrderUpdatesEnabled()) {
         Collections.sort(this.collectionCreations);
         Collections.sort(this.collectionUpdates);
         Collections.sort(this.collectionRemovals);
      }

   }

   public void sortActions() {
      if (this.session.getFactory().getSettings().isOrderUpdatesEnabled()) {
         Collections.sort(this.updates);
      }

      if (this.session.getFactory().getSettings().isOrderInsertsEnabled()) {
         this.sortInsertActions();
      }

   }

   private void sortInsertActions() {
      (new InsertActionSorter()).sort();
   }

   public ArrayList cloneDeletions() {
      return (ArrayList)this.deletions.clone();
   }

   public void clearFromFlushNeededCheck(int previousCollectionRemovalSize) {
      this.collectionCreations.clear();
      this.collectionUpdates.clear();
      this.updates.clear();

      for(int i = this.collectionRemovals.size() - 1; i >= previousCollectionRemovalSize; --i) {
         this.collectionRemovals.remove(i);
      }

   }

   public boolean hasAfterTransactionActions() {
      return this.afterTransactionProcesses.processes.size() > 0;
   }

   public boolean hasBeforeTransactionActions() {
      return this.beforeTransactionProcesses.processes.size() > 0;
   }

   public boolean hasAnyQueuedActions() {
      return this.updates.size() > 0 || this.insertions.size() > 0 || !this.unresolvedInsertions.isEmpty() || this.deletions.size() > 0 || this.collectionUpdates.size() > 0 || this.collectionRemovals.size() > 0 || this.collectionCreations.size() > 0;
   }

   public void unScheduleDeletion(EntityEntry entry, Object rescuedEntity) {
      for(int i = 0; i < this.deletions.size(); ++i) {
         EntityDeleteAction action = (EntityDeleteAction)this.deletions.get(i);
         if (action.getInstance() == rescuedEntity) {
            this.deletions.remove(i);
            return;
         }
      }

      throw new AssertionFailure("Unable to perform un-delete for instance " + entry.getEntityName());
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      LOG.trace("Serializing action-queue");
      this.unresolvedInsertions.serialize(oos);
      int queueSize = this.insertions.size();
      LOG.tracev("Starting serialization of [{0}] insertions entries", queueSize);
      oos.writeInt(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         oos.writeObject(this.insertions.get(i));
      }

      queueSize = this.deletions.size();
      LOG.tracev("Starting serialization of [{0}] deletions entries", queueSize);
      oos.writeInt(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         oos.writeObject(this.deletions.get(i));
      }

      queueSize = this.updates.size();
      LOG.tracev("Starting serialization of [{0}] updates entries", queueSize);
      oos.writeInt(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         oos.writeObject(this.updates.get(i));
      }

      queueSize = this.collectionUpdates.size();
      LOG.tracev("Starting serialization of [{0}] collectionUpdates entries", queueSize);
      oos.writeInt(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         oos.writeObject(this.collectionUpdates.get(i));
      }

      queueSize = this.collectionRemovals.size();
      LOG.tracev("Starting serialization of [{0}] collectionRemovals entries", queueSize);
      oos.writeInt(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         oos.writeObject(this.collectionRemovals.get(i));
      }

      queueSize = this.collectionCreations.size();
      LOG.tracev("Starting serialization of [{0}] collectionCreations entries", queueSize);
      oos.writeInt(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         oos.writeObject(this.collectionCreations.get(i));
      }

   }

   public static ActionQueue deserialize(ObjectInputStream ois, SessionImplementor session) throws IOException, ClassNotFoundException {
      LOG.trace("Dedeserializing action-queue");
      ActionQueue rtn = new ActionQueue(session);
      rtn.unresolvedInsertions = UnresolvedEntityInsertActions.deserialize(ois, session);
      int queueSize = ois.readInt();
      LOG.tracev("Starting deserialization of [{0}] insertions entries", queueSize);
      rtn.insertions = new ArrayList(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         EntityAction action = (EntityAction)ois.readObject();
         action.afterDeserialize(session);
         rtn.insertions.add(action);
      }

      queueSize = ois.readInt();
      LOG.tracev("Starting deserialization of [{0}] deletions entries", queueSize);
      rtn.deletions = new ArrayList(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         EntityDeleteAction action = (EntityDeleteAction)ois.readObject();
         action.afterDeserialize(session);
         rtn.deletions.add(action);
      }

      queueSize = ois.readInt();
      LOG.tracev("Starting deserialization of [{0}] updates entries", queueSize);
      rtn.updates = new ArrayList(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         EntityAction action = (EntityAction)ois.readObject();
         action.afterDeserialize(session);
         rtn.updates.add(action);
      }

      queueSize = ois.readInt();
      LOG.tracev("Starting deserialization of [{0}] collectionUpdates entries", queueSize);
      rtn.collectionUpdates = new ArrayList(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         CollectionAction action = (CollectionAction)ois.readObject();
         action.afterDeserialize(session);
         rtn.collectionUpdates.add(action);
      }

      queueSize = ois.readInt();
      LOG.tracev("Starting deserialization of [{0}] collectionRemovals entries", queueSize);
      rtn.collectionRemovals = new ArrayList(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         CollectionAction action = (CollectionAction)ois.readObject();
         action.afterDeserialize(session);
         rtn.collectionRemovals.add(action);
      }

      queueSize = ois.readInt();
      LOG.tracev("Starting deserialization of [{0}] collectionCreations entries", queueSize);
      rtn.collectionCreations = new ArrayList(queueSize);

      for(int i = 0; i < queueSize; ++i) {
         CollectionAction action = (CollectionAction)ois.readObject();
         action.afterDeserialize(session);
         rtn.collectionCreations.add(action);
      }

      return rtn;
   }

   private static class BeforeTransactionCompletionProcessQueue {
      private SessionImplementor session;
      private List processes;

      private BeforeTransactionCompletionProcessQueue(SessionImplementor session) {
         this.processes = new ArrayList();
         this.session = session;
      }

      public void register(BeforeTransactionCompletionProcess process) {
         if (process != null) {
            this.processes.add(process);
         }
      }

      public void beforeTransactionCompletion() {
         for(BeforeTransactionCompletionProcess process : this.processes) {
            try {
               process.doBeforeTransactionCompletion(this.session);
            } catch (HibernateException he) {
               throw he;
            } catch (Exception e) {
               throw new AssertionFailure("Unable to perform beforeTransactionCompletion callback", e);
            }
         }

         this.processes.clear();
      }
   }

   private static class AfterTransactionCompletionProcessQueue {
      private SessionImplementor session;
      private Set querySpacesToInvalidate;
      private List processes;

      private AfterTransactionCompletionProcessQueue(SessionImplementor session) {
         this.querySpacesToInvalidate = new HashSet();
         this.processes = new ArrayList(15);
         this.session = session;
      }

      public void addSpacesToInvalidate(String[] spaces) {
         for(String space : spaces) {
            this.addSpaceToInvalidate(space);
         }

      }

      public void addSpaceToInvalidate(String space) {
         this.querySpacesToInvalidate.add(space);
      }

      public void register(AfterTransactionCompletionProcess process) {
         if (process != null) {
            this.processes.add(process);
         }
      }

      public void afterTransactionCompletion(boolean success) {
         for(AfterTransactionCompletionProcess process : this.processes) {
            try {
               process.doAfterTransactionCompletion(success, this.session);
            } catch (CacheException ce) {
               ActionQueue.LOG.unableToReleaseCacheLock(ce);
            } catch (Exception e) {
               throw new AssertionFailure("Exception releasing cache locks", e);
            }
         }

         this.processes.clear();
         if (this.session.getFactory().getSettings().isQueryCacheEnabled()) {
            this.session.getFactory().getUpdateTimestampsCache().invalidate((Serializable[])this.querySpacesToInvalidate.toArray(new String[this.querySpacesToInvalidate.size()]));
         }

         this.querySpacesToInvalidate.clear();
      }
   }

   private class InsertActionSorter {
      private HashMap latestBatches = new HashMap();
      private HashMap entityBatchNumber;
      private HashMap actionBatches = new HashMap();

      public InsertActionSorter() {
         this.entityBatchNumber = new HashMap(ActionQueue.this.insertions.size() + 1, 1.0F);
      }

      public void sort() {
         for(EntityInsertAction action : ActionQueue.this.insertions) {
            String entityName = action.getEntityName();
            Object currentEntity = action.getInstance();
            Integer batchNumber;
            if (this.latestBatches.containsKey(entityName)) {
               batchNumber = this.findBatchNumber(action, entityName);
            } else {
               batchNumber = this.actionBatches.size();
               this.latestBatches.put(entityName, batchNumber);
            }

            this.entityBatchNumber.put(currentEntity, batchNumber);
            this.addToBatch(batchNumber, action);
         }

         ActionQueue.this.insertions.clear();

         for(int i = 0; i < this.actionBatches.size(); ++i) {
            for(EntityInsertAction action : (List)this.actionBatches.get(i)) {
               ActionQueue.this.insertions.add(action);
            }
         }

      }

      private Integer findBatchNumber(EntityInsertAction action, String entityName) {
         Integer latestBatchNumberForType = (Integer)this.latestBatches.get(entityName);
         Object[] propertyValues = action.getState();
         Type[] propertyTypes = action.getPersister().getClassMetadata().getPropertyTypes();

         for(int i = 0; i < propertyValues.length; ++i) {
            Object value = propertyValues[i];
            Type type = propertyTypes[i];
            if (type.isEntityType() && value != null) {
               Integer associationBatchNumber = (Integer)this.entityBatchNumber.get(value);
               if (associationBatchNumber != null && associationBatchNumber.compareTo(latestBatchNumberForType) > 0) {
                  latestBatchNumberForType = this.actionBatches.size();
                  this.latestBatches.put(entityName, latestBatchNumberForType);
                  break;
               }
            }
         }

         return latestBatchNumberForType;
      }

      private void addToBatch(Integer batchNumber, EntityInsertAction action) {
         List<EntityInsertAction> actions = (List)this.actionBatches.get(batchNumber);
         if (actions == null) {
            actions = new LinkedList();
            this.actionBatches.put(batchNumber, actions);
         }

         actions.add(action);
      }
   }
}
