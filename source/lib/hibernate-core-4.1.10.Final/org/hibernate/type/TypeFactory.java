package org.hibernate.type;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Properties;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.classic.Lifecycle;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.tuple.component.ComponentMetamodel;
import org.hibernate.usertype.CompositeUserType;
import org.hibernate.usertype.ParameterizedType;
import org.hibernate.usertype.UserType;
import org.jboss.logging.Logger;

public final class TypeFactory implements Serializable {
   private static final CoreMessageLogger LOG = (CoreMessageLogger)Logger.getMessageLogger(CoreMessageLogger.class, TypeFactory.class.getName());
   private final TypeScopeImpl typeScope = new TypeScopeImpl();

   public void injectSessionFactory(SessionFactoryImplementor factory) {
      this.typeScope.injectSessionFactory(factory);
   }

   public SessionFactoryImplementor resolveSessionFactory() {
      return this.typeScope.resolveFactory();
   }

   public Type byClass(Class clazz, Properties parameters) {
      if (Type.class.isAssignableFrom(clazz)) {
         return this.type(clazz, parameters);
      } else if (CompositeUserType.class.isAssignableFrom(clazz)) {
         return this.customComponent(clazz, parameters);
      } else if (UserType.class.isAssignableFrom(clazz)) {
         return this.custom(clazz, parameters);
      } else if (Lifecycle.class.isAssignableFrom(clazz)) {
         return this.manyToOne(clazz.getName());
      } else {
         return Serializable.class.isAssignableFrom(clazz) ? serializable(clazz) : null;
      }
   }

   public Type type(Class typeClass, Properties parameters) {
      try {
         Type type = (Type)typeClass.newInstance();
         injectParameters(type, parameters);
         return type;
      } catch (Exception e) {
         throw new MappingException("Could not instantiate Type: " + typeClass.getName(), e);
      }
   }

   public static void injectParameters(Object type, Properties parameters) {
      if (ParameterizedType.class.isInstance(type)) {
         ((ParameterizedType)type).setParameterValues(parameters);
      } else if (parameters != null && !parameters.isEmpty()) {
         throw new MappingException("type is not parameterized: " + type.getClass().getName());
      }

   }

   public CompositeCustomType customComponent(Class typeClass, Properties parameters) {
      return customComponent(typeClass, parameters, this.typeScope);
   }

   /** @deprecated */
   @Deprecated
   public static CompositeCustomType customComponent(Class typeClass, Properties parameters, TypeScope scope) {
      try {
         CompositeUserType userType = (CompositeUserType)typeClass.newInstance();
         injectParameters(userType, parameters);
         return new CompositeCustomType(userType);
      } catch (Exception e) {
         throw new MappingException("Unable to instantiate custom type: " + typeClass.getName(), e);
      }
   }

   /** @deprecated */
   @Deprecated
   public CollectionType customCollection(String typeName, Properties typeParameters, String role, String propertyRef, boolean embedded) {
      Class typeClass;
      try {
         typeClass = ReflectHelper.classForName(typeName);
      } catch (ClassNotFoundException cnfe) {
         throw new MappingException("user collection type class not found: " + typeName, cnfe);
      }

      CustomCollectionType result = new CustomCollectionType(this.typeScope, typeClass, role, propertyRef, embedded);
      if (typeParameters != null) {
         injectParameters(result.getUserType(), typeParameters);
      }

      return result;
   }

   public CollectionType customCollection(String typeName, Properties typeParameters, String role, String propertyRef) {
      Class typeClass;
      try {
         typeClass = ReflectHelper.classForName(typeName);
      } catch (ClassNotFoundException cnfe) {
         throw new MappingException("user collection type class not found: " + typeName, cnfe);
      }

      CustomCollectionType result = new CustomCollectionType(this.typeScope, typeClass, role, propertyRef);
      if (typeParameters != null) {
         injectParameters(result.getUserType(), typeParameters);
      }

      return result;
   }

   public CustomType custom(Class typeClass, Properties parameters) {
      return custom(typeClass, parameters, this.typeScope);
   }

   /** @deprecated */
   @Deprecated
   public static CustomType custom(Class typeClass, Properties parameters, TypeScope scope) {
      try {
         UserType userType = (UserType)typeClass.newInstance();
         injectParameters(userType, parameters);
         return new CustomType(userType);
      } catch (Exception e) {
         throw new MappingException("Unable to instantiate custom type: " + typeClass.getName(), e);
      }
   }

   public static SerializableType serializable(Class serializableClass) {
      return new SerializableType(serializableClass);
   }

   /** @deprecated */
   @Deprecated
   public EntityType oneToOne(String persistentClass, ForeignKeyDirection foreignKeyType, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, boolean isEmbeddedInXML, String entityName, String propertyName) {
      return new OneToOneType(this.typeScope, persistentClass, foreignKeyType, uniqueKeyPropertyName, lazy, unwrapProxy, isEmbeddedInXML, entityName, propertyName);
   }

   public EntityType oneToOne(String persistentClass, ForeignKeyDirection foreignKeyType, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, String entityName, String propertyName) {
      return new OneToOneType(this.typeScope, persistentClass, foreignKeyType, uniqueKeyPropertyName, lazy, unwrapProxy, entityName, propertyName);
   }

