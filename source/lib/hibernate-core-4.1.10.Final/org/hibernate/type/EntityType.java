package org.hibernate.type;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import org.dom4j.Element;
import org.dom4j.Node;
import org.hibernate.AssertionFailure;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.engine.internal.ForeignKeys;
import org.hibernate.engine.spi.EntityUniqueKey;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.PersistenceContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.util.ReflectHelper;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.entity.Joinable;
import org.hibernate.persister.entity.UniqueKeyLoadable;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.tuple.ElementWrapper;

public abstract class EntityType extends AbstractType implements AssociationType {
   private final TypeFactory.TypeScope scope;
   private final String associatedEntityName;
   protected final String uniqueKeyPropertyName;
   protected final boolean isEmbeddedInXML;
   private final boolean eager;
   private final boolean unwrapProxy;
   private transient Class returnedClass;

   /** @deprecated */
   @Deprecated
   protected EntityType(TypeFactory.TypeScope scope, String entityName, String uniqueKeyPropertyName, boolean eager, boolean isEmbeddedInXML, boolean unwrapProxy) {
      this.scope = scope;
      this.associatedEntityName = entityName;
      this.uniqueKeyPropertyName = uniqueKeyPropertyName;
      this.isEmbeddedInXML = isEmbeddedInXML;
      this.eager = eager;
      this.unwrapProxy = unwrapProxy;
   }

   protected EntityType(TypeFactory.TypeScope scope, String entityName, String uniqueKeyPropertyName, boolean eager, boolean unwrapProxy) {
      this.scope = scope;
      this.associatedEntityName = entityName;
      this.uniqueKeyPropertyName = uniqueKeyPropertyName;
      this.isEmbeddedInXML = true;
      this.eager = eager;
      this.unwrapProxy = unwrapProxy;
   }

   protected TypeFactory.TypeScope scope() {
      return this.scope;
   }

   public boolean isAssociationType() {
      return true;
   }

   public final boolean isEntityType() {
      return true;
   }

   public boolean isMutable() {
      return false;
   }

   public String toString() {
      return this.getClass().getName() + '(' + this.getAssociatedEntityName() + ')';
   }

   public String getName() {
      return this.associatedEntityName;
   }

   public boolean isReferenceToPrimaryKey() {
      return this.uniqueKeyPropertyName == null;
   }

   public String getRHSUniqueKeyPropertyName() {
      return this.uniqueKeyPropertyName;
   }

   public String getLHSPropertyName() {
      return null;
   }

   public String getPropertyName() {
      return null;
   }

   public final String getAssociatedEntityName() {
      return this.associatedEntityName;
   }

   public String getAssociatedEntityName(SessionFactoryImplementor factory) {
      return this.getAssociatedEntityName();
   }

   public Joinable getAssociatedJoinable(SessionFactoryImplementor factory) throws MappingException {
      return (Joinable)factory.getEntityPersister(this.associatedEntityName);
   }

   public final Class getReturnedClass() {
      if (this.returnedClass == null) {
         this.returnedClass = this.determineAssociatedEntityClass();
      }

      return this.returnedClass;
   }

   private Class determineAssociatedEntityClass() {
      try {
         return ReflectHelper.classForName(this.getAssociatedEntityName());
      } catch (ClassNotFoundException var2) {
         return Map.class;
      }
   }

   public Object nullSafeGet(ResultSet rs, String name, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.nullSafeGet(rs, new String[]{name}, session, owner);
   }

   public final Object nullSafeGet(ResultSet rs, String[] names, SessionImplementor session, Object owner) throws HibernateException, SQLException {
      return this.resolve(this.hydrate(rs, names, session, owner), session, owner);
   }

   public final boolean isSame(Object x, Object y) {
      return x == y;
   }

   public int compare(Object x, Object y) {
      return 0;
   }

   public Object deepCopy(Object value, SessionFactoryImplementor factory) {
      return value;
   }

