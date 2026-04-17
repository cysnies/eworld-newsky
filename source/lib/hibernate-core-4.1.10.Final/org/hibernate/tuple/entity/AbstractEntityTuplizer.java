package org.hibernate.tuple.entity;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.EntityEntry;
import org.hibernate.engine.spi.EntityKey;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventSource;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.PersistEvent;
import org.hibernate.event.spi.PersistEventListener;
import org.hibernate.id.Assigned;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.mapping.Component;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.property.Getter;
import org.hibernate.property.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.tuple.Instantiator;
import org.hibernate.tuple.StandardProperty;
import org.hibernate.tuple.VersionProperty;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.jboss.logging.Logger;

public abstract class AbstractEntityTuplizer implements EntityTuplizer {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, AbstractEntityTuplizer.class.getName());
   private final EntityMetamodel entityMetamodel;
   private final Getter idGetter;
   private final Setter idSetter;
   protected final Getter[] getters;
   protected final Setter[] setters;
   protected final int propertySpan;
   protected final boolean hasCustomAccessors;
   private final Instantiator instantiator;
   private final ProxyFactory proxyFactory;
   private final CompositeType identifierMapperType;
   private final MappedIdentifierValueMarshaller mappedIdentifierValueMarshaller;

   public Type getIdentifierMapperType() {
      return this.identifierMapperType;
   }

   protected abstract Getter buildPropertyGetter(Property var1, PersistentClass var2);

   protected abstract Setter buildPropertySetter(Property var1, PersistentClass var2);

   protected abstract Instantiator buildInstantiator(PersistentClass var1);

   protected abstract ProxyFactory buildProxyFactory(PersistentClass var1, Getter var2, Setter var3);

   protected abstract Getter buildPropertyGetter(AttributeBinding var1);

   protected abstract Setter buildPropertySetter(AttributeBinding var1);

   protected abstract Instantiator buildInstantiator(EntityBinding var1);

   protected abstract ProxyFactory buildProxyFactory(EntityBinding var1, Getter var2, Setter var3);

   public AbstractEntityTuplizer(EntityMetamodel entityMetamodel, PersistentClass mappingInfo) {
      this.entityMetamodel = entityMetamodel;
      if (!entityMetamodel.getIdentifierProperty().isVirtual()) {
         this.idGetter = this.buildPropertyGetter(mappingInfo.getIdentifierProperty(), mappingInfo);
         this.idSetter = this.buildPropertySetter(mappingInfo.getIdentifierProperty(), mappingInfo);
      } else {
         this.idGetter = null;
         this.idSetter = null;
      }

      this.propertySpan = entityMetamodel.getPropertySpan();
      this.getters = new Getter[this.propertySpan];
      this.setters = new Setter[this.propertySpan];
      Iterator itr = mappingInfo.getPropertyClosureIterator();
      boolean foundCustomAccessor = false;

      for(int i = 0; itr.hasNext(); ++i) {
         Property property = (Property)itr.next();
         this.getters[i] = this.buildPropertyGetter(property, mappingInfo);
         this.setters[i] = this.buildPropertySetter(property, mappingInfo);
         if (!property.isBasicPropertyAccessor()) {
            foundCustomAccessor = true;
         }
      }

      this.hasCustomAccessors = foundCustomAccessor;
      this.instantiator = this.buildInstantiator(mappingInfo);
      if (entityMetamodel.isLazy()) {
         this.proxyFactory = this.buildProxyFactory(mappingInfo, this.idGetter, this.idSetter);
         if (this.proxyFactory == null) {
            entityMetamodel.setLazy(false);
         }
      } else {
         this.proxyFactory = null;
      }

      Component mapper = mappingInfo.getIdentifierMapper();
      if (mapper == null) {
         this.identifierMapperType = null;
         this.mappedIdentifierValueMarshaller = null;
      } else {
         this.identifierMapperType = (CompositeType)mapper.getType();
         this.mappedIdentifierValueMarshaller = buildMappedIdentifierValueMarshaller((ComponentType)entityMetamodel.getIdentifierProperty().getType(), (ComponentType)this.identifierMapperType);
      }

   }

   public AbstractEntityTuplizer(EntityMetamodel entityMetamodel, EntityBinding mappingInfo) {
      this.entityMetamodel = entityMetamodel;
      if (!entityMetamodel.getIdentifierProperty().isVirtual()) {
         this.idGetter = this.buildPropertyGetter(mappingInfo.getHierarchyDetails().getEntityIdentifier().getValueBinding());
         this.idSetter = this.buildPropertySetter(mappingInfo.getHierarchyDetails().getEntityIdentifier().getValueBinding());
      } else {
         this.idGetter = null;
         this.idSetter = null;
      }

      this.propertySpan = entityMetamodel.getPropertySpan();
      this.getters = new Getter[this.propertySpan];
      this.setters = new Setter[this.propertySpan];
      boolean foundCustomAccessor = false;
      int i = 0;

      for(AttributeBinding property : mappingInfo.getAttributeBindingClosure()) {
         if (property != mappingInfo.getHierarchyDetails().getEntityIdentifier().getValueBinding()) {
            this.getters[i] = this.buildPropertyGetter(property);
            this.setters[i] = this.buildPropertySetter(property);
            if (!property.isBasicPropertyAccessor()) {
               foundCustomAccessor = true;
            }

            ++i;
         }
      }

      this.hasCustomAccessors = foundCustomAccessor;
      this.instantiator = this.buildInstantiator(mappingInfo);
      if (entityMetamodel.isLazy()) {
         this.proxyFactory = this.buildProxyFactory(mappingInfo, this.idGetter, this.idSetter);
         if (this.proxyFactory == null) {
            entityMetamodel.setLazy(false);
         }
      } else {
         this.proxyFactory = null;
      }

      Component mapper = null;
      if (mapper == null) {
         this.identifierMapperType = null;
         this.mappedIdentifierValueMarshaller = null;
      } else {
         this.identifierMapperType = (CompositeType)mapper.getType();
         this.mappedIdentifierValueMarshaller = buildMappedIdentifierValueMarshaller((ComponentType)entityMetamodel.getIdentifierProperty().getType(), (ComponentType)this.identifierMapperType);
      }

   }

   protected String getEntityName() {
      return this.entityMetamodel.getName();
   }

   protected Set getSubclassEntityNames() {
      return this.entityMetamodel.getSubclassEntityNames();
   }

   public Serializable getIdentifier(Object entity) throws HibernateException {
      return this.getIdentifier(entity, (SessionImplementor)null);
   }

   public Serializable getIdentifier(Object entity, SessionImplementor session) {
      Object id;
      if (this.entityMetamodel.getIdentifierProperty().isEmbedded()) {
         id = entity;
      } else if (HibernateProxy.class.isInstance(entity)) {
         id = ((HibernateProxy)entity).getHibernateLazyInitializer().getIdentifier();
      } else if (this.idGetter == null) {
         if (this.identifierMapperType == null) {
            throw new HibernateException("The class has no identifier property: " + this.getEntityName());
         }

         id = this.mappedIdentifierValueMarshaller.getIdentifier(entity, this.getEntityMode(), session);
      } else {
         id = this.idGetter.get(entity);
      }

      try {
         return (Serializable)id;
      } catch (ClassCastException cce) {
         StringBuilder msg = new StringBuilder("Identifier classes must be serializable. ");
         if (id != null) {
            msg.append(id.getClass().getName()).append(" is not serializable. ");
         }

         if (cce.getMessage() != null) {
            msg.append(cce.getMessage());
         }

         throw new ClassCastException(msg.toString());
      }
   }

   public void setIdentifier(Object entity, Serializable id) throws HibernateException {
      this.setIdentifier(entity, id, (SessionImplementor)null);
   }

   public void setIdentifier(Object entity, Serializable id, SessionImplementor session) {
      if (this.entityMetamodel.getIdentifierProperty().isEmbedded()) {
         if (entity != id) {
            CompositeType copier = (CompositeType)this.entityMetamodel.getIdentifierProperty().getType();
            copier.setPropertyValues(entity, copier.getPropertyValues(id, (EntityMode)this.getEntityMode()), this.getEntityMode());
         }
      } else if (this.idSetter != null) {
         this.idSetter.set(entity, id, this.getFactory());
      } else if (this.identifierMapperType != null) {
         this.mappedIdentifierValueMarshaller.setIdentifier(entity, id, this.getEntityMode(), session);
      }

   }

   private static MappedIdentifierValueMarshaller buildMappedIdentifierValueMarshaller(ComponentType mappedIdClassComponentType, ComponentType virtualIdComponent) {
      boolean wereAllEquivalent = true;

      for(int i = 0; i < virtualIdComponent.getSubtypes().length; ++i) {
         if (virtualIdComponent.getSubtypes()[i].isEntityType() && !mappedIdClassComponentType.getSubtypes()[i].isEntityType()) {
            wereAllEquivalent = false;
            break;
         }
      }

      return (MappedIdentifierValueMarshaller)(wereAllEquivalent ? new NormalMappedIdentifierValueMarshaller(virtualIdComponent, mappedIdClassComponentType) : new IncrediblySillyJpaMapsIdMappedIdentifierValueMarshaller(virtualIdComponent, mappedIdClassComponentType));
   }

   private static Iterable persistEventListeners(SessionImplementor session) {
      return ((EventListenerRegistry)session.getFactory().getServiceRegistry().getService(EventListenerRegistry.class)).getEventListenerGroup(EventType.PERSIST).listeners();
   }

   public void resetIdentifier(Object entity, Serializable currentId, Object currentVersion) {
      this.resetIdentifier(entity, currentId, currentVersion, (SessionImplementor)null);
   }

   public void resetIdentifier(Object entity, Serializable currentId, Object currentVersion, SessionImplementor session) {
      if (!(this.entityMetamodel.getIdentifierProperty().getIdentifierGenerator() instanceof Assigned)) {
         Serializable result = this.entityMetamodel.getIdentifierProperty().getUnsavedValue().getDefaultValue(currentId);
         this.setIdentifier(entity, result, session);
         VersionProperty versionProperty = this.entityMetamodel.getVersionProperty();
         if (this.entityMetamodel.isVersioned()) {
            this.setPropertyValue(entity, this.entityMetamodel.getVersionPropertyIndex(), versionProperty.getUnsavedValue().getDefaultValue(currentVersion));
         }
      }

   }

   public Object getVersion(Object entity) throws HibernateException {
      return !this.entityMetamodel.isVersioned() ? null : this.getters[this.entityMetamodel.getVersionPropertyIndex()].get(entity);
   }

   protected boolean shouldGetAllProperties(Object entity) {
      return !this.hasUninitializedLazyProperties(entity);
   }

   public Object[] getPropertyValues(Object entity) throws HibernateException {
      boolean getAll = this.shouldGetAllProperties(entity);
      int span = this.entityMetamodel.getPropertySpan();
      Object[] result = new Object[span];

      for(int j = 0; j < span; ++j) {
         StandardProperty property = this.entityMetamodel.getProperties()[j];
         if (!getAll && property.isLazy()) {
            result[j] = LazyPropertyInitializer.UNFETCHED_PROPERTY;
         } else {
            result[j] = this.getters[j].get(entity);
         }
      }

      return result;
   }

   public Object[] getPropertyValuesToInsert(Object entity, Map mergeMap, SessionImplementor session) throws HibernateException {
      int span = this.entityMetamodel.getPropertySpan();
      Object[] result = new Object[span];

      for(int j = 0; j < span; ++j) {
         result[j] = this.getters[j].getForInsert(entity, mergeMap, session);
      }

      return result;
   }

   public Object getPropertyValue(Object entity, int i) throws HibernateException {
      return this.getters[i].get(entity);
   }

   public Object getPropertyValue(Object entity, String propertyPath) throws HibernateException {
      int loc = propertyPath.indexOf(46);
      String basePropertyName = loc > 0 ? propertyPath.substring(0, loc) : propertyPath;
      Integer index = this.entityMetamodel.getPropertyIndexOrNull(basePropertyName);
      if (index == null) {
         propertyPath = "_identifierMapper." + propertyPath;
         loc = propertyPath.indexOf(46);
         basePropertyName = loc > 0 ? propertyPath.substring(0, loc) : propertyPath;
      }

      index = this.entityMetamodel.getPropertyIndexOrNull(basePropertyName);
      Object baseValue = this.getPropertyValue(entity, index);
      if (loc > 0) {
         return baseValue == null ? null : this.getComponentValue((ComponentType)this.entityMetamodel.getPropertyTypes()[index], baseValue, propertyPath.substring(loc + 1));
      } else {
         return baseValue;
      }
   }

   protected Object getComponentValue(ComponentType type, Object component, String propertyPath) {
      int loc = propertyPath.indexOf(46);
      String basePropertyName = loc > 0 ? propertyPath.substring(0, loc) : propertyPath;
      int index = this.findSubPropertyIndex(type, basePropertyName);
      Object baseValue = type.getPropertyValue(component, index, this.getEntityMode());
      if (loc > 0) {
         return baseValue == null ? null : this.getComponentValue((ComponentType)type.getSubtypes()[index], baseValue, propertyPath.substring(loc + 1));
      } else {
         return baseValue;
      }
   }

   private int findSubPropertyIndex(ComponentType type, String subPropertyName) {
      String[] propertyNames = type.getPropertyNames();

      for(int index = 0; index < propertyNames.length; ++index) {
         if (subPropertyName.equals(propertyNames[index])) {
            return index;
         }
      }

      throw new MappingException("component property not found: " + subPropertyName);
   }

   public void setPropertyValues(Object entity, Object[] values) throws HibernateException {
      boolean setAll = !this.entityMetamodel.hasLazyProperties();

      for(int j = 0; j < this.entityMetamodel.getPropertySpan(); ++j) {
         if (setAll || values[j] != LazyPropertyInitializer.UNFETCHED_PROPERTY) {
            this.setters[j].set(entity, values[j], this.getFactory());
         }
      }

   }

   public void setPropertyValue(Object entity, int i, Object value) throws HibernateException {
      this.setters[i].set(entity, value, this.getFactory());
   }

   public void setPropertyValue(Object entity, String propertyName, Object value) throws HibernateException {
      this.setters[this.entityMetamodel.getPropertyIndex(propertyName)].set(entity, value, this.getFactory());
   }

   public final Object instantiate(Serializable id) throws HibernateException {
      return this.instantiate(id, (SessionImplementor)null);
   }

   public final Object instantiate(Serializable id, SessionImplementor session) {
      Object result = this.getInstantiator().instantiate(id);
      if (id != null) {
         this.setIdentifier(result, id, session);
      }

      return result;
   }

   public final Object instantiate() throws HibernateException {
      return this.instantiate((Serializable)null, (SessionImplementor)null);
   }

   public void afterInitialize(Object entity, boolean lazyPropertiesAreUnfetched, SessionImplementor session) {
   }

   public boolean hasUninitializedLazyProperties(Object entity) {
      return false;
   }

   public final boolean isInstance(Object object) {
      return this.getInstantiator().isInstance(object);
   }

   public boolean hasProxy() {
      return this.entityMetamodel.isLazy();
   }

   public final Object createProxy(Serializable id, SessionImplementor session) throws HibernateException {
      return this.getProxyFactory().getProxy(id, session);
   }

   public boolean isLifecycleImplementor() {
      return false;
   }

   protected final EntityMetamodel getEntityMetamodel() {
      return this.entityMetamodel;
   }

   protected final SessionFactoryImplementor getFactory() {
      return this.entityMetamodel.getSessionFactory();
   }

   protected final Instantiator getInstantiator() {
      return this.instantiator;
   }

   protected final ProxyFactory getProxyFactory() {
      return this.proxyFactory;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.getEntityMetamodel().getName() + ')';
   }

   public Getter getIdentifierGetter() {
      return this.idGetter;
   }

   public Getter getVersionGetter() {
      return this.getEntityMetamodel().isVersioned() ? this.getGetter(this.getEntityMetamodel().getVersionPropertyIndex()) : null;
   }

   public Getter getGetter(int i) {
      return this.getters[i];
   }

   private static class NormalMappedIdentifierValueMarshaller implements MappedIdentifierValueMarshaller {
      private final ComponentType virtualIdComponent;
      private final ComponentType mappedIdentifierType;

      private NormalMappedIdentifierValueMarshaller(ComponentType virtualIdComponent, ComponentType mappedIdentifierType) {
         this.virtualIdComponent = virtualIdComponent;
         this.mappedIdentifierType = mappedIdentifierType;
      }

      public Object getIdentifier(Object entity, EntityMode entityMode, SessionImplementor session) {
         Object id = this.mappedIdentifierType.instantiate(entityMode);
         Object[] propertyValues = this.virtualIdComponent.getPropertyValues(entity, entityMode);
         this.mappedIdentifierType.setPropertyValues(id, propertyValues, entityMode);
         return id;
      }

      public void setIdentifier(Object entity, Serializable id, EntityMode entityMode, SessionImplementor session) {
         this.virtualIdComponent.setPropertyValues(entity, this.mappedIdentifierType.getPropertyValues(id, (SessionImplementor)session), entityMode);
      }
   }

   private static class IncrediblySillyJpaMapsIdMappedIdentifierValueMarshaller implements MappedIdentifierValueMarshaller {
      private final ComponentType virtualIdComponent;
      private final ComponentType mappedIdentifierType;

      private IncrediblySillyJpaMapsIdMappedIdentifierValueMarshaller(ComponentType virtualIdComponent, ComponentType mappedIdentifierType) {
         this.virtualIdComponent = virtualIdComponent;
         this.mappedIdentifierType = mappedIdentifierType;
      }

      public Object getIdentifier(Object entity, EntityMode entityMode, SessionImplementor session) {
         Object id = this.mappedIdentifierType.instantiate(entityMode);
         Object[] propertyValues = this.virtualIdComponent.getPropertyValues(entity, entityMode);
         Type[] subTypes = this.virtualIdComponent.getSubtypes();
         Type[] copierSubTypes = this.mappedIdentifierType.getSubtypes();
         Iterable<PersistEventListener> persistEventListeners = AbstractEntityTuplizer.persistEventListeners(session);
         PersistenceContext persistenceContext = session.getPersistenceContext();
         int length = subTypes.length;

         for(int i = 0; i < length; ++i) {
            if (propertyValues[i] == null) {
               throw new HibernateException("No part of a composite identifier may be null");
            }

            if (subTypes[i].isAssociationType() && !copierSubTypes[i].isAssociationType()) {
               if (session == null) {
                  throw new AssertionError("Deprecated version of getIdentifier (no session) was used but session was required");
               }

               Object subId;
               if (HibernateProxy.class.isInstance(propertyValues[i])) {
                  subId = ((HibernateProxy)propertyValues[i]).getHibernateLazyInitializer().getIdentifier();
               } else {
                  EntityEntry pcEntry = session.getPersistenceContext().getEntry(propertyValues[i]);
                  if (pcEntry != null) {
                     subId = pcEntry.getId();
                  } else {
                     AbstractEntityTuplizer.LOG.debug("Performing implicit derived identity cascade");
                     PersistEvent event = new PersistEvent((String)null, propertyValues[i], (EventSource)session);

                     for(PersistEventListener listener : persistEventListeners) {
                        listener.onPersist(event);
                     }

                     pcEntry = persistenceContext.getEntry(propertyValues[i]);
                     if (pcEntry == null || pcEntry.getId() == null) {
                        throw new HibernateException("Unable to process implicit derived identity cascade");
                     }

                     subId = pcEntry.getId();
                  }
               }

               propertyValues[i] = subId;
            }
         }

         this.mappedIdentifierType.setPropertyValues(id, propertyValues, entityMode);
         return id;
      }

      public void setIdentifier(Object entity, Serializable id, EntityMode entityMode, SessionImplementor session) {
         Object[] extractedValues = this.mappedIdentifierType.getPropertyValues(id, (EntityMode)entityMode);
         Object[] injectionValues = new Object[extractedValues.length];
         PersistenceContext persistenceContext = session.getPersistenceContext();

         for(int i = 0; i < this.virtualIdComponent.getSubtypes().length; ++i) {
            Type virtualPropertyType = this.virtualIdComponent.getSubtypes()[i];
            Type idClassPropertyType = this.mappedIdentifierType.getSubtypes()[i];
            if (virtualPropertyType.isEntityType() && !idClassPropertyType.isEntityType()) {
               if (session == null) {
                  throw new AssertionError("Deprecated version of getIdentifier (no session) was used but session was required");
               }

               String associatedEntityName = ((EntityType)virtualPropertyType).getAssociatedEntityName();
               EntityKey entityKey = session.generateEntityKey((Serializable)extractedValues[i], session.getFactory().getEntityPersister(associatedEntityName));
               Object association = persistenceContext.getProxy(entityKey);
               if (association == null) {
                  association = persistenceContext.getEntity(entityKey);
               }

               injectionValues[i] = association;
            } else {
               injectionValues[i] = extractedValues[i];
            }
         }

         this.virtualIdComponent.setPropertyValues(entity, injectionValues, entityMode);
      }
   }

   private interface MappedIdentifierValueMarshaller {
      Object getIdentifier(Object var1, EntityMode var2, SessionImplementor var3);

      void setIdentifier(Object var1, Serializable var2, EntityMode var3, SessionImplementor var4);
   }
}
