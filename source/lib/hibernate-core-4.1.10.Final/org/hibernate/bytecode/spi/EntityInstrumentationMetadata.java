package org.hibernate.bytecode.spi;

import java.util.Set;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.engine.spi.SessionImplementor;

public interface EntityInstrumentationMetadata {
   String getEntityName();

   boolean isInstrumented();

   FieldInterceptor injectInterceptor(Object var1, String var2, Set var3, SessionImplementor var4) throws NotInstrumentedException;

   FieldInterceptor extractInterceptor(Object var1) throws NotInstrumentedException;
}
