package org.hibernate.tuple.entity;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.hibernate.EntityMode;
import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.bytecode.instrumentation.internal.FieldInterceptionHelper;
import org.hibernate.bytecode.instrumentation.spi.FieldInterceptor;
import org.hibernate.bytecode.spi.ReflectionOptimizer;
import org.hibernate.cfg.Environment;
import org.hibernate.classic.Lifecycle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.mapping.Subclass;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.PojoInstantiator;
import org.hibernate.type.CompositeType;
import org.jboss.logging.Logger;

public class PojoEntityTuplizer extends AbstractEntityTuplizer {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, PojoEntityTuplizer.class.getName());
   private final Class mappedClass;
   private final Class proxyInterface;
   private final boolean lifecycleImplementor;
   private final Set lazyPropertyNames = new HashSet();
   private final ReflectionOptimizer optimizer;
   private final boolean isInstrumented;

   public PojoEntityTuplizer(EntityMetamodel entityMetamodel, PersistentClass mappedEntity) {
      super(entityMetamodel, mappedEntity);
      this.mappedClass = mappedEntity.getMappedClass();
      this.proxyInterface = mappedEntity.getProxyInterface();
      this.lifecycleImplementor = Lifecycle.class.isAssignableFrom(this.mappedClass);
      this.isInstrumented = entityMetamodel.isInstrumented();
      Iterator iter = mappedEntity.getPropertyClosureIterator();

      while(iter.hasNext()) {
         Property property = (Property)iter.next();
         if (property.isLazy()) {
            this.lazyPropertyNames.add(property.getName());
         }
      }

      String[] getterNames = new String[this.propertySpan];
      String[] setterNames = new String[this.propertySpan];
      Class[] propTypes = new Class[this.propertySpan];

      for(int i = 0; i < this.propertySpan; ++i) {
         getterNames[i] = this.getters[i].getMethodName();
         setterNames[i] = this.setters[i].getMethodName();
         propTypes[i] = this.getters[i].getReturnType();
      }

      if (!this.hasCustomAccessors && Environment.useReflectionOptimizer()) {
         this.optimizer = Environment.getBytecodeProvider().getReflectionOptimizer(this.mappedClass, getterNames, setterNames, propTypes);
      } else {
         this.optimizer = null;
      }

   }

   public PojoEntityTuplizer(EntityMetamodel entityMetamodel, EntityBinding mappedEntity) {
      super(entityMetamodel, mappedEntity);
      this.mappedClass = mappedEntity.getEntity().getClassReference();
      this.proxyInterface = (Class)mappedEntity.getProxyInterfaceType().getValue();
      this.lifecycleImplementor = Lifecycle.class.isAssignableFrom(this.mappedClass);
      this.isInstrumented = entityMetamodel.isInstrumented();

      for(AttributeBinding property : mappedEntity.getAttributeBindingClosure()) {
         if (property.isLazy()) {
            this.lazyPropertyNames.add(property.getAttribute().getName());
         }
      }

      String[] getterNames = new String[this.propertySpan];
      String[] setterNames = new String[this.propertySpan];
      Class[] propTypes = new Class[this.propertySpan];

      for(int i = 0; i < this.propertySpan; ++i) {
         getterNames[i] = this.getters[i].getMethodName();
         setterNames[i] = this.setters[i].getMethodName();
         propTypes[i] = this.getters[i].getReturnType();
      }

      if (!this.hasCustomAccessors && Environment.useReflectionOptimizer()) {
         this.optimizer = Environment.getBytecodeProvider().getReflectionOptimizer(this.mappedClass, getterNames, setterNames, propTypes);
      } else {
         this.optimizer = null;
      }

   }

   protected ProxyFactory buildProxyFactory(PersistentClass persistentClass, Getter idGetter, Setter idSetter) {
      HashSet<Class> proxyInterfaces = new HashSet();
      proxyInterfaces.add(HibernateProxy.class);
      Class mappedClass = persistentClass.getMappedClass();
      Class proxyInterface = persistentClass.getProxyInterface();
      if (proxyInterface != null && !mappedClass.equals(proxyInterface)) {
         if (!proxyInterface.isInterface()) {
            throw new MappingException("proxy must be either an interface, or the class itself: " + this.getEntityName());
         }

         proxyInterfaces.add(proxyInterface);
      }

      if (mappedClass.isInterface()) {
         proxyInterfaces.add(mappedClass);
      }

      Iterator subclasses = persistentClass.getSubclassIterator();

      while(subclasses.hasNext()) {
         Subclass subclass = (Subclass)subclasses.next();
         Class subclassProxy = subclass.getProxyInterface();
         Class subclassClass = subclass.getMappedClass();
         if (subclassProxy != null && !subclassClass.equals(subclassProxy)) {
            if (!subclassProxy.isInterface()) {
               throw new MappingException("proxy must be either an interface, or the class itself: " + subclass.getEntityName());
            }

            proxyInterfaces.add(subclassProxy);
         }
      }

      Iterator properties = persistentClass.getPropertyIterator();
      Class clazz = persistentClass.getMappedClass();

      while(properties.hasNext()) {
         Property property = (Property)properties.next();
         Method method = property.getGetter(clazz).getMethod();
         if (method != null && Modifier.isFinal(method.getModifiers())) {
            LOG.gettersOfLazyClassesCannotBeFinal(persistentClass.getEntityName(), property.getName());
         }

         method = property.getSetter(clazz).getMethod();
         if (method != null && Modifier.isFinal(method.getModifiers())) {
            LOG.settersOfLazyClassesCannotBeFinal(persistentClass.getEntityName(), property.getName());
         }
      }

      Method idGetterMethod = idGetter == null ? null : idGetter.getMethod();
      Method idSetterMethod = idSetter == null ? null : idSetter.getMethod();
      Method proxyGetIdentifierMethod = idGetterMethod != null && proxyInterface != null ? ReflectHelper.getMethod(proxyInterface, idGetterMethod) : null;
      Method proxySetIdentifierMethod = idSetterMethod != null && proxyInterface != null ? ReflectHelper.getMethod(proxyInterface, idSetterMethod) : null;
      ProxyFactory pf = this.buildProxyFactoryInternal(persistentClass, idGetter, idSetter);

      try {
         pf.postInstantiate(this.getEntityName(), mappedClass, proxyInterfaces, proxyGetIdentifierMethod, proxySetIdentifierMethod, persistentClass.hasEmbeddedIdentifier() ? (CompositeType)persistentClass.getIdentifier().getType() : null);
      } catch (HibernateException he) {
         LOG.unableToCreateProxyFactory(this.getEntityName(), he);
         pf = null;
      }

      return pf;
   }

   protected ProxyFactory buildProxyFactoryInternal(PersistentClass persistentClass, Getter idGetter, Setter idSetter) {
      return Environment.getBytecodeProvider().getProxyFactoryFactory().buildProxyFactory();
   }

   protected Instantiator buildInstantiator(PersistentClass persistentClass) {
      return this.optimizer == null ? new PojoInstantiator(persistentClass, (ReflectionOptimizer.InstantiationOptimizer)null) : new PojoInstantiator(persistentClass, this.optimizer.getInstantiationOptimizer());
   }

   protected ProxyFactory buildProxyFactory(EntityBinding entityBinding, Getter idGetter, Setter idSetter) {
      HashSet<Class> proxyInterfaces = new HashSet();
      proxyInterfaces.add(HibernateProxy.class);
      Class mappedClass = entityBinding.getEntity().getClassReference();
      Class proxyInterface = (Class)entityBinding.getProxyInterfaceType().getValue();
      if (proxyInterface != null && !mappedClass.equals(proxyInterface)) {
         if (!proxyInterface.isInterface()) {
            throw new MappingException("proxy must be either an interface, or the class itself: " + this.getEntityName());
         }

         proxyInterfaces.add(proxyInterface);
      }

      if (mappedClass.isInterface()) {
         proxyInterfaces.add(mappedClass);
      }

      for(EntityBinding subEntityBinding : entityBinding.getPostOrderSubEntityBindingClosure()) {
         Class subclassProxy = (Class)subEntityBinding.getProxyInterfaceType().getValue();
         Class subclassClass = subEntityBinding.getClassReference();
         if (subclassProxy != null && !subclassClass.equals(subclassProxy)) {
            if (!subclassProxy.isInterface()) {
               throw new MappingException("proxy must be either an interface, or the class itself: " + subEntityBinding.getEntity().getName());
            }

            proxyInterfaces.add(subclassProxy);
         }
      }

      for(AttributeBinding property : entityBinding.attributeBindings()) {
         Method method = this.getGetter(property).getMethod();
         if (method != null && Modifier.isFinal(method.getModifiers())) {
            LOG.gettersOfLazyClassesCannotBeFinal(entityBinding.getEntity().getName(), property.getAttribute().getName());
         }

         method = this.getSetter(property).getMethod();
         if (method != null && Modifier.isFinal(method.getModifiers())) {
            LOG.settersOfLazyClassesCannotBeFinal(entityBinding.getEntity().getName(), property.getAttribute().getName());
         }
      }

      Method idGetterMethod = idGetter == null ? null : idGetter.getMethod();
      Method idSetterMethod = idSetter == null ? null : idSetter.getMethod();
      Method proxyGetIdentifierMethod = idGetterMethod != null && proxyInterface != null ? ReflectHelper.getMethod(proxyInterface, idGetterMethod) : null;
      Method proxySetIdentifierMethod = idSetterMethod != null && proxyInterface != null ? ReflectHelper.getMethod(proxyInterface, idSetterMethod) : null;
      ProxyFactory pf = this.buildProxyFactoryInternal(entityBinding, idGetter, idSetter);

      try {
         pf.postInstantiate(this.getEntityName(), mappedClass, proxyInterfaces, proxyGetIdentifierMethod, proxySetIdentifierMethod, entityBinding.getHierarchyDetails().getEntityIdentifier().isEmbedded() ? (CompositeType)entityBinding.getHierarchyDetails().getEntityIdentifier().getValueBinding().getHibernateTypeDescriptor().getResolvedTypeMapping() : null);
      } catch (HibernateException he) {
         LOG.unableToCreateProxyFactory(this.getEntityName(), he);
         pf = null;
      }

      return pf;
   }

   protected ProxyFactory buildProxyFactoryInternal(EntityBinding entityBinding, Getter idGetter, Setter idSetter) {
      return Environment.getBytecodeProvider().getProxyFactoryFactory().buildProxyFactory();
   }

   protected Instantiator buildInstantiator(EntityBinding entityBinding) {
      return this.optimizer == null ? new PojoInstantiator(entityBinding, (ReflectionOptimizer.InstantiationOptimizer)null) : new PojoInstantiator(entityBinding, this.optimizer.getInstantiationOptimizer());
   }

   public void setPropertyValues(Object entity, Object[] values) throws HibernateException {
      if (!this.getEntityMetamodel().hasLazyProperties() && this.optimizer != null && this.optimizer.getAccessOptimizer() != null) {
         this.setPropertyValuesWithOptimizer(entity, values);
      } else {
         super.setPropertyValues(entity, values);
      }

   }

   public Object[] getPropertyValues(Object entity) throws HibernateException {
      return this.shouldGetAllProperties(entity) && this.optimizer != null && this.optimizer.getAccessOptimizer() != null ? this.getPropertyValuesWithOptimizer(entity) : super.getPropertyValues(entity);
   }

   public Object[] getPropertyValuesToInsert(Object entity, Map mergeMap, SessionImplementor session) throws HibernateException {
      return this.shouldGetAllProperties(entity) && this.optimizer != null && this.optimizer.getAccessOptimizer() != null ? this.getPropertyValuesWithOptimizer(entity) : super.getPropertyValuesToInsert(entity, mergeMap, session);
   }

   protected void setPropertyValuesWithOptimizer(Object object, Object[] values) {
      this.optimizer.getAccessOptimizer().setPropertyValues(object, values);
   }

   protected Object[] getPropertyValuesWithOptimizer(Object object) {
      return this.optimizer.getAccessOptimizer().getPropertyValues(object);
   }

   public EntityMode getEntityMode() {
      return EntityMode.POJO;
   }

   public Class getMappedClass() {
      return this.mappedClass;
   }

   public boolean isLifecycleImplementor() {
      return this.lifecycleImplementor;
   }

   protected Getter buildPropertyGetter(Property mappedProperty, PersistentClass mappedEntity) {
      return mappedProperty.getGetter(mappedEntity.getMappedClass());
   }

   protected Setter buildPropertySetter(Property mappedProperty, PersistentClass mappedEntity) {
      return mappedProperty.getSetter(mappedEntity.getMappedClass());
   }

   protected Getter buildPropertyGetter(AttributeBinding mappedProperty) {
      return this.getGetter(mappedProperty);
   }

   protected Setter buildPropertySetter(AttributeBinding mappedProperty) {
      return this.getSetter(mappedProperty);
   }

   private Getter getGetter(AttributeBinding mappedProperty) throws PropertyNotFoundException, MappingException {
      return this.getPropertyAccessor(mappedProperty).getGetter(mappedProperty.getContainer().getClassReference(), mappedProperty.getAttribute().getName());
   }

   private Setter getSetter(AttributeBinding mappedProperty) throws PropertyNotFoundException, MappingException {
      return this.getPropertyAccessor(mappedProperty).getSetter(mappedProperty.getContainer().getClassReference(), mappedProperty.getAttribute().getName());
   }

   private PropertyAccessor getPropertyAccessor(AttributeBinding mappedProperty) throws MappingException {
      return PropertyAccessorFactory.getPropertyAccessor(mappedProperty.getContainer().getClassReference(), mappedProperty.getPropertyAccessorName());
   }

   public Class getConcreteProxyClass() {
      return this.proxyInterface;
   }

   public void afterInitialize(Object entity, boolean lazyPropertiesAreUnfetched, SessionImplementor session) {
      if (this.isInstrumented()) {
         Set lazyProps = lazyPropertiesAreUnfetched && this.getEntityMetamodel().hasLazyProperties() ? this.lazyPropertyNames : null;
         FieldInterceptionHelper.injectFieldInterceptor(entity, this.getEntityName(), lazyProps, session);
      }

   }

   public boolean hasUninitializedLazyProperties(Object entity) {
      if (!this.getEntityMetamodel().hasLazyProperties()) {
         return false;
      } else {
         FieldInterceptor callback = FieldInterceptionHelper.extractFieldInterceptor(entity);
         return callback != null && !callback.isInitialized();
      }
   }

   public boolean isInstrumented() {
      return this.isInstrumented;
   }

   public String determineConcreteSubclassEntityName(Object entityInstance, SessionFactoryImplementor factory) {
      Class concreteEntityClass = entityInstance.getClass();
      if (concreteEntityClass == this.getMappedClass()) {
         return this.getEntityName();
      } else {
         String entityName = this.getEntityMetamodel().findEntityNameByEntityClass(concreteEntityClass);
         if (entityName == null) {
            throw new HibernateException("Unable to resolve entity name from Class [" + concreteEntityClass.getName() + "]" + " expected instance/subclass of [" + this.getEntityName() + "]");
         } else {
            return entityName;
         }
      }
   }

   public EntityNameResolver[] getEntityNameResolvers() {
      return null;
   }
}
