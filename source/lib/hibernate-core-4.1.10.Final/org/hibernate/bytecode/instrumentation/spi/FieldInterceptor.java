package org.hibernate.bytecode.instrumentation.spi;

import org.hibernate.engine.spi.SessionImplementor;

public interface FieldInterceptor {
   void setSession(SessionImplementor var1);

   boolean isInitialized();

   boolean isInitialized(String var1);

   void dirty();

   boolean isDirty();

   void clearDirty();
}
