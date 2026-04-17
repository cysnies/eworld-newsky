package org.hibernate.tuple.entity;

import java.util.Set;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.spi.EntityInstrumentationMetadata;
import org.hibernate.bytecode.spi.NotInstrumentedException;
import org.hibernate.engine.spi.SessionImplementor;

public class NonPojoInstrumentationMetadata implements EntityInstrumentationMetadata {
   private final String entityName;
   private final String errorMsg;

   public NonPojoInstrumentationMetadata(String entityName) {
      this.entityName = entityName;
      this.errorMsg = "Entity [" + entityName + "] is non-pojo, and therefore not instrumented";
   }

   public String getEntityName() {
      return this.entityName;
   }

   public boolean isInstrumented() {
      return false;
   }

   public FieldInterceptor extractInterceptor(Object entity) throws NotInstrumentedException {
      throw new NotInstrumentedException(this.errorMsg);
   }

   public FieldInterceptor injectInterceptor(Object entity, String entityName, Set uninitializedFieldNames, SessionImplementor session) throws NotInstrumentedException {
      throw new NotInstrumentedException(this.errorMsg);
   }
}
