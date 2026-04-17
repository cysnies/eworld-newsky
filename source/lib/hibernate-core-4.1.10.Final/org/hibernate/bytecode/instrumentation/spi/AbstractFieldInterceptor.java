package org.hibernate.bytecode.instrumentation.spi;

import java.io.Serializable;
import java.util.Set;
import org.hibernate.LazyInitializationException;
import org.hibernate.engine.spi.SessionImplementor;

public abstract class AbstractFieldInterceptor implements FieldInterceptor, Serializable {
   private transient SessionImplementor session;
   private Set uninitializedFields;
   private final String entityName;
   private transient boolean initializing;
   private boolean dirty;

   protected AbstractFieldInterceptor(SessionImplementor session, Set uninitializedFields, String entityName) {
      this.session = session;
      this.uninitializedFields = uninitializedFields;
      this.entityName = entityName;
   }

   public final void setSession(SessionImplementor session) {
      this.session = session;
   }

   public final boolean isInitialized() {
      return this.uninitializedFields == null || this.uninitializedFields.size() == 0;
   }

   public final boolean isInitialized(String field) {
      return this.uninitializedFields == null || !this.uninitializedFields.contains(field);
   }

   public final void dirty() {
      this.dirty = true;
   }

   public final boolean isDirty() {
      return this.dirty;
   }

   public final void clearDirty() {
      this.dirty = false;
   }

   protected final Object intercept(Object target, String fieldName, Object value) {
      if (this.initializing) {
         return value;
      } else if (this.uninitializedFields != null && this.uninitializedFields.contains(fieldName)) {
         if (this.session == null) {
            throw new LazyInitializationException("entity with lazy properties is not associated with a session");
         } else if (this.session.isOpen() && this.session.isConnected()) {
            this.initializing = true;

            Object result;
            try {
               result = ((LazyPropertyInitializer)this.session.getFactory().getEntityPersister(this.entityName)).initializeLazyProperty(fieldName, target, this.session);
            } finally {
               this.initializing = false;
            }

            this.uninitializedFields = null;
            return result;
         } else {
            throw new LazyInitializationException("session is not connected");
         }
      } else {
         return value;
      }
   }

   public final SessionImplementor getSession() {
      return this.session;
   }

   public final Set getUninitializedFields() {
      return this.uninitializedFields;
   }

   public final String getEntityName() {
      return this.entityName;
   }

   public final boolean isInitializing() {
      return this.initializing;
   }
}