   public EntityType specialOneToOne(String persistentClass, ForeignKeyDirection foreignKeyType, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, String entityName, String propertyName) {
      return new SpecialOneToOneType(this.typeScope, persistentClass, foreignKeyType, uniqueKeyPropertyName, lazy, unwrapProxy, entityName, propertyName);
   }

   public EntityType manyToOne(String persistentClass) {
      return new ManyToOneType(this.typeScope, persistentClass);
   }

   public EntityType manyToOne(String persistentClass, boolean lazy) {
      return new ManyToOneType(this.typeScope, persistentClass, lazy);
   }

   /** @deprecated */
   @Deprecated
   public EntityType manyToOne(String persistentClass, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, boolean isEmbeddedInXML, boolean ignoreNotFound, boolean isLogicalOneToOne) {
      return new ManyToOneType(this.typeScope, persistentClass, uniqueKeyPropertyName, lazy, unwrapProxy, isEmbeddedInXML, ignoreNotFound, isLogicalOneToOne);
   }

   public EntityType manyToOne(String persistentClass, String uniqueKeyPropertyName, boolean lazy, boolean unwrapProxy, boolean ignoreNotFound, boolean isLogicalOneToOne) {
      return new ManyToOneType(this.typeScope, persistentClass, uniqueKeyPropertyName, lazy, unwrapProxy, ignoreNotFound, isLogicalOneToOne);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType array(String role, String propertyRef, boolean embedded, Class elementClass) {
      return new ArrayType(this.typeScope, role, propertyRef, elementClass, embedded);
   }

   public CollectionType array(String role, String propertyRef, Class elementClass) {
      return new ArrayType(this.typeScope, role, propertyRef, elementClass);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType list(String role, String propertyRef, boolean embedded) {
      return new ListType(this.typeScope, role, propertyRef, embedded);
   }

   public CollectionType list(String role, String propertyRef) {
      return new ListType(this.typeScope, role, propertyRef);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType bag(String role, String propertyRef, boolean embedded) {
      return new BagType(this.typeScope, role, propertyRef, embedded);
   }

   public CollectionType bag(String role, String propertyRef) {
      return new BagType(this.typeScope, role, propertyRef);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType idbag(String role, String propertyRef, boolean embedded) {
      return new IdentifierBagType(this.typeScope, role, propertyRef, embedded);
   }

   public CollectionType idbag(String role, String propertyRef) {
      return new IdentifierBagType(this.typeScope, role, propertyRef);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType map(String role, String propertyRef, boolean embedded) {
      return new MapType(this.typeScope, role, propertyRef, embedded);
   }

   public CollectionType map(String role, String propertyRef) {
      return new MapType(this.typeScope, role, propertyRef);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType orderedMap(String role, String propertyRef, boolean embedded) {
      return new OrderedMapType(this.typeScope, role, propertyRef, embedded);
   }

   public CollectionType orderedMap(String role, String propertyRef) {
      return new OrderedMapType(this.typeScope, role, propertyRef);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType sortedMap(String role, String propertyRef, boolean embedded, Comparator comparator) {
      return new SortedMapType(this.typeScope, role, propertyRef, comparator, embedded);
   }

   public CollectionType sortedMap(String role, String propertyRef, Comparator comparator) {
      return new SortedMapType(this.typeScope, role, propertyRef, comparator);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType set(String role, String propertyRef, boolean embedded) {
      return new SetType(this.typeScope, role, propertyRef, embedded);
   }

   public CollectionType set(String role, String propertyRef) {
      return new SetType(this.typeScope, role, propertyRef);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType orderedSet(String role, String propertyRef, boolean embedded) {
      return new OrderedSetType(this.typeScope, role, propertyRef, embedded);
   }

   public CollectionType orderedSet(String role, String propertyRef) {
      return new OrderedSetType(this.typeScope, role, propertyRef);
   }

   /** @deprecated */
   @Deprecated
   public CollectionType sortedSet(String role, String propertyRef, boolean embedded, Comparator comparator) {
      return new SortedSetType(this.typeScope, role, propertyRef, comparator, embedded);
   }

   public CollectionType sortedSet(String role, String propertyRef, Comparator comparator) {
      return new SortedSetType(this.typeScope, role, propertyRef, comparator);
   }

   public ComponentType component(ComponentMetamodel metamodel) {
      return new ComponentType(this.typeScope, metamodel);
   }

   public EmbeddedComponentType embeddedComponent(ComponentMetamodel metamodel) {
      return new EmbeddedComponentType(this.typeScope, metamodel);
   }

   public Type any(Type metaType, Type identifierType) {
      return new AnyType(metaType, identifierType);
   }

   private static class TypeScopeImpl implements TypeScope {
      private SessionFactoryImplementor factory;

      private TypeScopeImpl() {
      }

      public void injectSessionFactory(SessionFactoryImplementor factory) {
         if (this.factory != null) {
            TypeFactory.LOG.scopingTypesToSessionFactoryAfterAlreadyScoped(this.factory, factory);
         } else {
            TypeFactory.LOG.tracev("Scoping types to session factory {0}", factory);
         }

         this.factory = factory;
      }

      public SessionFactoryImplementor resolveFactory() {
         if (this.factory == null) {
            throw new HibernateException("SessionFactory for type scoping not yet known");
         } else {
            return this.factory;
         }
      }
   }

   public interface TypeScope extends Serializable {
      SessionFactoryImplementor resolveFactory();
   }
}
