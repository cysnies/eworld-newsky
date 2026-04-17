package org.hibernate.persister.internal;

import java.util.Iterator;
import org.hibernate.mapping.Collection;
import org.hibernate.mapping.JoinedSubclass;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.RootClass;
import org.hibernate.mapping.SingleTableSubclass;
import org.hibernate.mapping.UnionSubclass;
import org.hibernate.metamodel.binding.CollectionElementNature;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.metamodel.binding.PluralAttributeBinding;
import org.hibernate.persister.collection.BasicCollectionPersister;
import org.hibernate.persister.collection.OneToManyPersister;
import org.hibernate.persister.entity.JoinedSubclassEntityPersister;
import org.hibernate.persister.entity.SingleTableEntityPersister;
import org.hibernate.persister.entity.UnionSubclassEntityPersister;
import org.hibernate.persister.spi.PersisterClassResolver;
import org.hibernate.persister.spi.UnknownPersisterException;

public class StandardPersisterClassResolver implements PersisterClassResolver {
   public Class getEntityPersisterClass(EntityBinding metadata) {
      if (metadata.isRoot()) {
         Iterator<EntityBinding> subEntityBindingIterator = metadata.getDirectSubEntityBindings().iterator();
         if (!subEntityBindingIterator.hasNext()) {
            return this.singleTableEntityPersister();
         }

         metadata = (EntityBinding)subEntityBindingIterator.next();
      }

      switch (metadata.getHierarchyDetails().getInheritanceType()) {
         case JOINED:
            return this.joinedSubclassEntityPersister();
         case SINGLE_TABLE:
            return this.singleTableEntityPersister();
         case TABLE_PER_CLASS:
            return this.unionSubclassEntityPersister();
         default:
            throw new UnknownPersisterException("Could not determine persister implementation for entity [" + metadata.getEntity().getName() + "]");
      }
   }

   public Class getEntityPersisterClass(PersistentClass metadata) {
      if (RootClass.class.isInstance(metadata)) {
         if (!metadata.hasSubclasses()) {
            return this.singleTableEntityPersister();
         }

         metadata = (PersistentClass)metadata.getDirectSubclasses().next();
      }

      if (JoinedSubclass.class.isInstance(metadata)) {
         return this.joinedSubclassEntityPersister();
      } else if (UnionSubclass.class.isInstance(metadata)) {
         return this.unionSubclassEntityPersister();
      } else if (SingleTableSubclass.class.isInstance(metadata)) {
         return this.singleTableEntityPersister();
      } else {
         throw new UnknownPersisterException("Could not determine persister implementation for entity [" + metadata.getEntityName() + "]");
      }
   }

   public Class singleTableEntityPersister() {
      return SingleTableEntityPersister.class;
   }

   public Class joinedSubclassEntityPersister() {
      return JoinedSubclassEntityPersister.class;
   }

   public Class unionSubclassEntityPersister() {
      return UnionSubclassEntityPersister.class;
   }

   public Class getCollectionPersisterClass(Collection metadata) {
      return metadata.isOneToMany() ? this.oneToManyPersister() : this.basicCollectionPersister();
   }

   public Class getCollectionPersisterClass(PluralAttributeBinding metadata) {
      return metadata.getCollectionElement().getCollectionElementNature() == CollectionElementNature.ONE_TO_MANY ? this.oneToManyPersister() : this.basicCollectionPersister();
   }

   private Class oneToManyPersister() {
      return OneToManyPersister.class;
   }

   private Class basicCollectionPersister() {
      return BasicCollectionPersister.class;
   }
}
