package org.hibernate.cfg.beanvalidation;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import org.hibernate.HibernateException;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.classloading.spi.ClassLoaderService;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.jboss.logging.Logger;

public class BeanValidationIntegrator implements Integrator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BeanValidationIntegrator.class.getName());
   public static final String APPLY_CONSTRAINTS = "hibernate.validator.apply_to_ddl";
   public static final String BV_CHECK_CLASS = "javax.validation.Validation";
   public static final String MODE_PROPERTY = "javax.persistence.validation.mode";
   private static final String ACTIVATOR_CLASS = "org.hibernate.cfg.beanvalidation.TypeSafeActivator";
   private static final String DDL_METHOD = "applyDDL";
   private static final String ACTIVATE_METHOD = "activateBeanValidation";
   private static final String VALIDATE_METHOD = "validateFactory";

   public static void validateFactory(Object object) {
      try {
         Class activatorClass = BeanValidationIntegrator.class.getClassLoader().loadClass("org.hibernate.cfg.beanvalidation.TypeSafeActivator");

         try {
            Method validateMethod = activatorClass.getMethod("validateFactory", Object.class);
            if (!validateMethod.isAccessible()) {
               validateMethod.setAccessible(true);
            }

            try {
               validateMethod.invoke((Object)null, object);
            } catch (InvocationTargetException e) {
               if (e.getTargetException() instanceof HibernateException) {
                  throw (HibernateException)e.getTargetException();
               } else {
                  throw new HibernateException("Unable to check validity of passed ValidatorFactory", e);
               }
            } catch (IllegalAccessException e) {
               throw new HibernateException("Unable to check validity of passed ValidatorFactory", e);
            }
         } catch (HibernateException e) {
            throw e;
         } catch (Exception e) {
            throw new HibernateException("Could not locate method needed for ValidatorFactory validation", e);
         }
      } catch (HibernateException e) {
         throw e;
      } catch (Exception e) {
         throw new HibernateException("Could not locate TypeSafeActivator class", e);
      }
   }

   public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
      Set<ValidationMode> modes = BeanValidationIntegrator.ValidationMode.getModes(configuration.getProperties().get("javax.persistence.validation.mode"));
      ClassLoaderService classLoaderService = (ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class);
      Dialect dialect = ((JdbcServices)serviceRegistry.getService(JdbcServices.class)).getDialect();

      boolean isBeanValidationAvailable;
      try {
         classLoaderService.classForName("javax.validation.Validation");
         isBeanValidationAvailable = true;
      } catch (Exception var9) {
         isBeanValidationAvailable = false;
      }

      Class typeSafeActivatorClass = this.loadTypeSafeActivatorClass(serviceRegistry);
      this.applyRelationalConstraints(modes, isBeanValidationAvailable, typeSafeActivatorClass, configuration, dialect);
      this.applyHibernateListeners(modes, isBeanValidationAvailable, typeSafeActivatorClass, configuration, sessionFactory, serviceRegistry);
   }

   public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
   }

   private Class loadTypeSafeActivatorClass(SessionFactoryServiceRegistry serviceRegistry) {
      try {
         return ((ClassLoaderService)serviceRegistry.getService(ClassLoaderService.class)).classForName("org.hibernate.cfg.beanvalidation.TypeSafeActivator");
      } catch (Exception var3) {
         return null;
      }
   }

   private void applyRelationalConstraints(Set modes, boolean beanValidationAvailable, Class typeSafeActivatorClass, Configuration configuration, Dialect dialect) {
      if (!ConfigurationHelper.getBoolean("hibernate.validator.apply_to_ddl", configuration.getProperties(), true)) {
         LOG.debug("Skipping application of relational constraints from legacy Hibernate Validator");
      } else if (modes.contains(BeanValidationIntegrator.ValidationMode.DDL) || modes.contains(BeanValidationIntegrator.ValidationMode.AUTO)) {
         if (!beanValidationAvailable) {
            if (modes.contains(BeanValidationIntegrator.ValidationMode.DDL)) {
               throw new HibernateException("Bean Validation not available in the class path but required in javax.persistence.validation.mode");
            }

            if (modes.contains(BeanValidationIntegrator.ValidationMode.AUTO)) {
               return;
            }
         }

         try {
            Method applyDDLMethod = typeSafeActivatorClass.getMethod("applyDDL", Collection.class, Properties.class, Dialect.class);

            try {
               applyDDLMethod.invoke((Object)null, configuration.createMappings().getClasses().values(), configuration.getProperties(), dialect);
            } catch (HibernateException e) {
               throw e;
            } catch (Exception e) {
               throw new HibernateException("Error applying BeanValidation relational constraints", e);
            }
         } catch (HibernateException e) {
            throw e;
         } catch (Exception e) {
            throw new HibernateException("Unable to locate TypeSafeActivator#applyDDL method", e);
         }
      }
   }

   private void applyHibernateListeners(Set modes, boolean beanValidationAvailable, Class typeSafeActivatorClass, Configuration configuration, SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
      if (configuration.getProperty("hibernate.check_nullability") == null) {
         sessionFactory.getSettings().setCheckNullability(false);
      }

      if (modes.contains(BeanValidationIntegrator.ValidationMode.CALLBACK) || modes.contains(BeanValidationIntegrator.ValidationMode.AUTO)) {
         if (!beanValidationAvailable) {
            if (modes.contains(BeanValidationIntegrator.ValidationMode.CALLBACK)) {
               throw new HibernateException("Bean Validation not available in the class path but required in javax.persistence.validation.mode");
            }

            if (modes.contains(BeanValidationIntegrator.ValidationMode.AUTO)) {
               return;
            }
         }

         try {
            Method activateMethod = typeSafeActivatorClass.getMethod("activateBeanValidation", EventListenerRegistry.class, Configuration.class);

            try {
               activateMethod.invoke((Object)null, serviceRegistry.getService(EventListenerRegistry.class), configuration);
            } catch (HibernateException e) {
               throw e;
            } catch (Exception e) {
               throw new HibernateException("Error applying BeanValidation relational constraints", e);
            }
         } catch (HibernateException e) {
            throw e;
         } catch (Exception e) {
            throw new HibernateException("Unable to locate TypeSafeActivator#applyDDL method", e);
         }
      }
   }

   public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
   }

   private static enum ValidationMode {
      AUTO,
      CALLBACK,
      NONE,
      DDL;

      public static Set getModes(Object modeProperty) {
         Set<ValidationMode> modes = new HashSet(3);
         if (modeProperty == null) {
            modes.add(AUTO);
         } else {
            String[] modesInString = modeProperty.toString().split(",");

            for(String modeInString : modesInString) {
               modes.add(getMode(modeInString));
            }
         }

         if (modes.size() > 1 && (modes.contains(AUTO) || modes.contains(NONE))) {
            StringBuilder message = new StringBuilder("Incompatible validation modes mixed: ");

            for(ValidationMode mode : modes) {
               message.append(mode).append(", ");
            }

            throw new HibernateException(message.substring(0, message.length() - 2));
         } else {
            return modes;
         }
      }

      private static ValidationMode getMode(String modeProperty) {
         if (modeProperty != null && modeProperty.length() != 0) {
            try {
               return valueOf(modeProperty.trim().toUpperCase());
            } catch (IllegalArgumentException var2) {
               throw new HibernateException("Unknown validation mode in javax.persistence.validation.mode: " + modeProperty);
            }
         } else {
            return AUTO;
         }
      }
   }
}
