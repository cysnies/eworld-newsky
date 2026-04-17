package org.hibernate.engine.internal;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.engine.spi.SessionImplementor;

public class NonNullableTransientDependencies {
   private final Map propertyPathsByTransientEntity = new IdentityHashMap();

   void add(String propertyName, Object transientEntity) {
      Set<String> propertyPaths = (Set)this.propertyPathsByTransientEntity.get(transientEntity);
      if (propertyPaths == null) {
         propertyPaths = new HashSet();
         this.propertyPathsByTransientEntity.put(transientEntity, propertyPaths);
      }

      propertyPaths.add(propertyName);
   }

   public Iterable getNonNullableTransientEntities() {
      return this.propertyPathsByTransientEntity.keySet();
   }

   public Iterable getNonNullableTransientPropertyPaths(Object entity) {
      return (Iterable)this.propertyPathsByTransientEntity.get(entity);
   }

   public boolean isEmpty() {
      return this.propertyPathsByTransientEntity.isEmpty();
   }

   public void resolveNonNullableTransientEntity(Object entity) {
      if (this.propertyPathsByTransientEntity.remove(entity) == null) {
         throw new IllegalStateException("Attempt to resolve a non-nullable, transient entity that is not a dependency.");
      }
   }

   public String toLoggableString(SessionImplementor session) {
      StringBuilder sb = (new StringBuilder(this.getClass().getSimpleName())).append('[');

      for(Map.Entry entry : this.propertyPathsByTransientEntity.entrySet()) {
         sb.append("transientEntityName=").append(session.bestGuessEntityName(entry.getKey()));
         sb.append(" requiredBy=").append(entry.getValue());
      }

      sb.append(']');
      return sb.toString();
   }
}
