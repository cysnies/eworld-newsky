package org.hibernate.loader;

import java.util.ArrayList;
import java.util.List;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.type.BagType;

public abstract class BasicLoader extends Loader {
   protected static final String[] NO_SUFFIX = new String[]{""};
   private EntityAliases[] descriptors;
   private CollectionAliases[] collectionDescriptors;

   public BasicLoader(SessionFactoryImplementor factory) {
      super(factory);
   }

   protected final EntityAliases[] getEntityAliases() {
      return this.descriptors;
   }

   protected final CollectionAliases[] getCollectionAliases() {
      return this.collectionDescriptors;
   }

   protected abstract String[] getSuffixes();

   protected abstract String[] getCollectionSuffixes();

   protected void postInstantiate() {
      Loadable[] persisters = this.getEntityPersisters();
      String[] suffixes = this.getSuffixes();
      this.descriptors = new EntityAliases[persisters.length];

      for(int i = 0; i < this.descriptors.length; ++i) {
         this.descriptors[i] = new DefaultEntityAliases(persisters[i], suffixes[i]);
      }

      CollectionPersister[] collectionPersisters = this.getCollectionPersisters();
      List bagRoles = null;
      if (collectionPersisters != null) {
         String[] collectionSuffixes = this.getCollectionSuffixes();
         this.collectionDescriptors = new CollectionAliases[collectionPersisters.length];

         for(int i = 0; i < collectionPersisters.length; ++i) {
            if (this.isBag(collectionPersisters[i])) {
               if (bagRoles == null) {
                  bagRoles = new ArrayList();
               }

               bagRoles.add(collectionPersisters[i].getRole());
            }

            this.collectionDescriptors[i] = new GeneratedCollectionAliases(collectionPersisters[i], collectionSuffixes[i]);
         }
      } else {
         this.collectionDescriptors = null;
      }

      if (bagRoles != null && bagRoles.size() > 1) {
         throw new MultipleBagFetchException(bagRoles);
      }
   }

   private boolean isBag(CollectionPersister collectionPersister) {
      return collectionPersister.getCollectionType().getClass().isAssignableFrom(BagType.class);
   }

   public static String[] generateSuffixes(int length) {
      return generateSuffixes(0, length);
   }

   public static String[] generateSuffixes(int seed, int length) {
      if (length == 0) {
         return NO_SUFFIX;
      } else {
         String[] suffixes = new String[length];

         for(int i = 0; i < length; ++i) {
            suffixes[i] = Integer.toString(i + seed) + "_";
         }

         return suffixes;
      }
   }
}
