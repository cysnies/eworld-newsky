package org.hibernate.cfg.beanvalidation;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.TraversableResolver;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.EntityMode;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.spi.PreDeleteEvent;
import org.hibernate.event.spi.PreDeleteEventListener;
import org.hibernate.event.spi.PreInsertEvent;
import org.hibernate.event.spi.PreInsertEventListener;
import org.hibernate.event.spi.PreUpdateEvent;
import org.hibernate.event.spi.PreUpdateEventListener;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.persister.entity.EntityPersister;
import org.jboss.logging.Logger;

public class BeanValidationEventListener implements PreInsertEventListener, PreUpdateEventListener, PreDeleteEventListener {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, BeanValidationEventListener.class.getName());
   private ValidatorFactory factory;
   private ConcurrentHashMap associationsPerEntityPersister = new ConcurrentHashMap();
   private GroupsPerOperation groupsPerOperation;
   boolean initialized;

   public BeanValidationEventListener() {
   }

   public BeanValidationEventListener(ValidatorFactory factory, Properties properties) {
      this.init(factory, properties);
   }

   public void initialize(Configuration cfg) {
      if (!this.initialized) {
         ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
         Properties props = cfg.getProperties();
         this.init(factory, props);
      }

   }

   public boolean onPreInsert(PreInsertEvent event) {
      this.validate(event.getEntity(), event.getPersister().getEntityMode(), event.getPersister(), event.getSession().getFactory(), GroupsPerOperation.Operation.INSERT);
      return false;
   }

   public boolean onPreUpdate(PreUpdateEvent event) {
      this.validate(event.getEntity(), event.getPersister().getEntityMode(), event.getPersister(), event.getSession().getFactory(), GroupsPerOperation.Operation.UPDATE);
      return false;
   }

   public boolean onPreDelete(PreDeleteEvent event) {
      this.validate(event.getEntity(), event.getPersister().getEntityMode(), event.getPersister(), event.getSession().getFactory(), GroupsPerOperation.Operation.DELETE);
      return false;
   }

   private void init(ValidatorFactory factory, Properties properties) {
      this.factory = factory;
      this.groupsPerOperation = new GroupsPerOperation(properties);
      this.initialized = true;
   }

   private void validate(Object object, EntityMode mode, EntityPersister persister, SessionFactoryImplementor sessionFactory, GroupsPerOperation.Operation operation) {
      if (object != null && mode == EntityMode.POJO) {
         TraversableResolver tr = new HibernateTraversableResolver(persister, this.associationsPerEntityPersister, sessionFactory);
         Validator validator = this.factory.usingContext().traversableResolver(tr).getValidator();
         Class<?>[] groups = this.groupsPerOperation.get(operation);
         if (groups.length > 0) {
            Set<ConstraintViolation<T>> constraintViolations = validator.validate(object, groups);
            if (constraintViolations.size() > 0) {
               Set<ConstraintViolation<?>> propagatedViolations = new HashSet(constraintViolations.size());
               Set<String> classNames = new HashSet();

               for(ConstraintViolation violation : constraintViolations) {
                  LOG.trace(violation);
                  propagatedViolations.add(violation);
                  classNames.add(violation.getLeafBean().getClass().getName());
               }

               StringBuilder builder = new StringBuilder();
               builder.append("Validation failed for classes ");
               builder.append(classNames);
               builder.append(" during ");
               builder.append(operation.getName());
               builder.append(" time for groups ");
               builder.append(this.toString(groups));
               builder.append("\nList of constraint violations:[\n");

               for(ConstraintViolation violation : constraintViolations) {
                  builder.append("\t").append(violation.toString()).append("\n");
               }

               builder.append("]");
               throw new ConstraintViolationException(builder.toString(), propagatedViolations);
            }
         }

      }
   }

   private String toString(Class[] groups) {
      StringBuilder toString = new StringBuilder("[");

      for(Class group : groups) {
         toString.append(group.getName()).append(", ");
      }

      toString.append("]");
      return toString.toString();
   }
}
