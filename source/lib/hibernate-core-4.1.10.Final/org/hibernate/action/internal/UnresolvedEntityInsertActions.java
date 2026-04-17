package org.hibernate.action.internal;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.hibernate.PropertyValueException;
import org.hibernate.TransientPropertyValueException;
import org.hibernate.engine.internal.NonNullableTransientDependencies;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.spi.Status;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.collections.IdentitySet;
import org.hibernate.pretty.MessageHelper;
import org.jboss.logging.Logger;

public class UnresolvedEntityInsertActions {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, UnresolvedEntityInsertActions.class.getName());
   private static final int INIT_SIZE = 5;
   private final Map dependenciesByAction = new IdentityHashMap(5);
   private final Map dependentActionsByTransientEntity = new IdentityHashMap(5);

   public void addUnresolvedEntityInsertAction(AbstractEntityInsertAction insert, NonNullableTransientDependencies dependencies) {
      if (dependencies != null && !dependencies.isEmpty()) {
         if (LOG.isTraceEnabled()) {
            LOG.tracev("Adding insert with non-nullable, transient entities; insert=[{0}], dependencies=[{1}]", insert, dependencies.toLoggableString(insert.getSession()));
         }

         this.dependenciesByAction.put(insert, dependencies);
         this.addDependenciesByTransientEntity(insert, dependencies);
      } else {
         throw new IllegalArgumentException("Attempt to add an unresolved insert action that has no non-nullable transient entities.");
      }
   }

   public Iterable getDependentEntityInsertActions() {
      return this.dependenciesByAction.keySet();
   }

   public void checkNoUnresolvedActionsAfterOperation() throws PropertyValueException {
      if (this.isEmpty()) {
         LOG.trace("No entity insert actions have non-nullable, transient entity dependencies.");
      } else {
         AbstractEntityInsertAction firstDependentAction = (AbstractEntityInsertAction)this.dependenciesByAction.keySet().iterator().next();
         this.logCannotResolveNonNullableTransientDependencies(firstDependentAction.getSession());
         NonNullableTransientDependencies nonNullableTransientDependencies = (NonNullableTransientDependencies)this.dependenciesByAction.get(firstDependentAction);
         Object firstTransientDependency = nonNullableTransientDependencies.getNonNullableTransientEntities().iterator().next();
         String firstPropertyPath = (String)nonNullableTransientDependencies.getNonNullableTransientPropertyPaths(firstTransientDependency).iterator().next();
         throw new TransientPropertyValueException("Not-null property references a transient value - transient instance must be saved before current operation", firstDependentAction.getSession().guessEntityName(firstTransientDependency), firstDependentAction.getEntityName(), firstPropertyPath);
      }
   }

   private void logCannotResolveNonNullableTransientDependencies(SessionImplementor session) {
      for(Map.Entry entry : this.dependentActionsByTransientEntity.entrySet()) {
         Object transientEntity = entry.getKey();
         String transientEntityName = session.guessEntityName(transientEntity);
         Serializable transientEntityId = session.getFactory().getEntityPersister(transientEntityName).getIdentifier(transientEntity, session);
         String transientEntityString = MessageHelper.infoString(transientEntityName, transientEntityId);
         Set<String> dependentEntityStrings = new TreeSet();
         Set<String> nonNullableTransientPropertyPaths = new TreeSet();

         for(AbstractEntityInsertAction dependentAction : (Set)entry.getValue()) {
            dependentEntityStrings.add(MessageHelper.infoString(dependentAction.getEntityName(), dependentAction.getId()));

            for(String path : ((NonNullableTransientDependencies)this.dependenciesByAction.get(dependentAction)).getNonNullableTransientPropertyPaths(transientEntity)) {
               String fullPath = (new StringBuilder(dependentAction.getEntityName().length() + path.length() + 1)).append(dependentAction.getEntityName()).append('.').append(path).toString();
               nonNullableTransientPropertyPaths.add(fullPath);
            }
         }

         LOG.cannotResolveNonNullableTransientDependencies(transientEntityString, dependentEntityStrings, nonNullableTransientPropertyPaths);
      }

   }

   public boolean isEmpty() {
      return this.dependenciesByAction.isEmpty();
   }

   private void addDependenciesByTransientEntity(AbstractEntityInsertAction insert, NonNullableTransientDependencies dependencies) {
      for(Object transientEntity : dependencies.getNonNullableTransientEntities()) {
         Set<AbstractEntityInsertAction> dependentActions = (Set)this.dependentActionsByTransientEntity.get(transientEntity);
         if (dependentActions == null) {
            dependentActions = new IdentitySet();
            this.dependentActionsByTransientEntity.put(transientEntity, dependentActions);
         }

         dependentActions.add(insert);
      }

   }

   public Set resolveDependentActions(Object managedEntity, SessionImplementor session) {
      EntityEntry entityEntry = session.getPersistenceContext().getEntry(managedEntity);
      if (entityEntry.getStatus() != Status.MANAGED && entityEntry.getStatus() != Status.READ_ONLY) {
         throw new IllegalArgumentException("EntityEntry did not have status MANAGED or READ_ONLY: " + entityEntry);
      } else {
         Set<AbstractEntityInsertAction> dependentActions = (Set)this.dependentActionsByTransientEntity.remove(managedEntity);
         if (dependentActions == null) {
            if (LOG.isTraceEnabled()) {
               LOG.tracev("No unresolved entity inserts that depended on [{0}]", MessageHelper.infoString(entityEntry.getEntityName(), entityEntry.getId()));
            }

            return Collections.emptySet();
         } else {
            Set<AbstractEntityInsertAction> resolvedActions = new IdentitySet();
            if (LOG.isTraceEnabled()) {
               LOG.tracev("Unresolved inserts before resolving [{0}]: [{1}]", MessageHelper.infoString(entityEntry.getEntityName(), entityEntry.getId()), this.toString());
            }

            for(AbstractEntityInsertAction dependentAction : dependentActions) {
               if (LOG.isTraceEnabled()) {
                  LOG.tracev("Resolving insert [{0}] dependency on [{1}]", MessageHelper.infoString(dependentAction.getEntityName(), dependentAction.getId()), MessageHelper.infoString(entityEntry.getEntityName(), entityEntry.getId()));
               }

               NonNullableTransientDependencies dependencies = (NonNullableTransientDependencies)this.dependenciesByAction.get(dependentAction);
               dependencies.resolveNonNullableTransientEntity(managedEntity);
               if (dependencies.isEmpty()) {
                  if (LOG.isTraceEnabled()) {
                     LOG.tracev("Resolving insert [{0}] (only depended on [{1}])", dependentAction, MessageHelper.infoString(entityEntry.getEntityName(), entityEntry.getId()));
                  }

                  this.dependenciesByAction.remove(dependentAction);
                  resolvedActions.add(dependentAction);
               }
            }

            if (LOG.isTraceEnabled()) {
               LOG.tracev("Unresolved inserts after resolving [{0}]: [{1}]", MessageHelper.infoString(entityEntry.getEntityName(), entityEntry.getId()), this.toString());
            }

            return resolvedActions;
         }
      }
   }

   public void clear() {
      this.dependenciesByAction.clear();
      this.dependentActionsByTransientEntity.clear();
   }

   public String toString() {
      StringBuilder sb = (new StringBuilder(this.getClass().getSimpleName())).append('[');

      for(Map.Entry entry : this.dependenciesByAction.entrySet()) {
         AbstractEntityInsertAction insert = (AbstractEntityInsertAction)entry.getKey();
         NonNullableTransientDependencies dependencies = (NonNullableTransientDependencies)entry.getValue();
         sb.append("[insert=").append(insert).append(" dependencies=[").append(dependencies.toLoggableString(insert.getSession())).append("]");
      }

      sb.append(']');
      return sb.toString();
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      int queueSize = this.dependenciesByAction.size();
      LOG.tracev("Starting serialization of [{0}] unresolved insert entries", queueSize);
      oos.writeInt(queueSize);

      for(AbstractEntityInsertAction unresolvedAction : this.dependenciesByAction.keySet()) {
         oos.writeObject(unresolvedAction);
      }

   }

   public static UnresolvedEntityInsertActions deserialize(ObjectInputStream ois, SessionImplementor session) throws IOException, ClassNotFoundException {
      UnresolvedEntityInsertActions rtn = new UnresolvedEntityInsertActions();
      int queueSize = ois.readInt();
      LOG.tracev("Starting deserialization of [{0}] unresolved insert entries", queueSize);

      for(int i = 0; i < queueSize; ++i) {
         AbstractEntityInsertAction unresolvedAction = (AbstractEntityInsertAction)ois.readObject();
         unresolvedAction.afterDeserialize(session);
         rtn.addUnresolvedEntityInsertAction(unresolvedAction, unresolvedAction.findNonNullableTransientEntities());
      }

      return rtn;
   }
}
