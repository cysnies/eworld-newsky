package org.hibernate.bytecode.instrumentation.spi;

import java.io.Serializable;
import org.hibernate.engine.spi.SessionImplementor;

public interface LazyPropertyInitializer {
   Serializable UNFETCHED_PROPERTY = new Serializable() {
      public String toString() {
         return "<lazy>";
      }

      public Object readResolve() {
         return LazyPropertyInitializer.UNFETCHED_PROPERTY;
      }
   };

   Object initializeLazyProperty(String var1, Object var2, SessionImplementor var3);
}