   public Object replace(Object original, Object target, SessionImplementor session, Object owner, Map copyCache) throws HibernateException {
      if (original == null) {
         return null;
      } else {
         Object cached = copyCache.get(original);
         if (cached != null) {
            return cached;
         } else if (original == target) {
            return target;
         } else if (session.getContextEntityIdentifier(original) == null && ForeignKeys.isTransient(this.associatedEntityName, original, Boolean.FALSE, session)) {
            Object copy = session.getFactory().getEntityPersister(this.associatedEntityName).instantiate((Serializable)null, session);
            copyCache.put(original, copy);
            return copy;
         } else {
            Object id = this.getIdentifier(original, session);
            if (id == null) {
               throw new AssertionFailure("non-transient entity has a null id");
            } else {
               id = this.getIdentifierOrUniqueKeyType(session.getFactory()).replace(id, (Object)null, session, owner, copyCache);
               return this.resolve(id, session, owner);
            }
         }
      }
   }

   public int getHashCode(Object x, SessionFactoryImplementor factory) {
      EntityPersister persister = factory.getEntityPersister(this.associatedEntityName);
      if (!persister.canExtractIdOutOfEntity()) {
         return super.getHashCode(x);
      } else {
         Serializable id;
         if (x instanceof HibernateProxy) {
            id = ((HibernateProxy)x).getHibernateLazyInitializer().getIdentifier();
         } else {
            Class mappedClass = persister.getMappedClass();
            if (mappedClass.isAssignableFrom(x.getClass())) {
               id = persister.getIdentifier(x);
            } else {
               id = (Serializable)x;
            }
         }

         return persister.getIdentifierType().getHashCode(id, factory);
      }
   }

   public boolean isEqual(Object x, Object y, SessionFactoryImplementor factory) {
      if (x != null && y != null) {
         EntityPersister persister = factory.getEntityPersister(this.associatedEntityName);
         if (!persister.canExtractIdOutOfEntity()) {
            return super.isEqual(x, y);
         } else {
            Class mappedClass = persister.getMappedClass();
            Serializable xid;
            if (x instanceof HibernateProxy) {
               xid = ((HibernateProxy)x).getHibernateLazyInitializer().getIdentifier();
            } else if (mappedClass.isAssignableFrom(x.getClass())) {
               xid = persister.getIdentifier(x);
            } else {
               xid = (Serializable)x;
            }

            Serializable yid;
            if (y instanceof HibernateProxy) {
               yid = ((HibernateProxy)y).getHibernateLazyInitializer().getIdentifier();
            } else if (mappedClass.isAssignableFrom(y.getClass())) {
               yid = persister.getIdentifier(y);
            } else {
               yid = (Serializable)y;
            }

            return persister.getIdentifierType().isEqual(xid, yid, factory);
         }
      } else {
         return x == y;
      }
   }

   public boolean isEmbeddedInXML() {
      return this.isEmbeddedInXML;
   }

   public boolean isXMLElement() {
      return this.isEmbeddedInXML;
   }

   public Object fromXMLNode(Node xml, Mapping factory) throws HibernateException {
      return !this.isEmbeddedInXML ? this.getIdentifierType(factory).fromXMLNode(xml, factory) : xml;
   }

   public void setToXMLNode(Node node, Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (!this.isEmbeddedInXML) {
         this.getIdentifierType((Mapping)factory).setToXMLNode(node, value, factory);
      } else {
         Element elt = (Element)value;
         replaceNode(node, new ElementWrapper(elt));
      }

   }

   public String getOnCondition(String alias, SessionFactoryImplementor factory, Map enabledFilters) throws MappingException {
      return this.isReferenceToPrimaryKey() ? "" : this.getAssociatedJoinable(factory).filterFragment(alias, enabledFilters);
   }

   public Object resolve(Object value, SessionImplementor session, Object owner) throws HibernateException {
      if (this.isNotEmbedded(session)) {
         return value;
      } else if (value == null) {
         return null;
      } else if (this.isNull(owner, session)) {
         return null;
      } else {
         return this.isReferenceToPrimaryKey() ? this.resolveIdentifier((Serializable)value, session) : this.loadByUniqueKey(this.getAssociatedEntityName(), this.uniqueKeyPropertyName, value, session);
      }
   }

   public Type getSemiResolvedType(SessionFactoryImplementor factory) {
      return factory.getEntityPersister(this.associatedEntityName).getIdentifierType();
   }

