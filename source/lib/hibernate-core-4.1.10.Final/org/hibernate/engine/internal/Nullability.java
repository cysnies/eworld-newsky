package org.hibernate.engine.internal;

import java.util.Iterator;
import org.hibernate.EntityMode;
import org.hibernate.HibernateException;
import org.hibernate.PropertyValueException;
import org.hibernate.bytecode.instrumentation.spi.LazyPropertyInitializer;
import org.hibernate.engine.spi.CascadingAction;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.Type;

public final class Nullability {
   private final SessionImplementor session;
   private final boolean checkNullability;

   public Nullability(SessionImplementor session) {
      this.session = session;
      this.checkNullability = session.getFactory().getSettings().isCheckNullability();
   }

   public void checkNullability(Object[] values, EntityPersister persister, boolean isUpdate) throws PropertyValueException, HibernateException {
      if (this.checkNullability) {
         boolean[] nullability = persister.getPropertyNullability();
         boolean[] checkability = isUpdate ? persister.getPropertyUpdateability() : persister.getPropertyInsertability();
         Type[] propertyTypes = persister.getPropertyTypes();

         for(int i = 0; i < values.length; ++i) {
            if (checkability[i] && values[i] != LazyPropertyInitializer.UNFETCHED_PROPERTY) {
               Object value = values[i];
               if (!nullability[i] && value == null) {
                  throw new PropertyValueException("not-null property references a null or transient value", persister.getEntityName(), persister.getPropertyNames()[i]);
               }

               if (value != null) {
                  String breakProperties = this.checkSubElementsNullability(propertyTypes[i], value);
                  if (breakProperties != null) {
                     throw new PropertyValueException("not-null property references a null or transient value", persister.getEntityName(), buildPropertyPath(persister.getPropertyNames()[i], breakProperties));
                  }
               }
            }
         }
      }

   }

   private String checkSubElementsNullability(Type propertyType, Object value) throws HibernateException {
      if (propertyType.isComponentType()) {
         return this.checkComponentNullability(value, (CompositeType)propertyType);
      } else {
         if (propertyType.isCollectionType()) {
            CollectionType collectionType = (CollectionType)propertyType;
            Type collectionElementType = collectionType.getElementType(this.session.getFactory());
            if (collectionElementType.isComponentType()) {
               CompositeType componentType = (CompositeType)collectionElementType;
               Iterator iter = CascadingAction.getLoadedElementsIterator(this.session, collectionType, value);

               while(iter.hasNext()) {
                  Object compValue = iter.next();
                  if (compValue != null) {
                     return this.checkComponentNullability(compValue, componentType);
                  }
               }
            }
         }

         return null;
      }
   }

   private String checkComponentNullability(Object value, CompositeType compType) throws HibernateException {
      boolean[] nullability = compType.getPropertyNullability();
      if (nullability != null) {
         Object[] values = compType.getPropertyValues(value, EntityMode.POJO);
         Type[] propertyTypes = compType.getSubtypes();

         for(int i = 0; i < values.length; ++i) {
            Object subvalue = values[i];
            if (!nullability[i] && subvalue == null) {
               return compType.getPropertyNames()[i];
            }

            if (subvalue != null) {
               String breakProperties = this.checkSubElementsNullability(propertyTypes[i], subvalue);
               if (breakProperties != null) {
                  return buildPropertyPath(compType.getPropertyNames()[i], breakProperties);
               }
            }
         }
      }

      return null;
   }

   private static String buildPropertyPath(String parent, String child) {
      return (new StringBuilder(parent.length() + child.length() + 1)).append(parent).append('.').append(child).toString();
   }
}
