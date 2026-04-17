package org.hibernate.cfg.beanvalidation;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.metadata.BeanDescriptor;
import javax.validation.metadata.ConstraintDescriptor;
import javax.validation.metadata.PropertyDescriptor;
import org.hibernate.AssertionFailure;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Dialect;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.internal.util.StringHelper;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.SingleTableSubclass;
import org.jboss.logging.Logger;

class TypeSafeActivator {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TypeSafeActivator.class.getName());
   private static final String FACTORY_PROPERTY = "javax.persistence.validation.factory";

   public static void validateFactory(Object object) {
      if (!ValidatorFactory.class.isInstance(object)) {
         throw new HibernateException("Given object was not an instance of " + ValidatorFactory.class.getName() + "[" + object.getClass().getName() + "]");
      }
   }

   public static void activateBeanValidation(EventListenerRegistry listenerRegistry, Configuration configuration) {
      Properties properties = configuration.getProperties();
      ValidatorFactory factory = getValidatorFactory(properties);
      BeanValidationEventListener listener = new BeanValidationEventListener(factory, properties);
      listenerRegistry.addDuplicationStrategy(DuplicationStrategyImpl.INSTANCE);
      listenerRegistry.appendListeners(EventType.PRE_INSERT, (Object[])(listener));
      listenerRegistry.appendListeners(EventType.PRE_UPDATE, (Object[])(listener));
      listenerRegistry.appendListeners(EventType.PRE_DELETE, (Object[])(listener));
      listener.initialize(configuration);
   }

   public static void applyDDL(Collection persistentClasses, Properties properties, Dialect dialect) {
      ValidatorFactory factory = getValidatorFactory(properties);
      Class<?>[] groupsArray = (new GroupsPerOperation(properties)).get(GroupsPerOperation.Operation.DDL);
      Set<Class<?>> groups = new HashSet(Arrays.asList(groupsArray));

      for(PersistentClass persistentClass : persistentClasses) {
         String className = persistentClass.getClassName();
         if (className != null && className.length() != 0) {
            Class<?> clazz;
            try {
               clazz = ReflectHelper.classForName(className, TypeSafeActivator.class);
            } catch (ClassNotFoundException e) {
               throw new AssertionFailure("Entity class not found", e);
            }

            try {
               applyDDL("", persistentClass, clazz, factory, groups, true, dialect);
            } catch (Exception e) {
               LOG.unableToApplyConstraints(className, e);
            }
         }
      }

   }

   private static void applyDDL(String prefix, PersistentClass persistentClass, Class clazz, ValidatorFactory factory, Set groups, boolean activateNotNull, Dialect dialect) {
      BeanDescriptor descriptor = factory.getValidator().getConstraintsForClass(clazz);

      for(PropertyDescriptor propertyDesc : descriptor.getConstrainedProperties()) {
         Property property = findPropertyByName(persistentClass, prefix + propertyDesc.getPropertyName());
         if (property != null) {
            boolean hasNotNull = applyConstraints(propertyDesc.getConstraintDescriptors(), property, propertyDesc, groups, activateNotNull, dialect);
            if (property.isComposite() && propertyDesc.isCascaded()) {
               Class<?> componentClass = ((Component)property.getValue()).getComponentClass();
               boolean canSetNotNullOnColumns = activateNotNull && hasNotNull;
               applyDDL(prefix + propertyDesc.getPropertyName() + ".", persistentClass, componentClass, factory, groups, canSetNotNullOnColumns, dialect);
            }
         }
      }

   }

   private static boolean applyConstraints(Set constraintDescriptors, Property property, PropertyDescriptor propertyDesc, Set groups, boolean canApplyNotNull, Dialect dialect) {
      boolean hasNotNull = false;

      for(ConstraintDescriptor descriptor : constraintDescriptors) {
         if (groups == null || !Collections.disjoint(descriptor.getGroups(), groups)) {
            if (canApplyNotNull) {
               hasNotNull = hasNotNull || applyNotNull(property, descriptor);
            }

            applyDigits(property, descriptor);
            applySize(property, descriptor, propertyDesc);
            applyMin(property, descriptor, dialect);
            applyMax(property, descriptor, dialect);
            applyLength(property, descriptor, propertyDesc);
            hasNotNull = hasNotNull || applyConstraints(descriptor.getComposingConstraints(), property, propertyDesc, (Set)null, canApplyNotNull, dialect);
         }
      }

      return hasNotNull;
   }

   private static void applyMin(Property property, ConstraintDescriptor descriptor, Dialect dialect) {
      if (Min.class.equals(descriptor.getAnnotation().annotationType())) {
         long min = ((Min)descriptor.getAnnotation()).value();
         Column col = (Column)property.getColumnIterator().next();
         String checkConstraint = col.getQuotedName(dialect) + ">=" + min;
         applySQLCheck(col, checkConstraint);
      }

   }

   private static void applyMax(Property property, ConstraintDescriptor descriptor, Dialect dialect) {
      if (Max.class.equals(descriptor.getAnnotation().annotationType())) {
         long max = ((Max)descriptor.getAnnotation()).value();
         Column col = (Column)property.getColumnIterator().next();
         String checkConstraint = col.getQuotedName(dialect) + "<=" + max;
         applySQLCheck(col, checkConstraint);
      }

   }

   private static void applySQLCheck(Column col, String checkConstraint) {
      String existingCheck = col.getCheckConstraint();
      if (StringHelper.isNotEmpty(existingCheck) && !existingCheck.contains(checkConstraint)) {
         checkConstraint = col.getCheckConstraint() + " AND " + checkConstraint;
      }

      col.setCheckConstraint(checkConstraint);
   }

   private static boolean applyNotNull(Property property, ConstraintDescriptor descriptor) {
      boolean hasNotNull = false;
      if (NotNull.class.equals(descriptor.getAnnotation().annotationType())) {
         if (!(property.getPersistentClass() instanceof SingleTableSubclass) && !property.isComposite()) {
            for(Iterator<Column> iter = property.getColumnIterator(); iter.hasNext(); hasNotNull = true) {
               ((Column)iter.next()).setNullable(false);
            }
         }

         hasNotNull = true;
      }

      return hasNotNull;
   }

   private static void applyDigits(Property property, ConstraintDescriptor descriptor) {
      if (Digits.class.equals(descriptor.getAnnotation().annotationType())) {
         int integerDigits = ((Digits)descriptor.getAnnotation()).integer();
         int fractionalDigits = ((Digits)descriptor.getAnnotation()).fraction();
         Column col = (Column)property.getColumnIterator().next();
         col.setPrecision(integerDigits + fractionalDigits);
         col.setScale(fractionalDigits);
      }

   }

   private static void applySize(Property property, ConstraintDescriptor descriptor, PropertyDescriptor propertyDescriptor) {
      if (Size.class.equals(descriptor.getAnnotation().annotationType()) && String.class.equals(propertyDescriptor.getElementClass())) {
         int max = ((Size)descriptor.getAnnotation()).max();
         Column col = (Column)property.getColumnIterator().next();
         if (max < Integer.MAX_VALUE) {
            col.setLength(max);
         }
      }

   }

   private static void applyLength(Property property, ConstraintDescriptor descriptor, PropertyDescriptor propertyDescriptor) {
      if ("org.hibernate.validator.constraints.Length".equals(descriptor.getAnnotation().annotationType().getName()) && String.class.equals(propertyDescriptor.getElementClass())) {
         int max = (Integer)descriptor.getAttributes().get("max");
         Column col = (Column)property.getColumnIterator().next();
         if (max < Integer.MAX_VALUE) {
            col.setLength(max);
         }
      }

   }

   private static Property findPropertyByName(PersistentClass associatedClass, String propertyName) {
      Property property = null;
      Property idProperty = associatedClass.getIdentifierProperty();
      String idName = idProperty != null ? idProperty.getName() : null;

      try {
         if (propertyName != null && propertyName.length() != 0 && !propertyName.equals(idName)) {
            if (propertyName.indexOf(idName + ".") == 0) {
               property = idProperty;
               propertyName = propertyName.substring(idName.length() + 1);
            }

            StringTokenizer st = new StringTokenizer(propertyName, ".", false);

            while(st.hasMoreElements()) {
               String element = (String)st.nextElement();
               if (property == null) {
                  property = associatedClass.getProperty(element);
               } else {
                  if (!property.isComposite()) {
                     return null;
                  }

                  property = ((Component)property.getValue()).getProperty(element);
               }
            }
         } else {
            property = idProperty;
         }
      } catch (MappingException var9) {
         try {
            if (associatedClass.getIdentifierMapper() == null) {
               return null;
            }

            StringTokenizer st = new StringTokenizer(propertyName, ".", false);

            while(st.hasMoreElements()) {
               String element = (String)st.nextElement();
               if (property == null) {
                  property = associatedClass.getIdentifierMapper().getProperty(element);
               } else {
                  if (!property.isComposite()) {
                     return null;
                  }

                  property = ((Component)property.getValue()).getProperty(element);
               }
            }
         } catch (MappingException var8) {
            return null;
         }
      }

      return property;
   }

   private static ValidatorFactory getValidatorFactory(Map properties) {
      ValidatorFactory factory = null;
      if (properties != null) {
         Object unsafeProperty = properties.get("javax.persistence.validation.factory");
         if (unsafeProperty != null) {
            try {
               factory = (ValidatorFactory)ValidatorFactory.class.cast(unsafeProperty);
            } catch (ClassCastException var5) {
               throw new HibernateException("Property javax.persistence.validation.factory should contain an object of type " + ValidatorFactory.class.getName());
            }
         }
      }

      if (factory == null) {
         try {
            factory = Validation.buildDefaultValidatorFactory();
         } catch (Exception e) {
            throw new HibernateException("Unable to build the default ValidatorFactory", e);
         }
      }

      return factory;
   }
}