   protected final Object getIdentifier(Object value, SessionImplementor session) throws HibernateException {
      if (this.isNotEmbedded(session)) {
         return value;
      } else if (this.isReferenceToPrimaryKey()) {
         return ForeignKeys.getEntityIdentifierIfNotUnsaved(this.getAssociatedEntityName(), value, session);
      } else if (value == null) {
         return null;
      } else {
         EntityPersister entityPersister = session.getFactory().getEntityPersister(this.getAssociatedEntityName());
         Object propertyValue = entityPersister.getPropertyValue(value, this.uniqueKeyPropertyName);
         Type type = entityPersister.getPropertyType(this.uniqueKeyPropertyName);
         if (type.isEntityType()) {
            propertyValue = ((EntityType)type).getIdentifier(propertyValue, session);
         }

         return propertyValue;
      }
   }

   /** @deprecated */
   @Deprecated
   protected boolean isNotEmbedded(SessionImplementor session) {
      return false;
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) {
      if (value == null) {
         return "null";
      } else {
         EntityPersister persister = factory.getEntityPersister(this.associatedEntityName);
         StringBuilder result = (new StringBuilder()).append(this.associatedEntityName);
         if (persister.hasIdentifierProperty()) {
            EntityMode entityMode = persister.getEntityMode();
            Serializable id;
            if (entityMode == null) {
               if (this.isEmbeddedInXML) {
                  throw new ClassCastException(value.getClass().getName());
               }

               id = (Serializable)value;
            } else if (value instanceof HibernateProxy) {
               HibernateProxy proxy = (HibernateProxy)value;
               id = proxy.getHibernateLazyInitializer().getIdentifier();
            } else {
               id = persister.getIdentifier(value);
            }

            result.append('#').append(persister.getIdentifierType().toLoggableString(id, factory));
         }

         return result.toString();
      }
   }

   public abstract boolean isOneToOne();

   public boolean isLogicalOneToOne() {
      return this.isOneToOne();
   }

   Type getIdentifierType(Mapping factory) {
      return factory.getIdentifierType(this.getAssociatedEntityName());
   }

   Type getIdentifierType(SessionImplementor session) {
      return this.getIdentifierType((Mapping)session.getFactory());
   }

   public final Type getIdentifierOrUniqueKeyType(Mapping factory) throws MappingException {
      if (this.isReferenceToPrimaryKey()) {
         return this.getIdentifierType(factory);
      } else {
         Type type = factory.getReferencedPropertyType(this.getAssociatedEntityName(), this.uniqueKeyPropertyName);
         if (type.isEntityType()) {
            type = ((EntityType)type).getIdentifierOrUniqueKeyType(factory);
         }

         return type;
      }
   }

   public final String getIdentifierOrUniqueKeyPropertyName(Mapping factory) throws MappingException {
      return this.isReferenceToPrimaryKey() ? factory.getIdentifierPropertyName(this.getAssociatedEntityName()) : this.uniqueKeyPropertyName;
   }

   protected abstract boolean isNullable();

   protected final Object resolveIdentifier(Serializable id, SessionImplementor session) throws HibernateException {
      boolean isProxyUnwrapEnabled = this.unwrapProxy && session.getFactory().getEntityPersister(this.getAssociatedEntityName()).isInstrumented();
      Object proxyOrEntity = session.internalLoad(this.getAssociatedEntityName(), id, this.eager, this.isNullable() && !isProxyUnwrapEnabled);
      if (proxyOrEntity instanceof HibernateProxy) {
         ((HibernateProxy)proxyOrEntity).getHibernateLazyInitializer().setUnwrap(isProxyUnwrapEnabled);
      }

      return proxyOrEntity;
   }

   protected boolean isNull(Object owner, SessionImplementor session) {
      return false;
   }

   public Object loadByUniqueKey(String entityName, String uniqueKeyPropertyName, Object key, SessionImplementor session) throws HibernateException {
      SessionFactoryImplementor factory = session.getFactory();
      UniqueKeyLoadable persister = (UniqueKeyLoadable)factory.getEntityPersister(entityName);
      EntityUniqueKey euk = new EntityUniqueKey(entityName, uniqueKeyPropertyName, key, this.getIdentifierOrUniqueKeyType(factory), persister.getEntityMode(), session.getFactory());
      PersistenceContext persistenceContext = session.getPersistenceContext();
      Object result = persistenceContext.getEntity(euk);
      if (result == null) {
         result = persister.loadByUniqueKey(uniqueKeyPropertyName, key, session);
      }

      return result == null ? null : persistenceContext.proxyFor(result);
   }
}
