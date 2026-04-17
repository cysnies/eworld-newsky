package org.hibernate.type;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.hibernate.HibernateException;
import org.hibernate.collection.internal.PersistentArrayHolder;
import org.hibernate.collection.spi.PersistentCollection;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;

public class ArrayType extends CollectionType {
   private final Class elementClass;
   private final Class arrayClass;

   /** @deprecated */
   @Deprecated
   public ArrayType(TypeFactory.TypeScope typeScope, String role, String propertyRef, Class elementClass, boolean isEmbeddedInXML) {
      super(typeScope, role, propertyRef, isEmbeddedInXML);
      this.elementClass = elementClass;
      this.arrayClass = Array.newInstance(elementClass, 0).getClass();
   }

   public ArrayType(TypeFactory.TypeScope typeScope, String role, String propertyRef, Class elementClass) {
      super(typeScope, role, propertyRef);
      this.elementClass = elementClass;
      this.arrayClass = Array.newInstance(elementClass, 0).getClass();
   }

   public Class getReturnedClass() {
      return this.arrayClass;
   }

   public PersistentCollection instantiate(SessionImplementor session, CollectionPersister persister, Serializable key) throws HibernateException {
      return new PersistentArrayHolder(session, persister);
   }

   public Iterator getElementsIterator(Object collection) {
      return Arrays.asList(collection).iterator();
   }

   public PersistentCollection wrap(SessionImplementor session, Object array) {
      return new PersistentArrayHolder(session, array);
   }

   public boolean isArrayType() {
      return true;
   }

   public String toLoggableString(Object value, SessionFactoryImplementor factory) throws HibernateException {
      if (value == null) {
         return "null";
      } else {
         int length = Array.getLength(value);
         List list = new ArrayList(length);
         Type elemType = this.getElementType(factory);

         for(int i = 0; i < length; ++i) {
            list.add(elemType.toLoggableString(Array.get(value, i), factory));
         }

         return list.toString();
      }
   }

   public Object instantiateResult(Object original) {
      return Array.newInstance(this.elementClass, Array.getLength(original));
   }

   public Object replaceElements(Object original, Object target, Object owner, Map copyCache, SessionImplementor session) throws HibernateException {
      int length = Array.getLength(original);
      if (length != Array.getLength(target)) {
         target = this.instantiateResult(original);
      }

      Type elemType = this.getElementType(session.getFactory());

      for(int i = 0; i < length; ++i) {
         Array.set(target, i, elemType.replace(Array.get(original, i), (Object)null, session, owner, copyCache));
      }

      return target;
   }

   public Object instantiate(int anticipatedSize) {
      throw new UnsupportedOperationException();
   }

   public Object indexOf(Object array, Object element) {
      int length = Array.getLength(array);

      for(int i = 0; i < length; ++i) {
         if (Array.get(array, i) == element) {
            return i;
         }
      }

      return null;
   }

   protected boolean initializeImmediately() {
      return true;
   }

   public boolean hasHolder() {
      return true;
   }
}
