package org.hibernate.loader;

import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.Loadable;
import org.hibernate.type.EntityType;

public abstract class OuterJoinLoader extends BasicLoader {
   protected Loadable[] persisters;
   protected CollectionPersister[] collectionPersisters;
   protected int[] collectionOwners;
   protected String[] aliases;
   private LockOptions lockOptions;
   protected LockMode[] lockModeArray;
   protected int[] owners;
   protected EntityType[] ownerAssociationTypes;
   protected String sql;
   protected String[] suffixes;
   protected String[] collectionSuffixes;
   private LoadQueryInfluencers loadQueryInfluencers;

   protected final Dialect getDialect() {
      return this.getFactory().getDialect();
   }

   public OuterJoinLoader(SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) {
      super(factory);
      this.loadQueryInfluencers = loadQueryInfluencers;
   }

   protected String[] getSuffixes() {
      return this.suffixes;
   }

   protected String[] getCollectionSuffixes() {
      return this.collectionSuffixes;
   }

   protected final String getSQLString() {
      return this.sql;
   }

   protected final Loadable[] getEntityPersisters() {
      return this.persisters;
   }

   protected int[] getOwners() {
      return this.owners;
   }

   protected EntityType[] getOwnerAssociationTypes() {
      return this.ownerAssociationTypes;
   }

   protected LockMode[] getLockModes(LockOptions lockOptions) {
      return this.lockModeArray;
   }

   protected LockOptions getLockOptions() {
      return this.lockOptions;
   }

   public LoadQueryInfluencers getLoadQueryInfluencers() {
      return this.loadQueryInfluencers;
   }

   protected final String[] getAliases() {
      return this.aliases;
   }

   protected final CollectionPersister[] getCollectionPersisters() {
      return this.collectionPersisters;
   }

   protected final int[] getCollectionOwners() {
      return this.collectionOwners;
   }

   protected void initFromWalker(JoinWalker walker) {
      this.persisters = walker.getPersisters();
      this.collectionPersisters = walker.getCollectionPersisters();
      this.ownerAssociationTypes = walker.getOwnerAssociationTypes();
      this.lockOptions = walker.getLockModeOptions();
      this.lockModeArray = walker.getLockModeArray();
      this.suffixes = walker.getSuffixes();
      this.collectionSuffixes = walker.getCollectionSuffixes();
      this.owners = walker.getOwners();
      this.collectionOwners = walker.getCollectionOwners();
      this.sql = walker.getSQLString();
      this.aliases = walker.getAliases();
   }
}
