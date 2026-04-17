package org.hibernate.engine.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.hibernate.AssertionFailure;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;

public final class EntityKey implements Serializable {
   private final Serializable identifier;
   private final String entityName;
   private final String rootEntityName;
   private final String tenantId;
   private final int hashCode;
   private final Type identifierType;
   private final boolean isBatchLoadable;
   private final SessionFactoryImplementor factory;

   public EntityKey(Serializable id, EntityPersister persister, String tenantId) {
      if (id == null) {
         throw new AssertionFailure("null identifier");
      } else {
         this.identifier = id;
         this.rootEntityName = persister.getRootEntityName();
         this.entityName = persister.getEntityName();
         this.tenantId = tenantId;
         this.identifierType = persister.getIdentifierType();
         this.isBatchLoadable = persister.isBatchLoadable();
         this.factory = persister.getFactory();
         this.hashCode = this.generateHashCode();
      }
   }

   private EntityKey(Serializable identifier, String rootEntityName, String entityName, Type identifierType, boolean batchLoadable, SessionFactoryImplementor factory, String tenantId) {
      this.identifier = identifier;
      this.rootEntityName = rootEntityName;
      this.entityName = entityName;
      this.identifierType = identifierType;
      this.isBatchLoadable = batchLoadable;
      this.factory = factory;
      this.tenantId = tenantId;
      this.hashCode = this.generateHashCode();
   }

   private int generateHashCode() {
      int result = 17;
      result = 37 * result + this.rootEntityName.hashCode();
      result = 37 * result + this.identifierType.getHashCode(this.identifier, this.factory);
      return result;
   }

   public boolean isBatchLoadable() {
      return this.isBatchLoadable;
   }

   public Serializable getIdentifier() {
      return this.identifier;
   }

   public String getEntityName() {
      return this.entityName;
   }

   public boolean equals(Object other) {
      EntityKey otherKey = (EntityKey)other;
      return otherKey.rootEntityName.equals(this.rootEntityName) && this.identifierType.isEqual(otherKey.identifier, this.identifier, this.factory) && EqualsHelper.equals(this.tenantId, otherKey.tenantId);
   }

   public int hashCode() {
      return this.hashCode;
   }

   public String toString() {
      return "EntityKey" + MessageHelper.infoString((EntityPersister)this.factory.getEntityPersister(this.entityName), (Object)this.identifier, (SessionFactoryImplementor)this.factory);
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      oos.writeObject(this.identifier);
      oos.writeObject(this.rootEntityName);
      oos.writeObject(this.entityName);
      oos.writeObject(this.identifierType);
      oos.writeBoolean(this.isBatchLoadable);
      oos.writeObject(this.tenantId);
   }

   public static EntityKey deserialize(ObjectInputStream ois, SessionImplementor session) throws IOException, ClassNotFoundException {
      return new EntityKey((Serializable)ois.readObject(), (String)ois.readObject(), (String)ois.readObject(), (Type)ois.readObject(), ois.readBoolean(), session == null ? null : session.getFactory(), (String)ois.readObject());
   }
}
