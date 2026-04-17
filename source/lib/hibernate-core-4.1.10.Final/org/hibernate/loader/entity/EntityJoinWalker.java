package org.hibernate.loader.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.hibernate.FetchMode;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.CascadeStyle;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.loader.AbstractEntityJoinWalker;
import org.hibernate.loader.JoinWalker;
import org.hibernate.loader.OuterJoinableAssociation;
import org.hibernate.loader.PropertyPath;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.sql.JoinType;
import org.hibernate.type.AssociationType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class EntityJoinWalker extends AbstractEntityJoinWalker {
   private final LockOptions lockOptions = new LockOptions();
   private final int[][] compositeKeyManyToOneTargetIndices;

   public EntityJoinWalker(OuterJoinLoadable persister, String[] uniqueKey, int batchSize, LockMode lockMode, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(persister, factory, loadQueryInfluencers);
      this.lockOptions.setLockMode(lockMode);
      StringBuilder whereCondition = this.whereString(this.getAlias(), uniqueKey, batchSize).append(persister.filterFragment(this.getAlias(), Collections.EMPTY_MAP));
      AssociationInitCallbackImpl callback = new AssociationInitCallbackImpl(factory);
      this.initAll(whereCondition.toString(), "", this.lockOptions, callback);
      this.compositeKeyManyToOneTargetIndices = callback.resolve();
   }

   public EntityJoinWalker(OuterJoinLoadable persister, String[] uniqueKey, int batchSize, LockOptions lockOptions, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(persister, factory, loadQueryInfluencers);
      LockOptions.copy(lockOptions, this.lockOptions);
      StringBuilder whereCondition = this.whereString(this.getAlias(), uniqueKey, batchSize).append(persister.filterFragment(this.getAlias(), Collections.EMPTY_MAP));
      AssociationInitCallbackImpl callback = new AssociationInitCallbackImpl(factory);
      this.initAll(whereCondition.toString(), "", lockOptions, callback);
      this.compositeKeyManyToOneTargetIndices = callback.resolve();
   }

   protected JoinType getJoinType(OuterJoinLoadable persister, PropertyPath path, int propertyNumber, AssociationType associationType, FetchMode metadataFetchMode, CascadeStyle metadataCascadeStyle, String lhsTable, String[] lhsColumns, boolean nullable, int currentDepth) throws MappingException {
      if (this.lockOptions.getLockMode().greaterThan(LockMode.READ)) {
         return JoinType.NONE;
      } else if (!this.isTooDeep(currentDepth) && (!associationType.isCollectionType() || !this.isTooManyCollections())) {
         if (!this.isJoinedFetchEnabledInMapping(metadataFetchMode, associationType) && !this.isJoinFetchEnabledByProfile(persister, path, propertyNumber)) {
            return JoinType.NONE;
         } else {
            return this.isDuplicateAssociation(lhsTable, lhsColumns, associationType) ? JoinType.NONE : this.getJoinType(nullable, currentDepth);
         }
      } else {
         return JoinType.NONE;
      }
   }

   public String getComment() {
      return "load " + this.getPersister().getEntityName();
   }

   public int[][] getCompositeKeyManyToOneTargetIndices() {
      return this.compositeKeyManyToOneTargetIndices;
   }

   private static class AssociationInitCallbackImpl implements JoinWalker.AssociationInitCallback {
      private final SessionFactoryImplementor factory;
      private final HashMap associationsByAlias = new HashMap();
      private final HashMap positionsByAlias = new HashMap();
      private final ArrayList aliasesForAssociationsWithCompositesIds = new ArrayList();

      public AssociationInitCallbackImpl(SessionFactoryImplementor factory) {
         this.factory = factory;
      }

      public void associationProcessed(OuterJoinableAssociation oja, int position) {
         this.associationsByAlias.put(oja.getRhsAlias(), oja);
         this.positionsByAlias.put(oja.getRhsAlias(), position);
         EntityPersister entityPersister = null;
         if (oja.getJoinableType().isCollectionType()) {
            entityPersister = ((QueryableCollection)oja.getJoinable()).getElementPersister();
         } else if (oja.getJoinableType().isEntityType()) {
            entityPersister = (EntityPersister)oja.getJoinable();
         }

         if (entityPersister != null && entityPersister.getIdentifierType().isComponentType() && !entityPersister.getEntityMetamodel().getIdentifierProperty().isEmbedded() && this.hasAssociation((CompositeType)entityPersister.getIdentifierType())) {
            this.aliasesForAssociationsWithCompositesIds.add(oja.getRhsAlias());
         }

      }

      private boolean hasAssociation(CompositeType componentType) {
         for(Type subType : componentType.getSubtypes()) {
            if (subType.isEntityType()) {
               return true;
            }

            if (subType.isComponentType() && this.hasAssociation((CompositeType)subType)) {
               return true;
            }
         }

         return false;
      }

      public int[][] resolve() {
         int[][] compositeKeyManyToOneTargetIndices = (int[][])null;

         for(String aliasWithCompositeId : this.aliasesForAssociationsWithCompositesIds) {
            OuterJoinableAssociation joinWithCompositeId = (OuterJoinableAssociation)this.associationsByAlias.get(aliasWithCompositeId);
            ArrayList<Integer> keyManyToOneTargetIndices = new ArrayList();
            EntityPersister entityPersister = null;
            if (joinWithCompositeId.getJoinableType().isCollectionType()) {
               entityPersister = ((QueryableCollection)joinWithCompositeId.getJoinable()).getElementPersister();
            } else if (joinWithCompositeId.getJoinableType().isEntityType()) {
               entityPersister = (EntityPersister)joinWithCompositeId.getJoinable();
            }

            this.findKeyManyToOneTargetIndices(keyManyToOneTargetIndices, joinWithCompositeId, (CompositeType)entityPersister.getIdentifierType());
            if (!keyManyToOneTargetIndices.isEmpty()) {
               if (compositeKeyManyToOneTargetIndices == null) {
                  compositeKeyManyToOneTargetIndices = new int[this.associationsByAlias.size()][];
               }

               int position = (Integer)this.positionsByAlias.get(aliasWithCompositeId);
               compositeKeyManyToOneTargetIndices[position] = new int[keyManyToOneTargetIndices.size()];
               int i = 0;

               for(int index : keyManyToOneTargetIndices) {
                  compositeKeyManyToOneTargetIndices[position][i] = index;
                  ++i;
               }
            }
         }

         return compositeKeyManyToOneTargetIndices;
      }

      private void findKeyManyToOneTargetIndices(ArrayList keyManyToOneTargetIndices, OuterJoinableAssociation joinWithCompositeId, CompositeType componentType) {
         for(Type subType : componentType.getSubtypes()) {
            if (subType.isEntityType()) {
               Integer index = this.locateKeyManyToOneTargetIndex(joinWithCompositeId, (EntityType)subType);
               if (index != null) {
                  keyManyToOneTargetIndices.add(index);
               }
            } else if (subType.isComponentType()) {
               this.findKeyManyToOneTargetIndices(keyManyToOneTargetIndices, joinWithCompositeId, (CompositeType)subType);
            }
         }

      }

      private Integer locateKeyManyToOneTargetIndex(OuterJoinableAssociation joinWithCompositeId, EntityType keyManyToOneType) {
         if (joinWithCompositeId.getLhsAlias() != null) {
            OuterJoinableAssociation lhs = (OuterJoinableAssociation)this.associationsByAlias.get(joinWithCompositeId.getLhsAlias());
            if (keyManyToOneType.getAssociatedEntityName(this.factory).equals(lhs.getJoinableType().getAssociatedEntityName(this.factory))) {
               return (Integer)this.positionsByAlias.get(lhs.getRhsAlias());
            }
         }

         for(OuterJoinableAssociation oja : this.associationsByAlias.values()) {
            if (oja.getLhsAlias() != null && oja.getLhsAlias().equals(joinWithCompositeId.getRhsAlias()) && keyManyToOneType.equals(oja.getJoinableType())) {
               return (Integer)this.positionsByAlias.get(oja.getLhsAlias());
            }
         }

         return null;
      }
   }
}
