package org.hibernate.tuple.entity;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import org.hibernate.EntityMode;
import org.hibernate.EntityNameResolver;
import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metamodel.binding.AttributeBinding;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.property.Getter;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.PropertyAccessorFactory;
import org.hibernate.property.Setter;
import org.hibernate.proxy.ProxyFactory;
import org.hibernate.proxy.map.MapProxyFactory;
import org.hibernate.tuple.DynamicMapInstantiator;
import org.hibernate.tuple.Instantiator;
import org.hibernate.type.CompositeType;
import org.jboss.logging.Logger;

public class DynamicMapEntityTuplizer extends AbstractEntityTuplizer {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, DynamicMapEntityTuplizer.class.getName());

   DynamicMapEntityTuplizer(EntityMetamodel entityMetamodel, PersistentClass mappedEntity) {
      super(entityMetamodel, mappedEntity);
   }

   DynamicMapEntityTuplizer(EntityMetamodel entityMetamodel, EntityBinding mappedEntity) {
      super(entityMetamodel, mappedEntity);
   }

   public EntityMode getEntityMode() {
      return EntityMode.MAP;
   }

   private PropertyAccessor buildPropertyAccessor(Property mappedProperty) {
      return mappedProperty.isBackRef() ? mappedProperty.getPropertyAccessor((Class)null) : PropertyAccessorFactory.getDynamicMapPropertyAccessor();
   }

   protected Getter buildPropertyGetter(Property mappedProperty, PersistentClass mappedEntity) {
      return this.buildPropertyAccessor(mappedProperty).getGetter((Class)null, mappedProperty.getName());
   }

   protected Setter buildPropertySetter(Property mappedProperty, PersistentClass mappedEntity) {
      return this.buildPropertyAccessor(mappedProperty).getSetter((Class)null, mappedProperty.getName());
   }

   protected Instantiator buildInstantiator(PersistentClass mappingInfo) {
      return new DynamicMapInstantiator(mappingInfo);
   }

   protected ProxyFactory buildProxyFactory(PersistentClass mappingInfo, Getter idGetter, Setter idSetter) {
      ProxyFactory pf = new MapProxyFactory();

      try {
         pf.postInstantiate(this.getEntityName(), (Class)null, (Set)null, (Method)null, (Method)null, (CompositeType)null);
      } catch (HibernateException he) {
         LOG.unableToCreateProxyFactory(this.getEntityName(), he);
         pf = null;
      }

      return pf;
   }

   private PropertyAccessor buildPropertyAccessor(AttributeBinding mappedProperty) {
      return PropertyAccessorFactory.getDynamicMapPropertyAccessor();
   }

   protected Getter buildPropertyGetter(AttributeBinding mappedProperty) {
      return this.buildPropertyAccessor(mappedProperty).getGetter((Class)null, mappedProperty.getAttribute().getName());
   }

   protected Setter buildPropertySetter(AttributeBinding mappedProperty) {
      return this.buildPropertyAccessor(mappedProperty).getSetter((Class)null, mappedProperty.getAttribute().getName());
   }

   protected Instantiator buildInstantiator(EntityBinding mappingInfo) {
      return new DynamicMapInstantiator(mappingInfo);
   }

   protected ProxyFactory buildProxyFactory(EntityBinding mappingInfo, Getter idGetter, Setter idSetter) {
      ProxyFactory pf = new MapProxyFactory();

      try {
         pf.postInstantiate(this.getEntityName(), (Class)null, (Set)null, (Method)null, (Method)null, (CompositeType)null);
      } catch (HibernateException he) {
         LOG.unableToCreateProxyFactory(this.getEntityName(), he);
         pf = null;
      }

      return pf;
   }

   public Class getMappedClass() {
      return Map.class;
   }

   public Class getConcreteProxyClass() {
      return Map.class;
   }

   public boolean isInstrumented() {
      return false;
   }

   public EntityNameResolver[] getEntityNameResolvers() {
      return new EntityNameResolver[]{DynamicMapEntityTuplizer.BasicEntityNameResolver.INSTANCE};
   }

   public String determineConcreteSubclassEntityName(Object entityInstance, SessionFactoryImplementor factory) {
      return extractEmbeddedEntityName((Map)entityInstance);
   }

   public static String extractEmbeddedEntityName(Map entity) {
      return (String)entity.get("$type$");
   }

   public static class BasicEntityNameResolver implements EntityNameResolver {
      public static final BasicEntityNameResolver INSTANCE = new BasicEntityNameResolver();

      public String resolveEntityName(Object entity) {
         if (!Map.class.isInstance(entity)) {
            return null;
         } else {
            String entityName = DynamicMapEntityTuplizer.extractEmbeddedEntityName((Map)entity);
            if (entityName == null) {
               throw new HibernateException("Could not determine type of dynamic map entity");
            } else {
               return entityName;
            }
         }
      }

      public boolean equals(Object obj) {
         return this.getClass().equals(obj.getClass());
      }

      public int hashCode() {
         return this.getClass().hashCode();
      }
   }
}
