package org.hibernate.type;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.usertype.LoggableUserType;
import org.hibernate.usertype.UserCollectionType;

public class CustomCollectionType extends CollectionType {
   private final UserCollectionType userType;
   private final boolean customLogging;

   /** @deprecated */
   @Deprecated
   public CustomCollectionType(TypeFactory.TypeScope typeScope, Class userTypeClass, String role, String foreignKeyPropertyName, boolean isEmbeddedInXML) {
      super(typeScope, role, foreignKeyPropertyName, isEmbeddedInXML);
      this.userType = createUserCollectionType(userTypeClass);
      this.customLogging = LoggableUserType.class.isAssignableFrom(userTypeClass);
   }

   public CustomCollectionType(TypeFactory.TypeScope typeScope, Class userTypeClass, String role, String foreignKeyPropertyName) {
      super(typeScope, role, foreignKeyPropertyName);
      this.userType = createUserCollectionType(userTypeClass);
      this.customLogging = LoggableUserType.class.isAssignableFrom(userTypeClass);
   }

   private static UserCollectionType createUserCollectionType(Class userTypeClass) {
      if (!UserCollectionType.class.isAssignableFrom(userTypeClass)) {
         throw new MappingException("Custom type does not implement UserCollectionType: " + userTypeClass.getName());
      } else {
         try {
            return (UserCollectionType)userTypeClass.newInstance();
         } catch (InstantiationException var2) {
            throw new MappingException("Cannot instantiate custom type: " + userTypeClass.getName());
         } catch (IllegalAccessException var3) {
            throw new MappingException("IllegalAccessException trying to instantiate custom type: " + userTypeClass.getName());
         }
      }
   }

   public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) throws HibernateException {
      return this.userType.instantiate(session, persister);
   }

   public PersistentCollection wrap(SessionImplementor session, Object collection) {
      return this.userType.wrap(session, collection);
   }

   public Class getReturnedClass() {
      return this.userType.instantiate(-1).getClass();
   }

   public Object instantiate(int anticipatedType) {
      return this.userType.instantiate(anticipatedType);
   }

   public Iterator getElementsIterator(Object collection) {
      return this.userType.getElementsIterator(collection);
   }

   public boolean contains(Object collection, Object entity, SessionImplementor session) {
      return this.userType.contains(collection, entity);
   }

   public Object indexOf(Object collection, Object entity) {
      return this.userType.indexOf(collection, entity);
   }

   public Object replaceElements(Object original, Object target, Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
      CollectionPersister cp = session.getFactory().getCollectionPersister(this.getRole());
      return this.userType.replaceElements(original, target, cp, owner, copyCache, session);
   }

   protected String renderLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      return this.customLogging ? ((LoggableUserType)this.userType).toLoggableString(value, factory) : super.renderLoggableString(value, factory);
   }

   public UserCollectionType getUserType() {
      return this.userType;
   }
}
