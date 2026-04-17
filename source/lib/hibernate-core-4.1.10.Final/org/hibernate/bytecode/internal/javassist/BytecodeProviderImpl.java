package org.hibernate.bytecode.internal.javassist;

import java.lang.reflect.Modifier;
import java.util.Set;
import org.hibernate.bytecode.buildtime.spi.ClassFilter;
import org.hibernate.bytecode.instrumentation.internal.javassist.JavassistHelper;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.spi.BytecodeProvider;
import org.hibernate.bytecode.spi.ClassTransformer;
import org.hibernate.bytecode.spi.EntityInstrumentationMetadata;
import org.hibernate.bytecode.spi.NotInstrumentedException;
import org.hibernate.bytecode.spi.ProxyFactoryFactory;
import org.hibernate.bytecode.spi.ReflectionOptimizer;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.StringHelper;
import org.jboss.logging.Logger;

public class BytecodeProviderImpl implements BytecodeProvider {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BytecodeProviderImpl.class.getName());

   public ProxyFactoryFactory getProxyFactoryFactory() {
      return new ProxyFactoryFactoryImpl();
   }

   public ReflectionOptimizer getReflectionOptimizer(Class clazz, String[] getterNames, String[] setterNames, Class[] types) {
      FastClass fastClass;
      BulkAccessor bulkAccessor;
      try {
         fastClass = FastClass.create(clazz);
         bulkAccessor = BulkAccessor.create(clazz, getterNames, setterNames, types);
         if (!clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers())) {
            if (fastClass == null) {
               bulkAccessor = null;
            } else {
               Object instance = fastClass.newInstance();
               bulkAccessor.setPropertyValues(instance, bulkAccessor.getPropertyValues(instance));
            }
         }
      } catch (Throwable var9) {
         fastClass = null;
         bulkAccessor = null;
         if (LOG.isDebugEnabled()) {
            int index = 0;
            if (var9 instanceof BulkAccessorException) {
               index = ((BulkAccessorException)var9).getIndex();
            }

            if (index >= 0) {
               LOG.debugf("Reflection optimizer disabled for: %s [%s: %s (property %s)", new Object[]{clazz.getName(), StringHelper.unqualify(var9.getClass().getName()), var9.getMessage(), setterNames[index]});
            } else {
               LOG.debugf("Reflection optimizer disabled for: %s [%s: %s", clazz.getName(), StringHelper.unqualify(var9.getClass().getName()), var9.getMessage());
            }
         }
      }

      return fastClass != null && bulkAccessor != null ? new ReflectionOptimizerImpl(new InstantiationOptimizerAdapter(fastClass), new AccessOptimizerAdapter(bulkAccessor, clazz)) : null;
   }

   public ClassTransformer getTransformer(ClassFilter classFilter, org.hibernate.bytecode.buildtime.spi.FieldFilter fieldFilter) {
      return new JavassistClassTransformer(classFilter, fieldFilter);
   }

   public EntityInstrumentationMetadata getEntityInstrumentationMetadata(Class entityClass) {
      return new EntityInstrumentationMetadataImpl(entityClass);
   }

   private class EntityInstrumentationMetadataImpl implements EntityInstrumentationMetadata {
      private final Class entityClass;
      private final boolean isInstrumented;

      private EntityInstrumentationMetadataImpl(Class entityClass) {
         this.entityClass = entityClass;
         this.isInstrumented = FieldHandled.class.isAssignableFrom(entityClass);
      }

      public String getEntityName() {
         return this.entityClass.getName();
      }

      public boolean isInstrumented() {
         return this.isInstrumented;
      }

      public FieldInterceptor extractInterceptor(Object entity) throws NotInstrumentedException {
         if (!this.entityClass.isInstance(entity)) {
            throw new IllegalArgumentException(String.format("Passed entity instance [%s] is not of expected type [%s]", entity, this.getEntityName()));
         } else if (!this.isInstrumented()) {
            throw new NotInstrumentedException(String.format("Entity class [%s] is not instrumented", this.getEntityName()));
         } else {
            return JavassistHelper.extractFieldInterceptor(entity);
         }
      }

      public FieldInterceptor injectInterceptor(Object entity, String entityName, Set uninitializedFieldNames, SessionImplementor session) throws NotInstrumentedException {
         if (!this.entityClass.isInstance(entity)) {
            throw new IllegalArgumentException(String.format("Passed entity instance [%s] is not of expected type [%s]", entity, this.getEntityName()));
         } else if (!this.isInstrumented()) {
            throw new NotInstrumentedException(String.format("Entity class [%s] is not instrumented", this.getEntityName()));
         } else {
            return JavassistHelper.injectFieldInterceptor(entity, entityName, uninitializedFieldNames, session);
         }
      }
   }
}
