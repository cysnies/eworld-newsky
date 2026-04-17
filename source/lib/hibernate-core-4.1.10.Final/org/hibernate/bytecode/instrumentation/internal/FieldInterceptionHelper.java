package org.hibernate.bytecode.instrumentation.internal;

import java.util.HashSet;
import java.util.Set;
import org.hibernate.bytecode.instrumentation.internal.javassist.JavassistHelper;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.engine.spi.SessionImplementor;

public class FieldInterceptionHelper {
   private static final Set INSTRUMENTATION_DELEGATES = buildInstrumentationDelegates();

   private static Set buildInstrumentationDelegates() {
      HashSet<Delegate> delegates = new HashSet();
      delegates.add(FieldInterceptionHelper.JavassistDelegate.INSTANCE);
      return delegates;
   }

   private FieldInterceptionHelper() {
   }

   public static boolean isInstrumented(Class entityClass) {
      for(Delegate delegate : INSTRUMENTATION_DELEGATES) {
         if (delegate.isInstrumented(entityClass)) {
            return true;
         }
      }

      return false;
   }

   public static boolean isInstrumented(Object entity) {
      return entity != null && isInstrumented(entity.getClass());
   }

   public static FieldInterceptor extractFieldInterceptor(Object entity) {
      if (entity == null) {
         return null;
      } else {
         FieldInterceptor interceptor = null;

         for(Delegate delegate : INSTRUMENTATION_DELEGATES) {
            interceptor = delegate.extractInterceptor(entity);
            if (interceptor != null) {
               break;
            }
         }

         return interceptor;
      }
   }

   public static FieldInterceptor injectFieldInterceptor(Object entity, String entityName, Set uninitializedFieldNames, SessionImplementor session) {
      if (entity == null) {
         return null;
      } else {
         FieldInterceptor interceptor = null;

         for(Delegate delegate : INSTRUMENTATION_DELEGATES) {
            interceptor = delegate.injectInterceptor(entity, entityName, uninitializedFieldNames, session);
            if (interceptor != null) {
               break;
            }
         }

         return interceptor;
      }
   }

   private static class JavassistDelegate implements Delegate {
      public static final JavassistDelegate INSTANCE = new JavassistDelegate();
      public static final String MARKER = "org.hibernate.bytecode.internal.javassist.FieldHandled";

      public boolean isInstrumented(Class classToCheck) {
         for(Class definedInterface : classToCheck.getInterfaces()) {
            if ("org.hibernate.bytecode.internal.javassist.FieldHandled".equals(definedInterface.getName())) {
               return true;
            }
         }

         return false;
      }

      public FieldInterceptor extractInterceptor(Object entity) {
         for(Class definedInterface : entity.getClass().getInterfaces()) {
            if ("org.hibernate.bytecode.internal.javassist.FieldHandled".equals(definedInterface.getName())) {
               return JavassistHelper.extractFieldInterceptor(entity);
            }
         }

         return null;
      }

      public FieldInterceptor injectInterceptor(Object entity, String entityName, Set uninitializedFieldNames, SessionImplementor session) {
         for(Class definedInterface : entity.getClass().getInterfaces()) {
            if ("org.hibernate.bytecode.internal.javassist.FieldHandled".equals(definedInterface.getName())) {
               return JavassistHelper.injectFieldInterceptor(entity, entityName, uninitializedFieldNames, session);
            }
         }

         return null;
      }
   }

   private interface Delegate {
      boolean isInstrumented(Class var1);

      FieldInterceptor extractInterceptor(Object var1);

      FieldInterceptor injectInterceptor(Object var1, String var2, Set var3, SessionImplementor var4);
   }
}
