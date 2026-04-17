package org.hibernate.cache.spi;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Arrays;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ValueHolder;
import org.hibernate.internal.util.compare.EqualsHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

public class NaturalIdCacheKey implements Serializable {
   private final Serializable[] naturalIdValues;
   private final String entityName;
   private final String tenantId;
   private final int hashCode;
   private transient ValueHolder toString;

   public NaturalIdCacheKey(Object[] naturalIdValues, EntityPersister persister, SessionImplementor session) {
      this.entityName = persister.getRootEntityName();
      this.tenantId = session.getTenantIdentifier();
      this.naturalIdValues = new Serializable[naturalIdValues.length];
      SessionFactoryImplementor factory = session.getFactory();
      int[] naturalIdPropertyIndexes = persister.getNaturalIdentifierProperties();
      Type[] propertyTypes = persister.getPropertyTypes();
      int prime = 31;
      int result = 1;
      result = 31 * result + (this.entityName == null ? 0 : this.entityName.hashCode());
      result = 31 * result + (this.tenantId == null ? 0 : this.tenantId.hashCode());

      for(int i = 0; i < naturalIdValues.length; ++i) {
         int naturalIdPropertyIndex = naturalIdPropertyIndexes[i];
         Type type = propertyTypes[naturalIdPropertyIndex];
         Object value = naturalIdValues[i];
         result = 31 * result + (value != null ? type.getHashCode(value, factory) : 0);
         this.naturalIdValues[i] = type.disassemble(value, session, (Object)null);
      }

      this.hashCode = result;
      this.initTransients();
   }

   private void initTransients() {
      this.toString = new ValueHolder(new ValueHolder.DeferredInitializer() {
         public String initialize() {
            StringBuilder toStringBuilder = (new StringBuilder(NaturalIdCacheKey.this.entityName)).append("##NaturalId[");

            for(int i = 0; i < NaturalIdCacheKey.this.naturalIdValues.length; ++i) {
               toStringBuilder.append(NaturalIdCacheKey.this.naturalIdValues[i]);
               if (i + 1 < NaturalIdCacheKey.this.naturalIdValues.length) {
                  toStringBuilder.append(", ");
               }
            }

            toStringBuilder.append("]");
            return toStringBuilder.toString();
         }
      });
   }

   public String getEntityName() {
      return this.entityName;
   }

   public String getTenantId() {
      return this.tenantId;
   }

   public Serializable[] getNaturalIdValues() {
      return this.naturalIdValues;
   }

   public String toString() {
      return (String)this.toString.getValue();
   }

   public int hashCode() {
      return this.hashCode;
   }

   public boolean equals(Object o) {
      if (o == null) {
         return false;
      } else if (this == o) {
         return true;
      } else if (this.hashCode == o.hashCode() && o instanceof NaturalIdCacheKey) {
         NaturalIdCacheKey other = (NaturalIdCacheKey)o;
         return EqualsHelper.equals(this.entityName, other.entityName) && EqualsHelper.equals(this.tenantId, other.tenantId) && Arrays.deepEquals(this.naturalIdValues, other.naturalIdValues);
      } else {
         return false;
      }
   }

   private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
      ois.defaultReadObject();
      this.initTransients();
   }
}
