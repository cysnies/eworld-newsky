package org.hibernate.loader.criteria;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import org.hibernate.persister.collection.QueryableCollection;
import org.hibernate.persister.entity.PropertyMapping;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

class ComponentCollectionCriteriaInfoProvider implements CriteriaInfoProvider {
   QueryableCollection persister;
   Map subTypes = new HashMap();

   ComponentCollectionCriteriaInfoProvider(QueryableCollection persister) {
      this.persister = persister;
      if (!persister.getElementType().isComponentType()) {
         throw new IllegalArgumentException("persister for role " + persister.getRole() + " is not a collection-of-component");
      } else {
         ComponentType componentType = (ComponentType)persister.getElementType();
         String[] names = componentType.getPropertyNames();
         Type[] types = componentType.getSubtypes();

         for(int i = 0; i < names.length; ++i) {
            this.subTypes.put(names[i], types[i]);
         }

      }
   }

   public String getName() {
      return this.persister.getRole();
   }

   public Serializable[] getSpaces() {
      return this.persister.getCollectionSpaces();
   }

   public PropertyMapping getPropertyMapping() {
      return this.persister;
   }

   public Type getType(String relativePath) {
      if (relativePath.indexOf(46) >= 0) {
         throw new IllegalArgumentException("dotted paths not handled (yet?!) for collection-of-component");
      } else {
         Type type = (Type)this.subTypes.get(relativePath);
         if (type == null) {
            throw new IllegalArgumentException("property " + relativePath + " not found in component of collection " + this.getName());
         } else {
            return type;
         }
      }
   }
}
