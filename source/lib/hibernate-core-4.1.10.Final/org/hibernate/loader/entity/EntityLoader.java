package org.hibernate.loader.entity;

import java.io.Serializable;
import org.hibernate.LockMode;
import org.hibernate.LockOptions;
import org.hibernate.MappingException;
import org.hibernate.engine.spi.LoadQueryInfluencers;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.OuterJoinLoadable;
import org.hibernate.type.Type;

public class EntityLoader extends AbstractEntityLoader {
   private final boolean batchLoader;
   private final int[][] compositeKeyManyToOneTargetIndices;

   public EntityLoader(OuterJoinLoadable persister, LockMode lockMode, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(persister, 1, (LockMode)lockMode, factory, loadQueryInfluencers);
   }

   public EntityLoader(OuterJoinLoadable persister, LockOptions lockOptions, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(persister, 1, (LockOptions)lockOptions, factory, loadQueryInfluencers);
   }

   public EntityLoader(OuterJoinLoadable persister, int batchSize, LockMode lockMode, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(persister, persister.getIdentifierColumnNames(), persister.getIdentifierType(), batchSize, lockMode, factory, loadQueryInfluencers);
   }

   public EntityLoader(OuterJoinLoadable persister, int batchSize, LockOptions lockOptions, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      this(persister, persister.getIdentifierColumnNames(), persister.getIdentifierType(), batchSize, lockOptions, factory, loadQueryInfluencers);
   }

   public EntityLoader(OuterJoinLoadable persister, String[] uniqueKey, Type uniqueKeyType, int batchSize, LockMode lockMode, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(persister, uniqueKeyType, factory, loadQueryInfluencers);
      EntityJoinWalker walker = new EntityJoinWalker(persister, uniqueKey, batchSize, lockMode, factory, loadQueryInfluencers);
      this.initFromWalker(walker);
      this.compositeKeyManyToOneTargetIndices = walker.getCompositeKeyManyToOneTargetIndices();
      this.postInstantiate();
      this.batchLoader = batchSize > 1;
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Static select for entity %s [%s]: %s", this.entityName, lockMode, this.getSQLString());
      }

   }

   public EntityLoader(OuterJoinLoadable persister, String[] uniqueKey, Type uniqueKeyType, int batchSize, LockOptions lockOptions, SessionFactoryImplementor factory, LoadQueryInfluencers loadQueryInfluencers) throws MappingException {
      super(persister, uniqueKeyType, factory, loadQueryInfluencers);
      EntityJoinWalker walker = new EntityJoinWalker(persister, uniqueKey, batchSize, lockOptions, factory, loadQueryInfluencers);
      this.initFromWalker(walker);
      this.compositeKeyManyToOneTargetIndices = walker.getCompositeKeyManyToOneTargetIndices();
      this.postInstantiate();
      this.batchLoader = batchSize > 1;
      if (LOG.isDebugEnabled()) {
         LOG.debugf("Static select for entity %s [%s:%s]: %s", new Object[]{this.entityName, lockOptions.getLockMode(), lockOptions.getTimeOut(), this.getSQLString()});
      }

   }

   public Object loadByUniqueKey(SessionImplementor session, Object key) {
      return this.load(session, key, (Object)null, (Serializable)null, LockOptions.NONE);
   }

   protected boolean isSingleRowLoader() {
      return !this.batchLoader;
   }

   public int[][] getCompositeKeyManyToOneTargetIndices() {
      return this.compositeKeyManyToOneTargetIndices;
   }
}
