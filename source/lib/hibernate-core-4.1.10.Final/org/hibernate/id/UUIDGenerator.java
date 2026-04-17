package org.hibernate.id;

import java.io.Serializable;
import java.util.Properties;
import java.util.UUID;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.uuid.StandardRandomStrategy;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.type.Type;
import org.hibernate.type.descriptor.java.UUIDTypeDescriptor;
import org.jboss.logging.Logger;

public class UUIDGenerator implements IdentifierGenerator, Configurable {
   public static final String UUID_GEN_STRATEGY = "uuid_gen_strategy";
   public static final String UUID_GEN_STRATEGY_CLASS = "uuid_gen_strategy_class";
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, UUIDGenerator.class.getName());
   private UUIDGenerationStrategy strategy;
   private UUIDTypeDescriptor.ValueTransformer valueTransformer;

   public static UUIDGenerator buildSessionFactoryUniqueIdentifierGenerator() {
      UUIDGenerator generator = new UUIDGenerator();
      generator.strategy = StandardRandomStrategy.INSTANCE;
      generator.valueTransformer = UUIDTypeDescriptor.ToStringTransformer.INSTANCE;
      return generator;
   }

   public void configure(Type type, Properties params, Dialect d) throws MappingException {
      this.strategy = (UUIDGenerationStrategy)params.get("uuid_gen_strategy");
      if (this.strategy == null) {
         String strategyClassName = params.getProperty("uuid_gen_strategy_class");
         if (strategyClassName != null) {
            try {
               Class strategyClass = ReflectHelper.classForName(strategyClassName);

               try {
                  this.strategy = (UUIDGenerationStrategy)strategyClass.newInstance();
               } catch (Exception ignore) {
                  LOG.unableToInstantiateUuidGenerationStrategy(ignore);
               }
            } catch (ClassNotFoundException var8) {
               LOG.unableToLocateUuidGenerationStrategy(strategyClassName);
            }
         }
      }

      if (this.strategy == null) {
         this.strategy = StandardRandomStrategy.INSTANCE;
      }

      if (UUID.class.isAssignableFrom(type.getReturnedClass())) {
         this.valueTransformer = UUIDTypeDescriptor.PassThroughTransformer.INSTANCE;
      } else if (String.class.isAssignableFrom(type.getReturnedClass())) {
         this.valueTransformer = UUIDTypeDescriptor.ToStringTransformer.INSTANCE;
      } else {
         if (!byte[].class.isAssignableFrom(type.getReturnedClass())) {
            throw new HibernateException("Unanticipated return type [" + type.getReturnedClass().getName() + "] for UUID conversion");
         }

         this.valueTransformer = UUIDTypeDescriptor.ToBytesTransformer.INSTANCE;
      }

   }

   public Serializable generate(SessionImplementor session, Object object) throws HibernateException {
      return this.valueTransformer.transform(this.strategy.generateUUID(session));
   }
}
