package org.hibernate.type;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

public class BagType extends CollectionType {
   /** @deprecated */
   @Deprecated
   public BagType(TypeFactory.TypeScope typeScope, String role, String propertyRef, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
   }

   public BagType(TypeFactory.TypeScope typeScope, String role, String propertyRef) {
      super(typeScope, role, propertyRef);
   }

   public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) throws HibernateException {
      return new PersistentBag(session);
   }

   public Class getReturnedClass() {
      return Collection.class;
   }

   public PersistentCollection wrap(SessionImplementor session, Object collection) {
      return new PersistentBag(session, (Collection)collection);
   }

   public Object instantiate(int anticipatedSize) {
      return anticipatedSize <= 0 ? new ArrayList() : new ArrayList(anticipatedSize + 1);
   }
}
