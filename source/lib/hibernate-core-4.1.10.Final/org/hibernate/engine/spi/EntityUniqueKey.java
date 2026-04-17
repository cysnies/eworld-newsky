package org.hibernate.engine.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.hibernate.EntityMode;
import org.hibernate.pretty.MessageHelper;
import org.hibernate.type.Type;

public class EntityUniqueKey implements Serializable {
   private final String uniqueKeyName;
   private final String entityName;
   private final Object key;
   private final Type keyType;
   private final EntityMode entityMode;
   private final int hashCode;

   public EntityUniqueKey(String entityName, String uniqueKeyName, Object semiResolvedKey, Type keyType, EntityMode entityMode, SessionFactoryImplementor factory) {
      this.uniqueKeyName = uniqueKeyName;
      this.entityName = entityName;
      this.key = semiResolvedKey;
      this.keyType = keyType.getSemiResolvedType(factory);
      this.entityMode = entityMode;
      this.hashCode = this.generateHashCode(factory);
   }

   public String getEntityName() {
      return this.entityName;
   }

   public Object getKey() {
      return this.key;
   }

   public String getUniqueKeyName() {
      return this.uniqueKeyName;
   }

   public int generateHashCode(SessionFactoryImplementor factory) {
      int result = 17;
      result = 37 * result + this.entityName.hashCode();
      result = 37 * result + this.uniqueKeyName.hashCode();
      result = 37 * result + this.keyType.getHashCode(this.key, factory);
      return result;
   }

   public int hashCode() {
      return this.hashCode;
   }

   public boolean equals(Object other) {
      EntityUniqueKey that = (EntityUniqueKey)other;
      return that.entityName.equals(this.entityName) && that.uniqueKeyName.equals(this.uniqueKeyName) && this.keyType.isEqual(that.key, this.key);
   }

   public String toString() {
      return "EntityUniqueKey" + MessageHelper.infoString(this.entityName, this.uniqueKeyName, this.key);
   }

   private void writeObject(ObjectOutputStream oos) throws IOException {
      this.checkAbilityToSerialize();
      oos.defaultWriteObject();
   }

   private void checkAbilityToSerialize() {
      if (this.key != null && !Serializable.class.isAssignableFrom(this.key.getClass())) {
         throw new IllegalStateException("Cannot serialize an EntityUniqueKey which represents a non serializable property value [" + this.entityName + "." + this.uniqueKeyName + "]");
      }
   }

   public void serialize(ObjectOutputStream oos) throws IOException {
      this.checkAbilityToSerialize();
      oos.writeObject(this.uniqueKeyName);
      oos.writeObject(this.entityName);
      oos.writeObject(this.key);
      oos.writeObject(this.keyType);
      oos.writeObject(this.entityMode);
   }

   public static EntityUniqueKey deserialize(ObjectInputStream ois, SessionImplementor session) throws IOException, ClassNotFoundException {
      return new EntityUniqueKey((String)ois.readObject(), (String)ois.readObject(), ois.readObject(), (Type)ois.readObject(), (EntityMode)ois.readObject(), session.getFactory());
   }
}
