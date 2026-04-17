package org.hibernate.bytecode.instrumentation.internal.javassist;

import java.util.Set;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.internal.javassist.FieldHandled;
import org.hibernate.engine.spi.SessionImplementor;

public class JavassistHelper {
   private JavassistHelper() {
   }

   public static FieldInterceptor extractFieldInterceptor(Object entity) {
      return (FieldInterceptor)((FieldHandled)entity).getFieldHandler();
   }

   public static FieldInterceptor injectFieldInterceptor(Object entity, String entityName, Set uninitializedFieldNames, SessionImplementor session) {
      FieldInterceptorImpl fieldInterceptor = new FieldInterceptorImpl(session, uninitializedFieldNames, entityName);
      ((FieldHandled)entity).setFieldHandler(fieldInterceptor);
      return fieldInterceptor;
   }
}
