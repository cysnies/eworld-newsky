package org.hibernate.type;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.collection.internal.PersistentSet;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

public class SetType extends CollectionType {
   /** @deprecated */
   @Deprecated
   public SetType(TypeFactory.TypeScope typeScope, String role, String propertyRef, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
   }

   public SetType(TypeFactory.TypeScope typeScope, String role, String propertyRef) {
      super(typeScope, role, propertyRef);
   }

   public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) {
      return new PersistentSet(session);
   }

   public Class getReturnedClass() {
      return Set.class;
   }

   public PersistentCollection wrap(SessionImplementor session, Object collection) {
      return new PersistentSet(session, (Set)collection);
   }

   public Object instantiate(int anticipatedSize) {
      return anticipatedSize <= 0 ? new HashSet() : new HashSet(anticipatedSize + (int)((float)anticipatedSize * 0.75F), 0.75F);
   }
}
